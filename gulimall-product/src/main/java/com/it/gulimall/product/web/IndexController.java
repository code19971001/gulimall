package com.it.gulimall.product.web;

import com.it.gulimall.product.entity.CategoryEntity;
import com.it.gulimall.product.service.CategoryService;
import com.it.gulimall.product.vo.Catalog2Vo;
import org.redisson.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 1.视图解析器会进行拼串：thymeleaf模板存在默认的前缀和后缀
 * private String prefix = "classpath:/templates/";
 * private String suffix = ".html";
 *
 * @author : code1997
 * @date : 2021/5/22 15:19
 */
@Controller
public class IndexController {

    @Autowired
    CategoryService categoryService;

    @Autowired
    RedissonClient redissonClient;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    /**
     * 查出所有的一级分类数据:index/catalog.json
     */
    @GetMapping({"/", "index.html"})
    public String indexPage(Model model) {
        List<CategoryEntity> categoryEntityList = categoryService.getLevel1Categorys();
        model.addAttribute("categorys", categoryEntityList);
        return "index";
    }

    /**
     * 查出所有的一级分类数据:
     */
    @GetMapping("index/catalog.json")
    @ResponseBody
    public Map<String, List<Catalog2Vo>> getCatalogJson() {
        return categoryService.getCatalogJson();
    }


    /**
     * 这是一个阻塞式的锁
     * 1）存在看门狗，可以给key自动续期，如果业务很长，运行期间自动给锁进行续期(默认30s)，不需要担心业务时间过长导致锁失效。
     * 2）加锁的业务只要运行完成，就不会给当前所进行续期，即使不自动解锁，锁默认也在30s之后自动删除。
     */
    @GetMapping("hello")
    @ResponseBody
    public String hello() {
        //如果我们设置了超时时间，就不会自动续期，无论业务是否执行完成，锁就会被删除，一旦过期了，线程再次去删除锁的时候会报错。
        RLock lock = redissonClient.getLock("my-lock");
        lock.lock();
        try {
            System.out.println("加锁成功，执行业务代码<==" + Thread.currentThread().getId());
            Thread.sleep(30000);
        } catch (Exception e) {

        } finally {
            System.out.println("释放锁<==" + Thread.currentThread().getId());
            lock.unlock();
        }
        return "hello";
    }

    /**
     * 读写锁:读读共享，读写互斥
     */
    @GetMapping("/write")
    @ResponseBody
    public String write() throws InterruptedException {
        String uuid = UUID.randomUUID().toString();
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock("read_write_lock");
        RLock writeLock = readWriteLock.writeLock();
        writeLock.lock();
        try {
            stringRedisTemplate.opsForValue().set("uuid", uuid);
            Thread.sleep(30000);
        } finally {
            writeLock.unlock();
        }
        return uuid;
    }

    @GetMapping("/read")
    @ResponseBody
    public String read() {
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock("read_write_lock");
        RLock readLock = readWriteLock.readLock();
        String uuid = "";
        readLock.lock();
        try {
            uuid = stringRedisTemplate.opsForValue().get("uuid");
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            readLock.unlock();
        }
        return uuid;
    }

    @GetMapping("/closeDoor")
    @ResponseBody
    public String closeDoor() throws InterruptedException {
        RCountDownLatch countDownLatch = redissonClient.getCountDownLatch("countDownLatch");
        System.out.println("准备锁门！");
        countDownLatch.trySetCount(3);
        countDownLatch.await();
        System.out.println("锁门成功！");
        return "锁门成功";
    }

    @GetMapping("/go")
    @ResponseBody
    public String go() throws InterruptedException {
        RCountDownLatch countDownLatch = redissonClient.getCountDownLatch("countDownLatch");
        System.out.println("go!!!");
        countDownLatch.countDown();
        return "走一个";
    }

    @GetMapping("/park")
    @ResponseBody
    public String park() throws InterruptedException {
        RSemaphore semaphore = redissonClient.getSemaphore("semaphore");
        //获取一个信号:是一个阻塞的方法
        semaphore.acquire();
        // 不会进行阻塞式的等待
        // semaphore.tryAcquire();

        return "park";
    }

    @GetMapping("/unpark")
    @ResponseBody
    public String unpark() throws InterruptedException {
        RSemaphore semaphore = redissonClient.getSemaphore("semaphore");
        //释放一个信号
        semaphore.release();
        return "unpark";
    }


}
