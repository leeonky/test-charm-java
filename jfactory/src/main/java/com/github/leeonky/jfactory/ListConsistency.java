package com.github.leeonky.jfactory;

public interface ListConsistency<T> {
    ListConsistency<T> direct(String property);

    <P> AbstractConsistency.LC1<T, P> property(String property);
}
