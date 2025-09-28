package com.github.leeonky.jfactory;

public interface Consistency<T> {
    Consistency<T> direct(String property);

    <P> AbstractConsistency.C1<T, P> property(String property);

    <P1, P2> AbstractConsistency.C2<T, P1, P2> properties(String property1, String property2);

    <P1, P2, P3> AbstractConsistency.C3<T, P1, P2, P3> properties(String property1, String property2, String property3);

    AbstractConsistency.CN<T> properties(String... properties);

    ListConsistency<T> list(String property);
}
