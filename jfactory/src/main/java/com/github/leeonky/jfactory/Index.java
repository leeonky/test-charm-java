package com.github.leeonky.jfactory;

public class Index {
    private final int index, size;

    public Index(int size, int index) {
        this.index = index;
        this.size = size;
    }

    public Index reverse() {
        return new Index(size, size - 1 - index);
    }

    public int index() {
        return index;
    }

    public Index shift(int i) {
        return new Index(size, (index + i) % size);
    }

    public Index sample(int period, int offset) {
        return index % period == offset ? new Index(size / period, index / period) : null;
    }

    public Index interpolate(int period, int offset) {
        return new Index(size * period, index * period + offset);
    }

    @Override
    public String toString() {
        return String.format("%d(%d)", index, size);
    }
}
