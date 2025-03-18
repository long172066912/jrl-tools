package org.jrl.utils.crypto;

import org.jrl.tools.crypto.JrlNumberIdCryptUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.*;

/**
 * 数字固定长度加解密测试用例
 *
 * @author JerryLong
 */
class JrlNumberIdCryptoUtilTest {

    private static final String SECRET_KEY = "ae76df597179811e";
    ;

    @Test
    void test() {
        String scope = "testScope";
        String prefix = "Jrl";
        String nid = "172066912";
        String jrlId = JrlNumberIdCryptUtil.encrypt(scope, prefix, SECRET_KEY, nid);
        Assertions.assertEquals("Jrl40d70a145d5de6400aN0hRU0JCQmRnVE1rQzZ3ZlhOT3o", jrlId);
        Assertions.assertEquals(nid, JrlNumberIdCryptUtil.decrypt(scope, prefix, SECRET_KEY, jrlId));
    }

    @Test
    void test2() {
        //测试100W个数据
        Map<String, String> map = new ConcurrentHashMap<>();
        String scope = "testScope";
        String prefix = "Jrl";
        ExecutorService pool = new ThreadPoolExecutor(20, 100, 30, TimeUnit.SECONDS, new SynchronousQueue<>(), r -> new Thread(r, "test-thread-" + r.hashCode()), new ThreadPoolExecutor.CallerRunsPolicy());
        final int count = 1000000;
        CountDownLatch countDownLatch = new CountDownLatch(count);
        for (int i = 0; i < count; i++) {
            int finalI = i;
            pool.execute(() -> {
                String nid = count + finalI + "";
                String jrlId = JrlNumberIdCryptUtil.encrypt(scope, prefix, SECRET_KEY, nid);
                Assertions.assertNull(map.put(nid, jrlId));
                Assertions.assertEquals(nid, JrlNumberIdCryptUtil.decrypt(scope, prefix, SECRET_KEY, jrlId));
                countDownLatch.countDown();
            });
        }
        while (countDownLatch.getCount() > 0) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Assertions.assertEquals(count, map.size());
    }
}