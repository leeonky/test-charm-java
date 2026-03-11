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

    public SubNestedBuilder(String property, boolean queryFirst, boolean force, TraitsSpec traitsSpec) {
        super(property);
        this.queryFirst = queryFirst;
        this.force = force;
        this.traitsSpec = traitsSpec;
    }

    protected Stream<SubBuilder> subBuilders(ObjectFactory<?> factory) {
        return subProperties.entrySet().stream().map(e -> SubBuilder.create(e.getKey(), e.getValue(),
                        BeanClass.cast(this, SubCollectionBuilder.class).orElse(null), queryFirst, factory))
                .collect(Collectors.groupingBy(SubBuilder::property, LinkedHashMap::new, Collectors.toList())).values().stream()
                .map(subBuilders -> subBuilders.stream().reduce(SubBuilder::mergeTo))
                .filter(Optional::isPresent).map(Optional::get);
    }
}
