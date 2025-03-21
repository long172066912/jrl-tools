package org.jrl.utils.thread.client;

import org.jrl.tools.thread.JrlThreadUtil;
import org.jrl.tools.thread.api.JrlLosslessTaskThreadExecutor;
import org.jrl.tools.thread.api.JrlMergeThreadExecutor;
import org.jrl.tools.thread.api.JrlShardThreadExecutor;
import org.jrl.tools.thread.api.callback.JrlThreadCallback;
import org.jrl.tools.thread.core.JrlThreadResponse;
import org.jrl.tools.thread.core.factory.JrlThreadPool;
import org.jrl.tools.thread.core.factory.JrlThreadPoolBuilder;
import org.jrl.tools.thread.core.factory.JrlThreadPoolConfig;
import org.jrl.tools.thread.core.lossless.LosslessTaskJrlThreadPoolExecutor;
import org.jrl.tools.thread.core.merge.JrlMergeRuleBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

class ThreadUtilTest {

    @Test
    void completableFutureTest() throws ExecutionException, InterruptedException {
        final JrlThreadPool demoPool = JrlThreadPoolBuilder.builder("demoPool",
                JrlThreadPoolConfig.builder()
                        .corePoolSize(10)
                        .maxPoolSize(100)
                        .keepAliveTime(10)
                        .timeunit(TimeUnit.SECONDS)
                        .preheat()
                        .queueSize(10000)
                        .rejectedExecutionHandler(ThreadPoolExecutor.CallerRunsPolicy::new)
                        .build()
        ).build();
        Assertions.assertEquals(1, CompletableFuture.supplyAsync(() -> 1, demoPool).get());
        AtomicInteger count = new AtomicInteger();
        demoPool.execute(() -> {
            //XXX
            return 1;
        }, new JrlThreadCallback<Integer>() {
            @Override
            public void onSuccess(Integer result) {
                //xxx
                count.addAndGet(result);
            }

            @Override
            public void onError(Throwable throwable) {
                //xxx
            }
        });
        Thread.sleep(10L);
        Assertions.assertEquals(1, count.get());
    }

    @Test
    void callableTest() throws ExecutionException, InterruptedException {
        Assertions.assertEquals(1, JrlThreadUtil.getDefaultPool().execute(() -> 1).get());
        Assertions.assertEquals(1, JrlThreadUtil.getDefaultPool().execute(() -> 1).get());
        Assertions.assertEquals(1, JrlThreadPoolBuilder.builder("test").build().submit(() -> 1).get());
    }

    @Test
    void runnableTest() throws InterruptedException {
        AtomicLong count = new AtomicLong();
        JrlThreadUtil.execute(count::incrementAndGet, new JrlThreadCallback() {
            @Override
            public void onError(Throwable throwable) {
                System.out.println(1111);
            }
        }, JrlThreadUtil.getDefaultPool());
        Assertions.assertEquals(0, count.get());
        Thread.sleep(10L);
        Assertions.assertEquals(1, count.get());
    }

    @Test
    void scheduleTest() throws InterruptedException {
        AtomicLong count = new AtomicLong();
        JrlThreadUtil.schedule(count::incrementAndGet, 10, 10, TimeUnit.MILLISECONDS, JrlThreadUtil.getDefaultSchedulePool());
        JrlThreadUtil.newSchedulePool("testSchedule", 1);
        JrlThreadUtil.newSchedulePool("testSchedule", JrlThreadPoolConfig.builder().schedule().build()).scheduleAtFixedRate(count::incrementAndGet, 15, 10, TimeUnit.MILLISECONDS);
        Assertions.assertEquals(0, count.get());
        Thread.sleep(11L);
        Assertions.assertEquals(1, count.get());
        Thread.sleep(4L);
        Assertions.assertEquals(2, count.get());
        Thread.sleep(5L);
        Assertions.assertEquals(3, count.get());
    }

    @Test
    void tasksTest() {
        //批量任务，无耗时
        final List<JrlThreadResponse> execute = JrlThreadUtil.executeTasks(100, Arrays.asList(() -> "1", () -> 2), JrlThreadUtil.getDefaultPool());
        Assertions.assertEquals(2, execute.size());
        Assertions.assertEquals("1", execute.get(0).getResultOrNull());
        Assertions.assertEquals(2, execute.get(1).getResultOrNull());
        //批量任务，有耗时，不超时
        long l = System.currentTimeMillis();
        final List<JrlThreadResponse> execute1 = JrlThreadUtil.executeTasks(100, Arrays.asList(() -> "1", () -> 2, () -> {
            Thread.sleep(50L);
            return 3;
        }), JrlThreadUtil.getDefaultPool());
        Assertions.assertEquals(3, execute1.size());
        Assertions.assertEquals("1", execute1.get(0).getResultOrNull());
        Assertions.assertEquals(2, execute1.get(1).getResultOrNull());
        Assertions.assertEquals(3, execute1.get(2).getResultOrNull());
        Assertions.assertTrue(System.currentTimeMillis() - l > 50L && System.currentTimeMillis() - l < 100L);
        //批量任务，有耗时，有超时，超时不会超过内部的超时时间
        l = System.currentTimeMillis();
        final List<JrlThreadResponse> execute2 = JrlThreadUtil.executeTasks(100, Arrays.asList(() -> "1", () -> {
            Thread.sleep(110L);
            return 2;
        }, () -> {
            Thread.sleep(50L);
            return 3;
        }), JrlThreadUtil.getDefaultPool());
        Assertions.assertEquals(3, execute2.size());
        Assertions.assertEquals("1", execute2.get(0).getResultOrNull());
        Assertions.assertNull(execute2.get(1).getResultOrNull());
        Assertions.assertEquals(3, execute2.get(2).getResultOrNull());
        Assertions.assertTrue(System.currentTimeMillis() - l > 100L && System.currentTimeMillis() - l < 120L, System.currentTimeMillis() - l + "");
    }

    @Test
    void shardTest() throws InterruptedException {
        int n = 0;
        Map<String, List<Integer>> map = new ConcurrentHashMap<>();
        final int shard = 10;
        final JrlShardThreadExecutor<Integer> shardTask = JrlThreadUtil.shard("shardTask", shard, v -> v % shard, JrlThreadPoolConfig.builder()
                .corePoolSize(1).maxPoolSize(2).build(), JrlThreadUtil.getDefaultPool());
        for (int i = 0; i < 100; i++) {
            for (int j = 0; j < 100; j++) {
                final int finalN = n;
                shardTask.execute(finalN,
                        (p, s, v) -> {
                            String thread = Thread.currentThread().getName();
                            thread = thread.replaceAll("JrlThreadPool-", "");
                            String poolName = thread.substring(0, thread.indexOf("-"));
//                            System.out.println(thread + " > " + s + " > " + poolName);
                            map.computeIfAbsent(poolName, k -> new ArrayList<>()).add(v);
                        });
                n++;
            }
        }
        Thread.sleep(1000L);
        Assertions.assertEquals(shard, map.size());
        map.forEach((k, v) -> {
            Assertions.assertEquals(v.size(), 1000, "pool " + k + " size error");
            final int i1 = Integer.parseInt(k.substring(k.length() - 1));
            for (int i = 0; i < v.size(); i++) {
                Assertions.assertEquals(v.get(i) % 10, i1);
            }
        });
    }

    @Test
    void mergeByCountTest() throws InterruptedException {
        List<List<Integer>> list = new ArrayList<>();
        final JrlMergeThreadExecutor<Integer> mergeThreadExecutor = JrlThreadUtil.merge("testMerge", JrlMergeRuleBuilder.<Integer>builder().onCount(100).build(), (v) -> {
            System.out.println("处理合并任务 ：" + v.size());
            System.out.println("处理合并任务 list ：" + v);
            list.add(v);
        }, JrlThreadUtil.getDefaultPool());

        final int num = 1000;
        for (int i = 0; i < num; i++) {
            mergeThreadExecutor.join(i);
        }
        Thread.sleep(100L);
        Assertions.assertEquals(list.size(), 10);
        Set<Integer> set = new HashSet<>();
        for (List<Integer> v : list) {
            Assertions.assertEquals(v.size(), 100);
            set.addAll(v);
        }
        Assertions.assertEquals(set.size(), num);
    }


    @Test
    void mergeByTimeTest() throws InterruptedException {
        List<List<Integer>> list = new ArrayList<>();
        final JrlMergeThreadExecutor<Integer> mergeThreadExecutor = JrlThreadUtil.merge("testMerge", JrlMergeRuleBuilder.<Integer>builder().onTime(10).onCount(1000).build(), (v) -> {
            System.out.println("处理合并任务 ：" + v.size());
            System.out.println("处理合并任务 list ：" + v);
            list.add(v);
        }, JrlThreadUtil.getDefaultPool());

        final int num = 1000;
        for (int i = 0; i < num; i++) {
            mergeThreadExecutor.join(i);
        }
        Thread.sleep(200L);
        Assertions.assertEquals(list.size(), 1);
        Set<Integer> set = new HashSet<>();
        for (List<Integer> v : list) {
            Assertions.assertEquals(v.size(), 1000);
            set.addAll(v);
        }
        Assertions.assertEquals(set.size(), num);
    }

    @Test
    void mergeByTimeAndCountTest() throws InterruptedException {
        List<List<Integer>> list = new ArrayList<>();
        final JrlMergeThreadExecutor<Integer> mergeThreadExecutor = JrlThreadUtil.merge("testMerge", (v) -> {
            System.out.println("处理合并任务 ：" + v.size());
            System.out.println("处理合并任务 list ：" + v);
            list.add(v);
        }, JrlThreadUtil.getDefaultPool());

        final int num = 1000;
        int n = 0;
        for (int i = 0; i < num; i++) {
            mergeThreadExecutor.join(i);
            Thread.sleep(1);
            n++;
        }
        Thread.sleep(2000L);
        Assertions.assertEquals(n, num);
        Assertions.assertTrue(list.size() > 10);
        Set<Integer> set = new HashSet<>();
        for (List<Integer> v : list) {
            Assertions.assertTrue(v.size() < 100);
            set.addAll(v);
        }
        Assertions.assertEquals(set.size(), num);
    }

    /**
     * 服务重启批量任务测试
     */
//    @Test
    void losslessTaskTest() {
        String prop = "test";
        List<String> uids = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            uids.add(i + "");
        }

        //自定义线程池
        final String name = "jrl-default-pool";
        JrlThreadPool defaultPool = JrlThreadPoolBuilder
                .builder(name, JrlThreadPoolConfig.builder()
                        .corePoolSize(5)
                        .maxPoolSize(20)
                        .queueSize(300)
                        .build())
                .build();
        List<String> faultList = new ArrayList<>();
        LosslessTaskJrlThreadPoolExecutor<String> executor = JrlThreadUtil.losslessTask(v -> {
            faultList.add(v);
            System.out.println(faultList.toString());
        }, defaultPool);

        for (String uid : uids) {
            executor.execute(new JrlLosslessTaskThreadExecutor.LossLessTaskFunction<String, Long>() {
                @Override
                public Long call() {
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    // 实现 run 方法
                    System.out.println("当前线程是:" + Thread.currentThread().getName() + ":当前用户：" + uid + "：获得了" + prop);
                    return Long.parseLong(uid);
                }

                @Override
                public String context() {
                    return uid;
                }
            });
        }
    }
}