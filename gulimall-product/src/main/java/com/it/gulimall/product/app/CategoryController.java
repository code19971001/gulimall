package com.it.gulimall.product.app;

import com.it.common.utils.R;
import com.it.gulimall.product.entity.CategoryEntity;
import com.it.gulimall.product.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;


/**
 * 商品三级分类
 *
 * @author code1997
 * @email p15194420545@gmail.com
 * @date 2021-01-09 10:47:32
 */
@RestController
@RequestMapping("product/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    /**
     * 查出所有分类以及子分类，以树形结构组装起来。
     */
    @RequestMapping("/list/tree")
    //@RequiresPermissions("product:category:list")
    public R listWithTree() {
        List<CategoryEntity> treeList = categoryService.listWithTree();
        return R.ok().put("data", treeList);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{catId}")
    //@RequiresPermissions("product:category:info")
    public R info(@PathVariable("catId") Long catId) {
        CategoryEntity category = categoryService.getById(catId);

        return R.ok().put("data", category);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:category:save")
    public R save(@RequestBody CategoryEntity category) {
        categoryService.save(category);

        return R.ok();
    }

    /**
     * 批量修改的方法
     */
    @RequestMapping("/update/sort")
    public R update(@RequestBody CategoryEntity[] categorys) {
        if (categorys.length == 0) {
            return R.ok();
        }
        categoryService.updateBatchById(Arrays.asList(categorys));
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:category:update")
    public R update(@RequestBody CategoryEntity category) {
        categoryService.updateCascade(category);

        return R.ok();
    }

    /**
     * 删除请求体中封装的id，这里使用逻辑删除的方式。
     *
     * @param catIds ：catId集合
     * @return
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:category:delete")
    public R delete(@RequestBody Long[] catIds) {
        System.out.println(Arrays.toString(catIds));
        //1.检查当前删除的菜单是否被别的地方引用
        categoryService.removeMenuByIds(Arrays.asList(catIds));
        return R.ok();
    }

}
