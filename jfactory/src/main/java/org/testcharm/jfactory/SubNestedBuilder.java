package org.testcharm.jfactory;

import org.testcharm.util.BeanClass;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

abstract class SubNestedBuilder extends SubBuilder {
    protected final boolean queryFirst;
    protected final boolean force;
    protected final TraitsSpec traitsSpec;
    protected final Map<String, Object> subProperties = new LinkedHashMap<>();

    public SubNestedBuilder(String property, TraitsSpec traitsSpec, boolean force) {
        super(property);
        queryFirst = true;
        this.force = force;
        this.traitsSpec = traitsSpec;
    }

    protected Stream<SubBuilder> subBuilders(ObjectFactory<?> factory) {
        return subProperties.entrySet().stream().map(e -> SubBuilder.create(e.getKey(), e.getValue(),
                        BeanClass.cast(this, SubCollectionBuilder.class).orElse(null), factory))
                .collect(Collectors.groupingBy(SubBuilder::property, LinkedHashMap::new, Collectors.toList())).values().stream()
                .map(subBuilders -> subBuilders.stream().reduce(SubBuilder::mergeTo))
                .filter(Optional::isPresent).map(Optional::get);
    }

    public SubNestedBuilder append(String clause, Object value) {
        subProperties.put(clause, value);
        return this;
    }
}
