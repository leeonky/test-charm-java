package org.testcharm.util;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


public class IterableBlockingQueue<T> implements Iterable<T> {
    private static final Object END = new Object();
    private final BlockingQueue<Object> queue;

    public IterableBlockingQueue(BlockingQueue<Object> queue) {
        this.queue = queue;
    }

    public IterableBlockingQueue() {
        this(new LinkedBlockingQueue<>());
    }

    public void put(T item) {
        Sneaky.run(() -> queue.put(Objects.requireNonNull(item)));
    }

    public void close() {
        Sneaky.run(() -> queue.put(END));
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private Object next = null;

            private void fetch() {
                if (next == null)
                    next = Sneaky.get(queue::take);
            }

            @Override
            public boolean hasNext() {
                fetch();
                return next != END;
            }

            @Override
            public T next() {
                fetch();
                if (next == END)
                    throw new NoSuchElementException();
                Object v = next;
                next = null;
                //noinspection unchecked
                return (T) v;
            }
        };
    }
}
