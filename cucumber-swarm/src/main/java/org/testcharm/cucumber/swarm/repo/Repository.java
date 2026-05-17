package org.testcharm.cucumber.swarm.repo;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class Repository<K, E> {
    private final Map<K, E> map = new ConcurrentHashMap<>();
    private final Function<E, K> idFunction;

    public Repository(Function<E, K> id) {
        idFunction = id;
    }

    public E save(E instance) {
        map.put(idFunction.apply(instance), instance);
        return instance;
    }

    public Collection<E> findAll() {
        return map.values();
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public E findByKey(K id) {
        E e = map.get(id);
        if (e == null)
//            TODO test
            throw new RuntimeException("No instance found for id: " + id);
        return e;
    }

    public Optional<E> findBy(Function<E, Boolean> predicate) {
        return map.values().stream().filter(predicate::apply).findFirst();
    }
}