package org.testcharm.dal.runtime;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.Supplier;

public interface AdaptiveList<T> {
    DALCollection<T> list();

    default T single() {
        DALCollection<T> list = list();
        Iterator<IndexedElement<T>> iterator = list.iterator();
        if (iterator.hasNext()) {
            IndexedElement<T> next = iterator.next();
            if (!iterator.hasNext())
                return next.value();
        }
        if (list.infinite())
            throw new InvalidAdaptiveListException("This operation requires exactly one element, but the list is infinite", list);
        throw new InvalidAdaptiveListException(String.format("This operation requires exactly one element, but found %d", list.size()), list);
    }

    static <T> AdaptiveList<T> staticList(Collection<T> list) {
        return new StaticAdaptiveList<>(new CollectionDALCollection<>(list));
    }

    static <T> AdaptiveList<T> staticList(Supplier<T> supplier) {
        return new StaticAdaptiveList<>(new InfiniteDALCollection<>(supplier));
    }
}
