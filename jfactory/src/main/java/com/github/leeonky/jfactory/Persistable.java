package com.github.leeonky.jfactory;

public interface Persistable {
    default void save(Object object) {
    }
}
