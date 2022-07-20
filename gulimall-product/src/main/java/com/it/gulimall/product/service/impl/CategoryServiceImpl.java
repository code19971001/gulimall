package com.it.gulimall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.it.common.utils.PageUtils;
import com.it.common.utils.Query;
import com.it.gulimall.product.dao.CategoryDao;
import com.it.gulimall.product.entity.CategoryEntity;
import com.it.gulimall.product.service.CategoryBrandRelationService;
import com.it.gulimall.product.service.CategoryService;
import com.it.gulimall.product.vo.Catalog2Vo;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author 龍
 */
@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {


    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    RedissonClient redissonClient;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        //1.查出所有分类
        List<CategoryEntity> entities = baseMapper.selectList(null);
        //2.组装菜单的树形结构  root-->children
        return entities.stream().filter(entity -> entity.getParentCid() == 0
        ).map(menu -> {
            menu.setChildren(getChildren(menu, entities));
            return menu;
        }).sorted(Comparator.comparingInt(menu -> (menu.getSort() == null ? 0 : menu.getSort()))
        ).collect(Collectors.toList());
    }

    /**
     * 递归设置root的子菜单，这里的数据不能存在循环，否则，可能会出现循环的情况。
     *
     * @param root ：根菜单
     * @param list ：所有的菜单
     * @return ：子菜单集合
     */
    private List<CategoryEntity> getChildren(CategoryEntity root, List<CategoryEntity> list) {
        return list.stream().filter(entity -> root.getCatId().equals(entity.getParentCid())
        ).map(categoryEntity -> {
            //1.找到子菜单
            categoryEntity.setChildren(getChildren(categoryEntity, list));
            return categoryEntity;
        }).sorted(Comparator.comparingInt(menu -> (menu.getSort() == null ? 0 : menu.getSort()))
        ).collect(Collectors.toList());
    }

    /**
     * 实际开发中使用的多为逻辑删除，实际上不会删除数据，只会在修改标志位。
     *
     * @param asList :传入需要删除的数据的id。
     */
    @Override
    public void removeMenuByIds(List<Long> asList) {
        //TODO 检查当前删除的菜单是否被别的地方所引用

        baseMapper.deleteBatchIds(asList);

    }

    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> paths = new ArrayList<>();
        CategoryEntity byId = this.getById(catelogId);
        paths.add(byId.getCatId());
        while (byId.getParentCid() != 0) {
            byId = this.getById(byId.getParentCid());
            paths.add(byId.getCatId());
        }
        Collections.reverse(paths);
        return paths.toArray(new Long[0]);
    }

    /**
     * 级联更新所有关联的数据
     * 失效模式的展示：每当修改信息就删除缓存。key的表达式如果是纯字符串需要加上单引号。
     * 同时及进行多种缓存操作：
     * 方法1：@Caching来进行组合
     *     @Caching(evict = {
     *             @CacheEvict(value = "category", key = "'getLevel1Categorys'"),
     *             @CacheEvict(value = "category", key = "'getCatalogJson'")
     *     })
     * 方法2：使用直接删除某个分区下的所有数据。@CacheEvict(value = "category",allEntries = true)
     * 存储同一类型的数据，都可以指定称为一个分区。
     *
     * @param category :需要更新的分类信息
     */
    @CacheEvict(value = "category",allEntries = true)
    @Transactional
    @Override
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        if (!StringUtils.isEmpty(category.getName())) {
            categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());
        }
    }

    /**
     * Cacheable:代表当前方法的结果需要缓存，如果缓存中存在，方法不调用，如果缓存中没有，会调用方法，然后将方法的返回缓存下来。
     * 1、定义生成的缓存使用的key，可以使用spel
     * 2、指定缓存的数据的存活时间,配置文件中设置ttl,默认未永不过期：spring.cache.redis.time-to-live=30000
     * 3、将数据保存为json格式，需要自定义缓存管理器
     */

    @Cacheable(value = {"category"}, key = "'getLevel1Categorys'")
    @Override
    public List<CategoryEntity> getLevel1Categorys() {
        return this.baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
    }

    /**
     * 如何保证缓存数据和数据库数据的一致性。
     */
    @Cacheable(value = "category", key = "'getCatalogJson'",sync = true)
    @Override
    public Map<String, List<Catalog2Vo>> getCatalogJson() {
        String catalogJson = stringRedisTemplate.opsForValue().get("getCatalogJson");
        if (!StringUtils.isEmpty(catalogJson)) {
            System.out.println("缓存命中，直接返回");
            return JSON.parseObject(catalogJson, new TypeReference<Map<String, List<Catalog2Vo>>>() {
            });
        }
        Map<String, List<Catalog2Vo>> catalogJsonFromDb;
        //因为锁和锁的名字息息相关，并发量和锁的粒度息息相关，因此锁的名字需要进行一个约定：product-12-lock,catalogJson-lock
        RLock catalogJsonLock = redissonClient.getLock("catalogJson-lock");
        catalogJsonLock.lock();
        try {
            System.out.println("缓存不命中，准备查询数据库");
            catalogJsonFromDb = getCatalogJsonFromDb();
            //将对象转化为json放到缓存中:可以兼容其他语言和平台：使用注解自动的将数据进行缓存。
           /* String jsonString = JSON.toJSONString(catalogJsonFromDb);
            stringRedisTemplate.opsForValue().set("catalogJson-lock", jsonString, 1, TimeUnit.DAYS);*/
        } finally {
            catalogJsonLock.unlock();
        }
        return catalogJsonFromDb;
    }

    /**
     * 使用setnx来设置
     * 问题：
     * 1）如果业务代码出现异常，或者删除锁之前机器挂掉了，就会无法删除锁，产生死锁现象-->设置过期时间，注意需要原子性设置。
     * 2）但是设置过期时间，我们的业务代码处理过程很长，导致key过期也没有执行结束，这个时候，别的线程抢到锁。我们可能会删除别人设置的锁。
     * 3）给每个线程设置的值都不一样，删除的时候先进行检查，防止删除别人的锁，但是获取值，判断也成功了，删除之前，锁过期了，别人重新设置了锁，还会存在误删的情况。因此获取锁并删除也需要是一个原子操作。
     * 4）使用lua脚本来实现，防止误删，但是业务的执行时间未知，导致key自动续期实现起来十分困难。
     */
    private Map<String, List<Catalog2Vo>> getCatalogJsonBySetnxAndLua() {
        String catalogJson = stringRedisTemplate.opsForValue().get("catalogJson");
        if (!StringUtils.isEmpty(catalogJson)) {
            return JSON.parseObject(catalogJson, new TypeReference<Map<String, List<Catalog2Vo>>>() {
            });
        }
        Map<String, List<Catalog2Vo>> catalogJsonFromDb = new HashMap<>();
        String lockValue = "lock_" + UUID.randomUUID();
        try {
            boolean lock = stringRedisTemplate.opsForValue().setIfAbsent("lock", lockValue, 30L, TimeUnit.SECONDS);
            //如果加锁失败就进行自旋重试
            while (!lock) {
                System.out.println("没有获取到锁，再次尝试获取");
                Thread.sleep(100);
                lock = stringRedisTemplate.opsForValue().setIfAbsent("lock", lockValue, 30L, TimeUnit.SECONDS);
            }
            System.out.println("缓存不命中，准备查询数据库");
            catalogJsonFromDb = getCatalogJsonFromDb();
            //将对象转化为json放到缓存中:可以兼容其他语言和平台
            String jsonString = JSON.toJSONString(catalogJsonFromDb);
            stringRedisTemplate.opsForValue().set("catalogJson", jsonString, 1, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            log.error("出现了异常", e);
        } finally {
            /*String curlockValue = stringRedisTemplate.opsForValue().get("lock");
                if (lockValue.equals(curlockValue)) {
                stringRedisTemplate.delete("lock");
            }*/
            String lua = "if redis.call(\"get\",KEYS[1]) == ARGV[1]\n" +
                    "then\n" +
                    "    return redis.call(\"del\",KEYS[1])\n" +
                    "else\n" +
                    "    return 0\n" +
                    "end";
            //返回0，1：
            stringRedisTemplate.execute(new DefaultRedisScript<>(lua, Long.class), Collections.singletonList("lock"), lockValue);
        }
        return catalogJsonFromDb;
    }

    private Map<String, List<Catalog2Vo>> getCatalogJsonByLocalLock() {
        String catalogJson = stringRedisTemplate.opsForValue().get("catalogJson");
        if (!StringUtils.isEmpty(catalogJson)) {
            return JSON.parseObject(catalogJson, new TypeReference<Map<String, List<Catalog2Vo>>>() {
            });
        }
        System.out.println("缓存不命中，准备查询数据库");
        synchronized (this) {
            Map<String, List<Catalog2Vo>> catalogJsonFromDb = getCatalogJsonFromDb();
            //将对象转化为json放到缓存中:可以兼容其他语言和平台
            String jsonString = JSONObject.toJSONString(catalogJsonFromDb);
            stringRedisTemplate.opsForValue().set("catalogJson", jsonString, 1, TimeUnit.DAYS);
            return catalogJsonFromDb;
        }
    }

    /**
     * 从数据库中查询并封装数据：双重check
     */
    private Map<String, List<Catalog2Vo>> getCatalogJsonFromDb() {
        String catalogJson = stringRedisTemplate.opsForValue().get("catalogJson");
        if (!StringUtils.isEmpty(catalogJson)) {
            return JSON.parseObject(catalogJson, new TypeReference<Map<String, List<Catalog2Vo>>>() {
            });
        }
        System.out.println("查询数据库----");
        List<CategoryEntity> categoryEntities = this.baseMapper.selectList(new QueryWrapper<>());
        //1.查出所有一级分类
        List<CategoryEntity> level1Categorys = getCategoryByParentCid(categoryEntities, 0L);
        return level1Categorys.stream().collect(Collectors.toMap(entity -> entity.getCatId().toString(), entity -> {
            //查到一级分类的所有二级分类
            List<CategoryEntity> category2Entities = getCategoryByParentCid(categoryEntities, entity.getCatId());
            List<Catalog2Vo> level2Categorys = null;
            if (category2Entities != null) {
                level2Categorys = category2Entities.stream().map(entity2 -> {
                    //查出当前分类的二级分类
                    //查出当前二级分类的三级分类
                    List<CategoryEntity> category3Entities = getCategoryByParentCid(categoryEntities, entity2.getCatId());
                    List<Catalog2Vo.Catalog3Vo> level3Categorys = null;
                    if (category3Entities != null) {
                        level3Categorys = category3Entities.stream().map(entity3 -> new Catalog2Vo.Catalog3Vo(entity2.getCatId().toString(), entity3.getCatId().toString(), entity3.getName())).collect(Collectors.toList());
                    }
                    return new Catalog2Vo(entity.getCatId().toString(), level3Categorys, entity2.getCatId().toString(), entity2.getName());
                }).collect(Collectors.toList());
            }
            return level2Categorys;
        }));

    }

    public List<CategoryEntity> getCategoryByParentCid(List<CategoryEntity> selectList, Long parentCid) {

        return selectList.stream().filter(categoryEntity -> categoryEntity.getParentCid().equals(parentCid)).collect(Collectors.toList());
    }

    /**
     * List<CategoryEntity> categoryEntities = this.baseMapper.selectList(new QueryWrapper<>());
     * 优化：将多次查询数据库转变为查询一次。
     */
    public Map<String, List<Catalog2Vo>> getCatalogJson2() {
        //1.查出所有一级分类
        List<CategoryEntity> level1Categorys = getLevel1Categorys();
        return level1Categorys.stream().collect(Collectors.toMap(entity -> entity.getCatId().toString(), entity -> {
            //查到一级分类的所有二级分类
            List<CategoryEntity> category2Entities = this.baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", entity.getCatId()));
            List<Catalog2Vo> level2Categorys = null;
            if (category2Entities != null) {
                level2Categorys = category2Entities.stream().map(entity2 -> {
                    //查出当前分类的二级分类
                    //查出当前二级分类的三级分类
                    List<CategoryEntity> category3Entities = this.baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", entity2.getCatId()));
                    List<Catalog2Vo.Catalog3Vo> level3Categorys = null;
                    if (category3Entities != null) {
                        level3Categorys = category3Entities.stream().map(entity3 -> new Catalog2Vo.Catalog3Vo(entity2.getCatId().toString(), entity3.getCatId().toString(), entity3.getName())).collect(Collectors.toList());
                    }
                    return new Catalog2Vo(entity.getCatId().toString(), level3Categorys, entity2.getCatId().toString(), entity2.getName());
                }).collect(Collectors.toList());
            }
            return level2Categorys;
        }));
    }


}