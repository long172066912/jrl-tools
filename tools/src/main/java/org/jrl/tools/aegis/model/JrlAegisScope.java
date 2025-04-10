package org.jrl.tools.aegis.model;

import java.util.Objects;

/**
* 作用域
* @author JerryLong
*/
public class JrlAegisScope {
    private int priority;
    private String scope;

    public static final JrlAegisScope GLOBAL = new JrlAegisScope(0, "GLOBAL");

    public JrlAegisScope() {
    }

    public JrlAegisScope(int priority, String scope) {
        this.priority = priority;
        this.scope = scope;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JrlAegisScope)) return false;
        JrlAegisScope that = (JrlAegisScope) o;
        return Objects.equals(scope, that.scope);
    }

    @Override
    public int hashCode() {
        return Objects.hash(priority, scope);
    }

    @Override
    public String toString() {
        return "Scope[" +
                "priority=" + priority +
                ", scope='" + scope + '\'' +
                ']';
    }
}
