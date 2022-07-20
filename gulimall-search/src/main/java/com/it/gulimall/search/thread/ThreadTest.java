package com.it.gulimall.search.thread;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author : code1997
 * @date : 2021/6/9 20:38
 */
public class ThreadTest {

    /**
     * 创建固定线程数的线程池
     */
    public static ExecutorService executor = Executors.newFixedThreadPool(10);

    private static void testThenCombineAsync() throws InterruptedException, ExecutionException {
        CompletableFuture<Integer> completableFuture = CompletableFuture.supplyAsync(() -> {
            System.out.println("任务1开始：" + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("任务1结果：" + i);
            return i;
        }, executor);

        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            System.out.println("任务2开始：");
            System.out.println("任务2结束");
            return "hello";
        }, executor).thenCombineAsync(completableFuture, (r1, r2) -> {
            System.out.println("任务3开始");
            System.out.println(r1 + "----->" + r2);
            System.out.println("任务3结束");
            return "成功";
        }, executor);
        System.out.println(future.get());
    }

    private static void testThenApplyAsync() {
        CompletableFuture.supplyAsync(() -> {
            System.out.println("当前线程：" + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("运行结果为：" + i);
            return i;
        }, executor).thenApplyAsync(task1Result -> task1Result * 2)
                .thenAcceptAsync(System.out::println);
        System.out.println(Thread.currentThread().getName() + "执行结束！");
    }

    private static void testCompletable() throws InterruptedException, ExecutionException {
        CompletableFuture.runAsync(() -> {
            System.out.println("当前线程：" + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("运行结果为：" + i);
        }, executor);
        CompletableFuture<Integer> supplyAsync = CompletableFuture.supplyAsync(() -> {
            System.out.println("当前线程：" + Thread.currentThread().getId());
            int i = 10 / 0;
            System.out.println("运行结果为：" + i);
            return i;
        }, executor).whenComplete((result, execution) -> {
            //result为结果，execution为异常
            System.out.println(result);
            System.out.println(execution);
        }).exceptionally(exception -> {
            //如果出现异常，如何进行处理，尽管whenComplete可以感知到异常，但是无法改变结果
            return 10;
        }).handle((result, exception) -> result == 10 ? result * 2 : result);
        System.out.println(supplyAsync.get());
        System.out.println(Thread.currentThread().getName() + "执行结束！");
    }

}
