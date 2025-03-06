package org.jrl.spi;

import org.jrl.log.JrlLoggerFactory;
import org.slf4j.Logger;

import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * spi实现类加载器
 *
 * @author JerryLong
 */
public class JrlSpiLoader {
    private static final Logger LOGGER = JrlLoggerFactory.getLogger(JrlSpiLoader.class);
    /**
     * 实例初始化列表
     */
    private static final Map<String, JrlSpiExtendsInfo> OBJECT_INSTANCE_MAP = new ConcurrentHashMap<>();

    /**
     * 获取实例
     *
     * @param clazz
     * @param def
     * @param <T>
     * @return
     */
    public static <T> T getInstanceOrDefault(Class<T> clazz, Supplier<T> def) {
        return getExtendsInfo(clazz).getOrDefault(def);
    }

    /**
     * 获取实例
     *
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T getInstance(Class<T> clazz) {
        return getExtendsInfo(clazz).get();
    }

    public static <T> T getInstance(Class<T> clazz, String group) {
        return getExtendsInfo(clazz).get(group);
    }

    /**
     * 获取扩展信息
     *
     * @param clazz
     * @param <T>
     * @return
     */
    private static <T> JrlSpiExtendsInfo getExtendsInfo(Class<T> clazz) {
        JrlSpiExtendsInfo extendsInfo = OBJECT_INSTANCE_MAP.get(clazz.getName());
        if (null == extendsInfo) {
            extendsInfo = loadExtends(clazz);
        }
        if (null == extendsInfo) {
            //加载失败使用默认的实现
            return OBJECT_INSTANCE_MAP.putIfAbsent(clazz.getName(), new JrlSpiExtendsInfo(clazz.getName()));
        }
        return extendsInfo;
    }

    /**
     * 加载并获取实例
     *
     * @param clazz
     * @param <T>
     * @return
     */
    private static <T> JrlSpiExtendsInfo loadExtends(Class<T> clazz) {
        //检测是否已加载过，不加锁，加载2次也没事
        JrlSpiExtendsInfo extendsInfo = OBJECT_INSTANCE_MAP.get(clazz.getName());
        if (null != extendsInfo) {
            return extendsInfo;
        }
        final String name = clazz.getName();
        extendsInfo = new JrlSpiExtendsInfo(name);
        try {
            final Iterator<? extends T> iterator = ServiceLoader.load(clazz).iterator();
            int i = 0;
            while (iterator.hasNext()) {
                final T next = iterator.next();
                //获取实现类的name
                final JrlSpi jrlSpi = next.getClass().getAnnotation(JrlSpi.class);
                String instanceName = null != jrlSpi ? jrlSpi.value() : next.getClass().getName();
                int order = null != jrlSpi ? jrlSpi.order() : i;
                //获取实现类的group
                final JrlSpiGroup jrlSpiGroup = next.getClass().getAnnotation(JrlSpiGroup.class);
                extendsInfo.add(
                        new JrlSpiInstance(instanceName, null != jrlSpiGroup ? jrlSpiGroup.value() : null, next.getClass(), order, next)
                );
                i++;
            }
            OBJECT_INSTANCE_MAP.put(name, extendsInfo);
        } catch (Throwable e) {
            LOGGER.error(" SdkSpiUtil fail to load spi , class : {}", clazz.getName(), e);
        }
        return extendsInfo;
    }
}
