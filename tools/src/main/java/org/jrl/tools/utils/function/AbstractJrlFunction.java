package org.jrl.tools.utils.function;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.function.Function;

/**
 * function抽象类，支持获取key和value类型
 *
 * @author JerryLong
 */
public abstract class AbstractJrlFunction<T, R> implements Function<T, R> {
    /**
     * key类型
     */
    private Type keyType;
    /**
     * value类型
     */
    private Type valueType;

    public AbstractJrlFunction() {
        final ParameterizedType genericSuperclass = (ParameterizedType) this.getClass().getGenericSuperclass();
        final Type[] actualTypeArguments = genericSuperclass.getActualTypeArguments();
        this.keyType = actualTypeArguments[0];
        this.valueType = actualTypeArguments[1];
    }

    public Type getKeyType() {
        return keyType;
    }

    public Type getValueType() {
        return valueType;
    }
}
