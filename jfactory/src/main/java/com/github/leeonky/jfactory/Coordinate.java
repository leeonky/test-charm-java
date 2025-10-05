package com.github.leeonky.jfactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
        return new D1(index);
    }

    public static class D1 extends Coordinate {
        private D1(Index index) {
            super(Collections.singletonList(index));
        }

        public Index index() {
            return indexes().get(0);
        }
    }
}
