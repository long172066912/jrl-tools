package org.jrl.spi;

import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * 扩展列表信息
 *
 * @author JerryLong
 */
public class JrlSpiExtendsInfo {
    private final String name;
    private final List<JrlSpiInstance> list = new ArrayList<>();

    public JrlSpiExtendsInfo(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public List<JrlSpiInstance> getList() {
        return list;
    }

    public void add(JrlSpiInstance instance) {
        list.add(instance);
    }

    /**
     * 获取实例，无group，只根据order排序
     *
     * @param def 默认值
     * @param <T>
     * @return
     */
    public <T> T getOrDefault(Supplier<T> def) {
        if (CollectionUtils.isEmpty(list)) {
            return def.get();
        }
        final Optional<JrlSpiInstance> first = list.stream().max(Comparator.comparing(JrlSpiInstance::getOrder));
        if (first.isPresent()) {
            return (T) first.get().getInstance();
        }
        return def.get();
    }

    public <T> T get() {
        return getOrDefault(() -> null);
    }

    /**
     * 获取实例，根据group获取
     *
     * @param group
     * @param def
     * @param <T>
     * @return
     */
    public <T> T get(String group, Supplier<T> def) {
        if (CollectionUtils.isEmpty(list)) {
            return def.get();
        }
        final Optional<JrlSpiInstance> first = list.stream().filter(next -> group.equals(next.getGroup())).max(Comparator.comparing(JrlSpiInstance::getOrder));
        if (first.isPresent()) {
            return (T) first.get().getInstance();
        }
        return def.get();
    }

    public <T> T get(String group) {
        return get(group, () -> null);
    }
}
