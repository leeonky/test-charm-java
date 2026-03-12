package org.testcharm.jfactory;

import org.testcharm.util.BeanClass;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

abstract class CompositeBuilder extends PropertyNode {
    protected final boolean queryFirst;
    protected final boolean force;
    protected final TraitsSpec traitsSpec;
    protected final Map<String, Object> subProperties = new LinkedHashMap<>();

    public CompositeBuilder(String property, TraitsSpec traitsSpec, boolean force) {
        super(property);
        queryFirst = true;
        this.force = force;
        this.traitsSpec = traitsSpec;
    }

    protected Stream<PropertyNode> createSubNodes(ObjectFactory<?> factory) {
        return subProperties.entrySet().stream().map(e -> PropertyNode.create(e.getKey(), e.getValue(),
                        BeanClass.cast(this, CollectionNode.class).orElse(null), factory))
                .collect(Collectors.groupingBy(PropertyNode::property, LinkedHashMap::new, Collectors.toList())).values().stream()
                .map(grouped -> grouped.stream().reduce(PropertyNode::mergeTo))
                .filter(Optional::isPresent).map(Optional::get);
    }

    public CompositeBuilder append(String clause, Object value) {
        subProperties.put(clause, value);
        return this;
    }
}
