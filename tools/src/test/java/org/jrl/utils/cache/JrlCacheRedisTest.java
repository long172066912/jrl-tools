package org.jrl.utils.cache;

import org.jrl.tools.cache.JrlCache;
import org.jrl.tools.cache.JrlCacheKeyBuilder;
import org.jrl.tools.cache.JrlCacheUtil;
import org.jrl.tools.cache.config.DefaultJrlCacheExpireConfig;
import org.jrl.tools.cache.config.JrlCacheExpireConfig;
import org.jrl.tools.cache.core.builder.CacheLoadBuilder;
import org.jrl.tools.cache.core.builder.JrlCacheBuilder;
import org.jrl.tools.cache.extend.mesh.JrlCacheLockConfig;
import org.jrl.tools.cache.extend.mesh.JrlCacheMeshConfig;
import org.jrl.tools.cache.extend.mesh.JrlCacheMeshConnectType;
import org.jrl.tools.cache.extend.mesh.redis.JrlCacheRedisKeyBuilder;
import org.jrl.tools.cache.model.JrlCacheLoadType;
import org.jrl.tools.json.JrlJsonNoExpUtil;
import org.jrl.tools.thread.JrlThreadUtil;
import org.jrl.tools.thread.core.factory.pool.JrlPoolExecutor;
import org.jrl.tools.utils.function.AbstractJrlFunction;
import org.jrl.utils.cache.spi.JrlRedisClientUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 本地缓存测试用例
 *
 * @author JerryLong
 */
public class JrlCacheRedisTest {
    @Test
    public void test() {
        JrlCache<String, String> cache = JrlCacheBuilder.<String, String>builder("test")
                .redisNoLock("test")
                .cacheLoader(CacheLoadBuilder.<String, String>builder()
                        .cacheLoader(new AbstractJrlFunction<String, String>() {
                            @Override
                            public String apply(String s) {
                                return "test";
                            }
                        })
                        .loadType(JrlCacheLoadType.PRELOAD)
                        .preLoad("test")
                        .build())
                .build();

        Assertions.assertEquals("test", cache.get("test"));
        cache.remove("test");
        Assertions.assertFalse(cache.exists("test"));
        cache.put("test", "test1");
        Assertions.assertEquals("test1", cache.get("test"));
    }

    @Test
    public void testInt() {
        final JrlCache<String, Integer> redisCache = JrlCacheUtil.getRedisCache("test", "cacheType", new AbstractJrlFunction<String, Integer>() {
            @Override
            public Integer apply(String s) {
                return 1;
            }
        }, JrlCacheLockConfig.noLock());

        final Integer testInt = redisCache.get("testInt");
        Assertions.assertEquals(1, testInt);
        redisCache.remove("testInt");
        Assertions.assertFalse(redisCache.exists("testInt"));
    }

    @Test
    public void testLong() {
        final JrlCache<String, Long> redisCache = JrlCacheUtil.getRedisCache("test", "cacheType", new AbstractJrlFunction<String, Long>() {
            @Override
            public Long apply(String s) {
                return 2L;
            }
        }, JrlCacheLockConfig.noLock());

        final Long testInt = redisCache.get("testLong");
        Assertions.assertEquals(2L, testInt);
        redisCache.remove("testLong");
        Assertions.assertFalse(redisCache.exists("testLong"));
    }

    @Test
    public void testPreload() {
        JrlCache<String, String> cache = JrlCacheBuilder.<String, String>builder("testPreload")
                .redisNoLock("test")
                .cacheLoader(CacheLoadBuilder.<String, String>builder()
                        .cacheLoader(new AbstractJrlFunction<String, String>() {
                            @Override
                            public String apply(String s) {
                                return "test";
                            }
                        })
                        .loadType(JrlCacheLoadType.PRELOAD)
                        .preLoad("test")
                        .build()
                )
                .build();

        Assertions.assertTrue(cache.exists("test"));
    }

    @Test
    public void testSchedule() {
        JrlCache<String, String> cache = JrlCacheBuilder.<String, String>builder("testSchedule")
                .redisNoLock("test")
                .cacheLoader(CacheLoadBuilder.<String, String>builder()
                        .cacheLoader(new AbstractJrlFunction<String, String>() {
                            @Override
                            public String apply(String s) {
                                return "test";
                            }
                        })
                        .loadType(JrlCacheLoadType.PRELOAD_SCHEDULED)
                        .preLoad("test")
                        .build()
                )
                //1秒过期
                .expire(new DefaultJrlCacheExpireConfig(1, TimeUnit.SECONDS))
                .build();

        Assertions.assertTrue(cache.exists("test"));
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Assertions.assertTrue(cache.exists("test"));
    }

    @Test
    public void testModel() {
        final TestModel test = new TestModel(1, "test");
        JrlCache<String, TestModel> cache = JrlCacheUtil.getRedisCache("test", "test", new AbstractJrlFunction<String, TestModel>() {
            @Override
            public TestModel apply(String s) {
                return test;
            }
        }, JrlCacheLockConfig.noLock());

        final TestModel test1 = cache.get("testModel");
        Assertions.assertNotNull(test1);
        Assertions.assertEquals(test.age, test1.age);
        Assertions.assertEquals(test.name, test1.name);
    }

    @Test
    public void testLock() throws InterruptedException {
        JrlCache<String, TestModel> cache = JrlCacheUtil.getRedisCache("test", "cacheType", new AbstractJrlFunction<String, TestModel>() {
            @Override
            public TestModel apply(String s) {
                return new TestModel(1, "test");
            }
        }, JrlCacheLockConfig.lock(10L, TimeUnit.SECONDS));

        final JrlPoolExecutor executorService = JrlThreadUtil.newPool("testLock");
        //多线程一直get，loader里的数据都是"test"，不是主动修改的那个
        for (int i = 0; i < executorService.getExecutor().getMaximumPoolSize(); i++) {
            executorService.execute(() -> {
                while (true) {
                    Thread.sleep(1L);
                    cache.get("test");
                }
            });
        }
        Thread.sleep(200L);
        //模拟并发场景，每10毫秒修改一次数据
        for (int i = 0; i < 100; i++) {
            int finalI = i;
            cache.putWithLock("test", () -> new TestModel(finalI, "test" + finalI));
            Assertions.assertEquals(cache.get("test").getAge(), i);
            //删除
            cache.remove("test");
            Assertions.assertEquals(cache.get("test").name, "test");
            Thread.sleep(10L);
        }
        executorService.shutdownNow();
    }

    @Test
    public void testAll() {
        Map<String, TestModel> loader = new HashMap<>();
        JrlCache<String, TestModel> cache = JrlCacheUtil.getRedisCache("test", "cacheType", new AbstractJrlFunction<String, TestModel>() {
            @Override
            public TestModel apply(String s) {
                try {
                    Thread.sleep(10L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return loader.get(s);
            }
        }, JrlCacheLockConfig.lock(10L, TimeUnit.SECONDS));
        for (int i = 0; i < 10; i++) {
            loader.put("test" + i, new TestModel(i, "test" + i));
            cache.remove("test" + i);
        }


        //设置一个
        cache.put("test", new TestModel(1, "test"));
        Assertions.assertEquals(cache.get("test").getAge(), 1);
        final Map<String, TestModel> all = cache.getAll(new HashSet<>(Arrays.asList("test", "test1")));
        Assertions.assertEquals(all.size(), 2);
        Assertions.assertEquals(all.get("test1").name, "test1");
        //获取多个，并有不存在的
        final long l = System.currentTimeMillis();
        final Map<String, TestModel> all1 = cache.getAll(new HashSet<>(Arrays.asList("test", "test1", "test2", "test11", "test20")));
        Assertions.assertTrue(100 > System.currentTimeMillis() - l);
        Assertions.assertEquals(all1.size(), 5);
        Assertions.assertEquals(all1.get("test2").name, "test2");
        Assertions.assertNull(all1.get("test11"));
    }

    @Test
    public void testValueList() {
        JrlCache<String, List<String>> cache = JrlCacheUtil.getRedisCache("test", "cacheType", new AbstractJrlFunction<String, List<String>>() {
            @Override
            public List<String> apply(String s) {
                return Arrays.asList("v1", "v2", "v3");
            }
        }, JrlCacheLockConfig.lock(10L, TimeUnit.SECONDS));

        final List<String> test = cache.get("testList");
        Assertions.assertNotNull(test);
        Assertions.assertEquals(test.size(), 3);
        Assertions.assertEquals(test.get(0), "v1");

        JrlCache<String, List<TestModel>> cache1 = JrlCacheUtil.getRedisCache("test1", "cacheType", new AbstractJrlFunction<String, List<TestModel>>() {
            @Override
            public List<TestModel> apply(String s) {
                return Arrays.asList(new TestModel(1, "v1"), new TestModel(2, "v2"));
            }
        }, JrlCacheLockConfig.lock(10L, TimeUnit.SECONDS));

        final List<TestModel> test1 = cache1.get("testListModel");
        Assertions.assertNotNull(test1);
        Assertions.assertEquals(test1.size(), 2);
        Assertions.assertEquals(test1.get(0).getName(), "v1");
    }

    @Test
    public void testRedisPool() {
        final JrlCache<String, String> build = JrlCacheBuilder.<String, String>builder("test")
                .redis("cacheType", JrlCacheLockConfig.noLock())
                .cacheLoader(CacheLoadBuilder.<String, String>builder()
                        .cacheLoader(new AbstractJrlFunction<String, String>() {
                            @Override
                            public String apply(String s) {
                                return "test";
                            }
                        })
                        .build()
                )
                .connectType(JrlCacheMeshConnectType.POOL)
                .expire(DefaultJrlCacheExpireConfig.oneDay(new JrlCacheExpireConfig.ExpireRandom(0L, 86400L)))
                .build();

        Assertions.assertEquals(build.get("test"), "test");
    }

    @Test
    public void testNullValue() {
        final JrlCache<String, String> cache = JrlCacheBuilder.<String, String>builder("test")
                .redis("cacheType", JrlCacheLockConfig.noLock())
                .cacheLoader(CacheLoadBuilder.<String, String>builder()
                        .cacheLoader(new AbstractJrlFunction<String, String>() {
                            @Override
                            public String apply(String s) {
                                return null;
                            }
                        })
                        .build()
                )
                .connectType(JrlCacheMeshConnectType.POOL)
                .nullValue("aaaaa")
                .expire(DefaultJrlCacheExpireConfig.oneDay(new JrlCacheExpireConfig.ExpireRandom(0L, 86400L)))
                .build();

        Assertions.assertNull(cache.get("testNull"));
        final JrlCacheMeshConfig<String, String> config = (JrlCacheMeshConfig) cache.getConfig();
        final String testNull = JrlRedisClientUtils.getCacheExecutor(config.getCacheSource(), config.getConnectType()).get("testNull");
        Assertions.assertEquals(testNull, "aaaaa");
    }

    @Test
    public void testNoNullValue() {
        final JrlCache<String, String> cache = JrlCacheBuilder.<String, String>builder("test")
                .redis("cacheType", JrlCacheLockConfig.noLock())
                .cacheLoader(CacheLoadBuilder.<String, String>builder()
                        .cacheLoader(new AbstractJrlFunction<String, String>() {
                            @Override
                            public String apply(String s) {
                                return null;
                            }
                        })
                        .build()
                )
                .connectType(JrlCacheMeshConnectType.POOL)
                .cacheNullValue(false)
                .expire(DefaultJrlCacheExpireConfig.oneDay(new JrlCacheExpireConfig.ExpireRandom(0L, 86400L)))
                .build();

        Assertions.assertNull(cache.get("testNoNull"));
        final JrlCacheMeshConfig<String, String> config = (JrlCacheMeshConfig) cache.getConfig();
        final String testNull = JrlRedisClientUtils.getCacheExecutor(config.getCacheSource(), config.getConnectType()).get("testNoNull");
        Assertions.assertNull(testNull);
    }

    public static class TestModel {
        private Integer age;
        private String name;

        public TestModel() {
        }

        public TestModel(Integer age, String name) {
            this.age = age;
            this.name = name;
        }

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @Test
    public void testKeyBuilder() {
        final JrlCache<JrlCacheKeyBuilder<TestModel, String>, TestModel> redisCacheByKeyBuilder = JrlCacheUtil.getRedisCacheByKeyBuilder("test", "cacheType", new AbstractJrlFunction<JrlCacheKeyBuilder<TestModel, String>, TestModel>() {

            @Override
            public TestModel apply(JrlCacheKeyBuilder<TestModel, String> keyBuilder) {
                return new TestModel(keyBuilder.getParam().getAge(), keyBuilder.getParam().getName());
            }
        }, JrlCacheLockConfig.noLock(), DefaultJrlCacheExpireConfig.oneDay());

        final TestModel res = redisCacheByKeyBuilder.get(new JrlCacheKeyBuilder<TestModel, String>() {
            @Override
            public TestModel getParam() {
                return new TestModel(2, "b");
            }

            @Override
            public String build() {
                return redisCacheByKeyBuilder + "2-b";
            }
        });
        Assertions.assertNotNull(res);
        Assertions.assertEquals(res.getName(), "b");
        Assertions.assertEquals(res.getAge(), 2);
    }

    @Test
    public void testKeyBuilder1() {
        final JrlCache<Req, TestModel> redisCacheByKeyBuilder = JrlCacheUtil.getRedisCacheExtKeyBuilder("test", "cacheType", new AbstractJrlFunction<Req, TestModel>() {

            @Override
            public TestModel apply(Req req) {
                return req.getModel();
            }
        }, JrlCacheLockConfig.noLock(), DefaultJrlCacheExpireConfig.oneDay());

        final TestModel res = redisCacheByKeyBuilder.get(new Req(new TestModel(1, "a")));
        Assertions.assertNotNull(res);
        Assertions.assertEquals(res.getName(), "a");
        Assertions.assertEquals(res.getAge(), 1);
    }

    @Test
    public void testKeyBuilderList() {
        final JrlCache<Req, List<TestModel>> redisCacheByKeyBuilder = JrlCacheUtil.getRedisCacheExtKeyBuilder("test", "cacheType", new AbstractJrlFunction<Req, List<TestModel>>() {

            @Override
            public List<TestModel> apply(Req req) {
                return Arrays.asList(req.getModel());
            }
        }, JrlCacheLockConfig.noLock(), DefaultJrlCacheExpireConfig.oneDay());
        final Req req = new Req(new TestModel(1, "a"));
        final List<TestModel> res = redisCacheByKeyBuilder.get(req);
        Assertions.assertNotNull(res);
        Assertions.assertEquals(res.size(), 1);
        Assertions.assertEquals(res.get(0).getName(), "a");
        Assertions.assertEquals(res.get(0).getAge(), 1);
        final List<TestModel> a = redisCacheByKeyBuilder.get(req);
        System.out.println(JrlJsonNoExpUtil.toJson(a));
        redisCacheByKeyBuilder.remove(req);
    }

    @Test
    public void testKeyBuilderMap() {
        final JrlCache<Req, Map<String, TestModel>> redisCacheByKeyBuilder = JrlCacheUtil.getRedisCacheExtKeyBuilder("test", "cacheType", new AbstractJrlFunction<Req, Map<String, TestModel>>() {

            @Override
            public Map<String, TestModel> apply(Req req) {
                if (!req.getModel().getName().equals("a")) {
                    return null;
                }
                final Map<String, TestModel> map = new HashMap<>();
                map.put("test", req.getModel());
                return map;
            }
        }, JrlCacheLockConfig.noLock(), DefaultJrlCacheExpireConfig.oneDay());
        final Req req = new Req(new TestModel(1, "a"));
        final Map<String, TestModel> res = redisCacheByKeyBuilder.get(req);
        Assertions.assertNotNull(res);
        Assertions.assertEquals(res.size(), 1);
        Assertions.assertEquals(res.get("test").getName(), "a");
        Assertions.assertEquals(res.get("test").getAge(), 1);
        final Map<String, TestModel> a = redisCacheByKeyBuilder.get(req);
        System.out.println(JrlJsonNoExpUtil.toJson(a));
        redisCacheByKeyBuilder.remove(req);

        Assertions.assertNull(redisCacheByKeyBuilder.get(new Req(new TestModel(1, "b"))));
        Assertions.assertNull(redisCacheByKeyBuilder.getIfPresent(new Req(new TestModel(1, "b"))));
        Assertions.assertFalse(redisCacheByKeyBuilder.exists(new Req(new TestModel(1, "b"))));
    }

    private static class Req extends JrlCacheRedisKeyBuilder {

        private final TestModel model;

        private Req(TestModel model) {
            this.model = model;
        }

        @Override
        public String build() {
            return "test" + model.getName() + model.getAge();
        }

        public TestModel getModel() {
            return model;
        }
    }
}
