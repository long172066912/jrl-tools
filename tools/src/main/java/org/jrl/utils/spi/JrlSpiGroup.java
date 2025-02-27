package org.jrl.utils.spi;

import java.lang.annotation.*;

/**
 * spi扩展注解-分组
 *
 * @author JerryLong
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
public @interface JrlSpiGroup {
    /**
     *  分组名称
     *
     * @return
     */
    String value() default "";
}
