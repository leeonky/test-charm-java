package com.github.leeonky.jfactory;

public interface ListConsistency<T> {
    ListConsistency<T> direct(String property);

    <P> AbstractConsistency.LC1<T, P> property(String property);

    <P1, P2> AbstractConsistency.LC2<T, P1, P2> properties(String property1, String property2);
}
