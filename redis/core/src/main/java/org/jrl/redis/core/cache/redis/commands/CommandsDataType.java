package org.jrl.redis.core.cache.redis.commands;


import org.jrl.redis.core.constant.CommandsDataTypeEnum;
import org.jrl.redis.core.constant.CommandsReadWriteTypeEnum;

import java.lang.annotation.*;

/**
* @Title: CommandsDataType
* @Description: 自定义注解，收集redis命令的数据类型
* @author JerryLong
* @date 2021/7/8 10:41 AM
* @version V1.0
*/
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CommandsDataType {

    /**
     * 命令名称
     * @return
     */
    public String commands() default "";

    /**
     * 读写类型
     * @return
     */
    public CommandsReadWriteTypeEnum readWriteType();

    /**
     * 命令名称
     * @return
     */
    public CommandsDataTypeEnum dataType() default CommandsDataTypeEnum.STRING;
}
