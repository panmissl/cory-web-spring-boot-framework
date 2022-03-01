package com.cory.util;

import com.alibaba.ttl.threadpool.TtlExecutors;
import com.cory.eagleeye.EagleEye;
import com.cory.eagleeye.EagleEyeIdGenerator;
import org.slf4j.MDC;

import java.util.concurrent.*;

import static com.cory.eagleeye.EagleEye.EAGLE_EYE_ID;

/**
 * https://github.com/panmissl/transmittable-thread-local
 * @author Cory Pan
 */
public class ConcurrentUtil {

    public static ExecutorService newSingleThreadPool() {
        return newFixedThreadPool(1);
    }

    public static ExecutorService newFixedThreadPool(int num) {
        ExecutorService executorService = new ThreadPoolExecutor(num, num,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(),
                new CoryThreadFactory());

        // 额外的处理，生成修饰了的对象executorService
        return TtlExecutors.getTtlExecutorService(executorService);
    }

    public static ExecutorService newCachedThreadPool() {
        ExecutorService executorService = new ThreadPoolExecutor(0, Integer.MAX_VALUE,
                60L, TimeUnit.SECONDS,
                new SynchronousQueue<>(),
                new CoryThreadFactory());

        // 额外的处理，生成修饰了的对象executorService
        return TtlExecutors.getTtlExecutorService(executorService);
    }

    public static ExecutorService newScheduledThreadPool(int coreSize) {
        ExecutorService executorService = new ScheduledThreadPoolExecutor(coreSize, new CoryThreadFactory());

        // 额外的处理，生成修饰了的对象executorService
        return TtlExecutors.getTtlExecutorService(executorService);
    }

    public static class CoryThreadFactory implements ThreadFactory {

        @Override
        public Thread newThread(Runnable r) {
            EagleEye parentEagleEye = EagleEye.get();
            return new Thread(() -> {
                EagleEye.get().setEagleEyeId(EagleEyeIdGenerator.generateEagleEyeId(parentEagleEye.getEagleEyeId()));
                MDC.put(EAGLE_EYE_ID, EagleEye.get().getEagleEyeId());
                try {
                    r.run();
                } finally {
                    EagleEye.remove();
                    MDC.remove(EAGLE_EYE_ID);
                }
            });
        }
    }

}
