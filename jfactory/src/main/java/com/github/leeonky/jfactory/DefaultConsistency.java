package com.github.leeonky.jfactory;

import com.github.leeonky.util.BeanClass;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.github.leeonky.util.function.Extension.not;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;

public class DefaultConsistency<T> implements Consistency<T> {
    private static final List<Class<?>> TYPE_PRIORITY = asList(
            FixedValueProducer.class,
            ReadOnlyProducer.class,
            DependencyProducer.class,
            UnFixedValueProducer.class
    );

    private final List<ConsistencyItem<T>> items = new ArrayList<>();
    private final BeanClass<T> type;

    public DefaultConsistency(Class<T> type) {
        this.type = BeanClass.create(type);
    }

    @Override
    public BeanClass<T> type() {
        return type;
    }

    @Override
    public Consistency<T> link(ConsistencyItem<T> item) {
        items.add(item);
        return this;
    }

    @Override
    public void apply(Producer<?> producer) {
        List<ConsistencyItem<T>.Resolving> resolvingList = items.stream().map(i -> i.resolving(producer)).collect(toList());
        guessDependency(resolvingList).ifPresent(dependency -> resolvingList.stream().filter(not(dependency::equals))
                .forEach(dependent -> dependent.resolve(dependency)));
    }

    private Optional<ConsistencyItem<T>.Resolving> guessDependency(List<ConsistencyItem<T>.Resolving> resolvingList) {
        resolvingList = resolvingList.stream().filter(ConsistencyItem.Resolving::hasComposer).collect(toList());
        for (Class<?> type : TYPE_PRIORITY)
            for (ConsistencyItem<T>.Resolving resolving : resolvingList)
                if (resolving.isProducerType(type))
                    return of(resolving);
        return resolvingList.stream().findFirst();
    }

    public boolean merge(DefaultConsistency<?> another) {
        if (items.stream().anyMatch(item -> another.items.stream().anyMatch(item::sameProperty))) {
            for (ConsistencyItem item : another.items)
                items.add(item);
            return true;
        }
        return false;
    }

    public List<ConsistencyItem<T>> items() {
        return Collections.unmodifiableList(items);
    }

    public boolean dependsOn(DefaultConsistency<?> another) {
        List<ConsistencyItem<?>> dependencyPair = dependencyPair(another);
        if (dependencyPair.isEmpty())
            return false;
        List<ConsistencyItem<?>> reversePair = another.dependencyPair(this);
        if (!reversePair.isEmpty())
            throw new ConflictConsistencyException(format("Conflict dependency between consistencies:\n%s\n%s",
                    dependencyPair.get(0).toTable(dependencyPair.get(1), "  "),
                    reversePair.get(0).toTable(reversePair.get(1), "  ")));
        return true;
    }

    private List<ConsistencyItem<?>> dependencyPair(DefaultConsistency<?> another) {
        for (ConsistencyItem<?> item : items)
            for (ConsistencyItem<?> anotherItem : another.items)
                if (item.dependsOn(anotherItem))
                    return asList(item, anotherItem);
        return Collections.emptyList();
    }
}
