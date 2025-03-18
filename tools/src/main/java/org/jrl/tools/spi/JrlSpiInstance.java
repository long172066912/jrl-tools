package org.jrl.tools.spi;

/**
* 扩展实例信息
* @author JerryLong
*/
public class JrlSpiInstance {
    private final String name;
    private final String group;
    private final Class<?> clazz;
    private final int order;
    private final Object instance;

    public JrlSpiInstance(String name, String group, Class<?> clazz, int order, Object instance) {
        this.name = name;
        this.group = group;
        this.clazz = clazz;
        this.order = order;
        this.instance = instance;
    }

    public String getName() {
        return name;
    }

    public String getGroup() {
        return group;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public int getOrder() {
        return order;
    }

    public Object getInstance() {
        return instance;
    }
}
