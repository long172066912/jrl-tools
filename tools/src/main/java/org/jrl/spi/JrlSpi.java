package org.jrl.spi;

import java.lang.annotation.*;

/**
 * spi扩展注解
 *
 * @author JerryLong
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
public @interface JrlSpi {
    /**
     * 扩展点名称
     *
     * @return
     */
    String value() default "";

    /**
     * 扩展点排序，高的优先
     *
     * @return
     */
    int order() default 0;

    /**
     * 扩展点分组，实现类请参考分组进行实现
     * 使用 {@link JrlSpiGroup} 标注
     *
     * @return
     */
    String[] group() default {};
}
