package com.github.leeonky.jfactory;

import java.util.*;
import java.util.function.Predicate;

public class CompositeDataRepository implements DataRepository {

    private final DataRepository defaultRepo;

    private final List<Map.Entry<Predicate<Class<?>>, DataRepository>> subRepos = new ArrayList<>();

    public CompositeDataRepository(DataRepository defaultRepo) {
        this.defaultRepo = defaultRepo;
    }

    public CompositeDataRepository() {
        this.defaultRepo = null;
    }

    @Override
    public <T> Collection<T> queryAll(Class<T> type) {
        return guessRepo(type).queryAll(type);
    }

    @Override
    public void clear() {
        if (defaultRepo != null)
            defaultRepo.clear();
    }

    @Override
    public void save(Object object) {
        if (object == null)
            return;
        guessRepo(object.getClass()).save(object);
    }

    private DataRepository guessRepo(Class<?> type) {
        DataRepository dataRepository = subRepos.stream().filter(subRepo -> subRepo.getKey().test(type))
                .findFirst().map(Map.Entry::getValue)
                .orElse(defaultRepo);
        if (dataRepository == null)
            throw new IllegalStateException(String.format("There is no default or appropriate repository available for type `%s`", type.getName()));
        return dataRepository;
    }

    public CompositeDataRepository registerByType(Class<?> type, DataRepository repo) {
        return registerBy(type::equals, repo);
    }

    public CompositeDataRepository registerByPackage(String packageName, DataRepository repo) {
        return registerBy(type -> type.getPackage().getName().equals(packageName), repo);
    }

    public CompositeDataRepository registerBy(Predicate<Class<?>> predicate, DataRepository repo) {
        subRepos.add(0, new AbstractMap.SimpleEntry<>(predicate, repo));
        return this;
    }
}
