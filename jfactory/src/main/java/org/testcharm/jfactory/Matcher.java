package org.testcharm.jfactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Matcher<T> {
    private final Collection<PropertyNode> expressions;

    Matcher(List<PropertyNode> expressions) {
        this.expressions = new ArrayList<>(expressions);
    }

    public boolean matches(T object, ObjectFactory<T> objectFactory) {
        return expressions.stream().allMatch(e -> e.matches(object, objectFactory));
    }
}
