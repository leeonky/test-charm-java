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

    public Index adjust(int i) {
        return new Index(size, (index + i) % size);
    }
}
