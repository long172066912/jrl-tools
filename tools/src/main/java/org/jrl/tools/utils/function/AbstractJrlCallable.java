package org.jrl.tools.utils.function;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.Callable;

/**
 * Callable抽象类，支持获取value类型
 *
 * @author JerryLong
 */
public abstract class AbstractJrlCallable<V> implements Callable<V> {
    /**
     * value类型
     */
    private Type valueType;

    public AbstractJrlCallable() {
        final ParameterizedType genericSuperclass = (ParameterizedType) this.getClass().getGenericSuperclass();
        final Type[] actualTypeArguments = genericSuperclass.getActualTypeArguments();
        this.valueType = actualTypeArguments[0];
    }

    public Type getValueType() {
        return valueType;
    }
}
