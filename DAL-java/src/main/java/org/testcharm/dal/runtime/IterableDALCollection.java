package org.testcharm.dal.runtime;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.StreamSupport;

public class IterableDALCollection<E> extends DALCollectionBase<E> {
    private Iterator<E> iterator;
    private final Iterable<E> iterable;
    private final List<E> cached = new ArrayList<>();

    public IterableDALCollection(Iterable<E> iterable) {
        this.iterable = iterable;
    }

    @Override
    public Iterator<IndexedElement<E>> iterator() {
        cached.clear();
        iterator = null;
        return new Iterator<IndexedElement<E>>() {
            private int index = firstIndex();
            private int position = 0;

            @Override
            public boolean hasNext() {
                if (position < cached.size()) {
                    return true;
                }
                return getIterator().hasNext();
            }

            @Override
            public IndexedElement<E> next() {
                if (position < cached.size())
                    return new IndexedElement<>(index++, cached.get(position++));
                position++;
                return new IndexedElement<>(index++, getNext());
            }
        };
    }

    private E getNext() {
        E next = getIterator().next();
        cached.add(next);
        return next;
    }

    @Override
    protected E getByPosition(int position) {
        if (position < cached.size())
            return cached.get(position);
        while (getIterator().hasNext()) {
            getNext();
            if (position < cached.size())
                return cached.get(position);
        }
        throw new IndexOutOfBoundsException();
    }

    private Iterator<E> getIterator() {
        return iterator == null ? (iterator = iterable.iterator()) : iterator;
    }

    @Override
    public int size() {
        return (int) StreamSupport.stream(
                requireLimitedCollection("Not supported for infinite collection").spliterator(), false).count();
    }
}
