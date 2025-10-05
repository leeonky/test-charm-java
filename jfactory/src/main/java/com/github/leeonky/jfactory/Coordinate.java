package com.github.leeonky.jfactory;

import com.github.leeonky.util.BeanClass;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;

public class Coordinate {
    private final List<Index> indexes = new ArrayList<>();

    public Coordinate(List<Index> indexes) {
        this.indexes.addAll(indexes);
    }

    public Coordinate reverse() {
        return new Coordinate(indexes.stream().map(Index::reverse).collect(Collectors.toList()));
    }

    public List<Index> indexes() {
        return indexes;
    }

    public static D1 d1(Index index) {
        return new D1(singletonList(index));
    }

    public <C extends Coordinate> C convertTo(BeanClass<C> type) {
        return type.newInstance(indexes());
    }

    private static List<Index> require(List<Index> indexes, int size) {
        if (indexes.size() != size)
            throw new IllegalArgumentException("Coordinate size not match");
        return indexes;
    }

    public static class D1 extends Coordinate {
        public D1(List<Index> index) {
            super(require(index, 1));
        }

        public Index index() {
            return indexes().get(0);
        }
    }
}
