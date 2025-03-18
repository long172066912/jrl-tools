package org.jrl.tools.utils.function;

import java.util.Objects;

/**
 * 提供一个函数接口，对外抛异常
 *
 * @author JerryLong
 */
@FunctionalInterface
public interface JrlFunction<T, R> {

    R apply(T t) throws Throwable;

    default <V> JrlFunction<V, R> compose(JrlFunction<? super V, ? extends T> before) {
        Objects.requireNonNull(before);
        return (V v) -> apply(before.apply(v));
    }

    default <V> JrlFunction<T, V> andThen(JrlFunction<? super R, ? extends V> after) {
        Objects.requireNonNull(after);
        return (T t) -> after.apply(apply(t));
    }

    static <T> JrlFunction<T, T> identity() {
        return t -> t;
    }
}
