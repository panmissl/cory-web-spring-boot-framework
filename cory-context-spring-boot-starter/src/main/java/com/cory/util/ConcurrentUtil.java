package com.cory.util;

import com.alibaba.ttl.threadpool.TtlExecutors;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * https://github.com/panmissl/transmittable-thread-local
 * @author Cory Pan
 */
public class ConcurrentUtil {

    public static ExecutorService newFixedThreadPool(int num) {
        ExecutorService executorService = Executors.newFixedThreadPool(num);
        // 额外的处理，生成修饰了的对象executorService
        return TtlExecutors.getTtlExecutorService(executorService);
    }

    public static ExecutorService newSingleThreadPool() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        // 额外的处理，生成修饰了的对象executorService
        return TtlExecutors.getTtlExecutorService(executorService);
    }

    public static ExecutorService newCachedThreadPool() {
        ExecutorService executorService = Executors.newCachedThreadPool();
        // 额外的处理，生成修饰了的对象executorService
        return TtlExecutors.getTtlExecutorService(executorService);
    }

    public static ExecutorService newScheduledThreadPool(int coreSize) {
        ExecutorService executorService = Executors.newScheduledThreadPool(coreSize);
        // 额外的处理，生成修饰了的对象executorService
        return TtlExecutors.getTtlExecutorService(executorService);
    }

}
