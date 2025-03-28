package org.jrl.utils.cache.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author JerryLong
 * @version V1.0
 */
public class Res implements Serializable {
    private String name;
    private Integer age;

    public Res() {
    }

    public Res(String name, Integer age) {
        this.name = name;
        this.age = age;
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Res)) return false;
        Res res = (Res) o;
        return Objects.equals(name, res.name) && Objects.equals(age, res.age);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, age);
    }
}
