package org.jrl.utils.cache.model;

import org.jrl.tools.cache.JrlCacheKeyBuilder;

import java.util.Objects;

/**
* @author JerryLong
* @version V1.0
*/
public class Req extends JrlCacheKeyBuilder<Req, String> {
    private String name;
    private Integer age;

    public Req() {
    }

    public Req(String name, Integer age) {
        this.name = name;
        this.age = age;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Req)) return false;
        Req req = (Req) o;
        return name.equals(req.name) && age.equals(req.age);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, age);
    }

    public String getName() {
        return name;
    }

    public Integer getAge() {
        return age;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    @Override
    public String toString() {
        return name + age;
    }

    @Override
    public Req getParam() {
        return this;
    }

    @Override
    public String build() {
        return name + age;
    }
}
