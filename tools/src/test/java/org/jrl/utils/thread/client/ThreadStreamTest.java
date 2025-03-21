package org.jrl.utils.thread.client;

import org.jrl.tools.thread.JrlThreadUtil;
import org.jrl.tools.thread.api.JrlThreadStream;
import org.jrl.tools.thread.api.task.JrlStreamWorker;
import org.jrl.tools.thread.core.factory.JrlThreadPool;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

class ThreadStreamTest {

    @Test
    void streamExecuteTest() throws ExecutionException, InterruptedException {
        JrlThreadPool pool = JrlThreadUtil.newPool("testStream");
        final JrlStreamWorker<Integer> execute = pool.stream().execute(() -> 1);
        Assertions.assertEquals(1, execute.get());
    }

    @Test
    void streamThenRunTest() throws ExecutionException, InterruptedException {
        JrlThreadPool pool = JrlThreadUtil.newPool("testStream");
        AtomicInteger atomicInteger = new AtomicInteger();
        final JrlStreamWorker<Integer> execute = pool.stream().execute(atomicInteger::incrementAndGet);
        Assertions.assertEquals(1, execute.get());
        execute.thenRun((i) -> {
            try {
                Thread.sleep(5L);
            } catch (InterruptedException e) {
            }
            atomicInteger.addAndGet(i);
        });
        Thread.sleep(3L);
        Assertions.assertEquals(1, atomicInteger.get());
        Thread.sleep(5L);
        Assertions.assertEquals(2, atomicInteger.get());
    }


    @Test
    void streamThenSupplyTest() throws InterruptedException, ExecutionException {
        JrlThreadPool pool = JrlThreadUtil.newPool("testStream");
        AtomicInteger atomicInteger = new AtomicInteger();
        final JrlStreamWorker<Integer> execute = pool.stream().execute(atomicInteger::incrementAndGet);
        Assertions.assertEquals(1, execute.get());
        execute.thenSupply((i) -> {
            try {
                Thread.sleep(5L);
            } catch (InterruptedException e) {
            }
            return ++i;
        }).thenRun(atomicInteger::addAndGet);
        Thread.sleep(3L);
        Assertions.assertEquals(1, atomicInteger.get());
        Thread.sleep(5L);
        Assertions.assertEquals(3, atomicInteger.get());
    }

    @Test
    void streamThenRunTasksTest() throws InterruptedException {
        JrlThreadPool pool = JrlThreadUtil.newPool("testStream");
        AtomicInteger atomicInteger = new AtomicInteger();
        pool.stream().execute(() -> {
            Thread.sleep(5L);
            return 1;
        }).thenRun(new JrlStreamWorker.BaJrlStreamRunnable<>(
                Arrays.asList(
                        atomicInteger::addAndGet,
                        atomicInteger::addAndGet,
                        atomicInteger::addAndGet
                )
        ));
        Assertions.assertEquals(0, atomicInteger.get());
        Thread.sleep(5L);
        Assertions.assertEquals(3, atomicInteger.get());
    }

    @Test
    void streamThenSupplyTasksTest() throws InterruptedException, ExecutionException {
        JrlThreadPool pool = JrlThreadUtil.newPool("testStream");
        final JrlStreamWorker<Integer> worker = pool.stream().execute(() -> 1).thenSupply(new JrlStreamWorker.BaJrlStreamWorker<>(
                Arrays.asList(
                        (i) -> ++i + "",
                        (i) -> ++i + ""
                )
        )).thenSupply((list) -> list.stream().mapToInt(e -> Integer.parseInt(e.toString())).sum());
        Assertions.assertEquals(4, worker.get());
    }

    @Test
    void streamTest() throws InterruptedException, ExecutionException {
        final JrlThreadStream stream = JrlThreadUtil.stream(JrlThreadUtil.getDefaultPool());
        //第一个编排线程
        final CompletableFuture<JrlStreamWorker<Integer>> test1 = CompletableFuture.supplyAsync(() -> stream.execute(() -> 1).thenSupply(new JrlStreamWorker.BaJrlStreamWorker<Integer, Integer>(Arrays.asList(
                (i) -> i + 1
        ))).thenSupply((list) -> list.stream().mapToInt(e -> e).sum()), JrlThreadUtil.newPool("test1"));
        //第二个编排线程
        final CompletableFuture<JrlStreamWorker<Integer>> test2 = CompletableFuture.supplyAsync(() -> stream.execute(() -> 1).thenSupply(new JrlStreamWorker.BaJrlStreamWorker<Integer, Integer>(Arrays.asList(
                (i) -> i + 1,
                (i) -> i + 2
        ))).thenSupply((list) -> list.stream().mapToInt(e -> e).sum()), JrlThreadUtil.newPool("test2"));
        Assertions.assertEquals(2, test1.get().get());
        Assertions.assertEquals(5, test2.get().get());
    }


    @Test
    void streamBiTaskTest() throws InterruptedException, ExecutionException {
        JrlThreadPool pool = JrlThreadUtil.newPool("testStream");
        final JrlStreamWorker.BiJrlStreamRes<Integer, String> res = pool.stream().execute(() -> 1).thenSupply(new JrlStreamWorker.BiJrlStreamCallable<>(
                (i) -> i + 1,
                (i) -> "b"
        ));
        Assertions.assertEquals(2, res.getLeftWorker().get());
        Assertions.assertEquals("b", res.getRightWorker().get());
        //合并任务
        final JrlStreamWorker<String> worker = pool.stream().combine(res, (l, r) -> l + r).thenSupply((s) -> s + "c");
        Assertions.assertEquals("2bc", worker.get());
    }

    @Test
    void streamBiTaskTest2() throws InterruptedException, ExecutionException {
        JrlThreadPool pool = JrlThreadUtil.newPool("testStream");
        final JrlStreamWorker<Integer> worker = pool.stream().execute(() -> 1);
        final JrlStreamWorker<Integer> worker1 = worker.thenSupply((i) -> i + 1);
        final JrlStreamWorker<String> worker2 = worker.thenSupply((i) -> {
            try {
                Thread.sleep(10L);
            } catch (InterruptedException e) {
            }
            return "2";
        });
        //合并任务
        final JrlStreamWorker<String> combine = worker1.combine(worker2, (l, r) -> l + r);
        Assertions.assertEquals("22", combine.get());
    }
}