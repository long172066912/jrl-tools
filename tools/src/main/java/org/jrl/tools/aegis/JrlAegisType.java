package org.jrl.tools.aegis;

/**
 * 类型，1：限流，2：熔断
 *
 * @author JerryLong
 */
public interface JrlAegisType {

    int LIMIT = 1;
    int BREAKER = 2;

    /**
     * 行为：1：限流，2：熔断
     *
     * @return 行为
     */
    int getAction();

    /**
     * 类型：具体类型
     *
     * @return 类型
     */
    int getType();

    /**
     * 类型名称
     *
     * @return 类型名称
     */
    String name();


}
