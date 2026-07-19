package org.testcharm.dal.runtime;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.Supplier;

public interface SoloList<T> {
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
            throw new InvalidSoloListException("This operation requires exactly one element, but the list is infinite", list);
        throw new InvalidSoloListException(String.format("This operation requires exactly one element, but found %d", list.size()), list);
    }

    static <T> SoloList<T> soloList(Collection<T> list) {
        return new DefaultSoloList<>(new CollectionDALCollection<>(list));
    }

    static <T> SoloList<T> soloList(Supplier<T> supplier) {
        return new DefaultSoloList<>(new InfiniteDALCollection<>(supplier));
    }
}
