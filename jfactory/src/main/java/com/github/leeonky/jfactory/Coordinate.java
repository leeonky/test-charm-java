package com.github.leeonky.jfactory;

import com.github.leeonky.util.BeanClass;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

public class Coordinate {
    private final List<Index> indexes = new ArrayList<>();

    public Coordinate(List<Index> indexes) {
        this.indexes.addAll(indexes);
    }

    public List<Index> indexes() {
        return indexes;
    }

    public <C extends Coordinate> C convertTo(BeanClass<C> type) {
        return type.newInstance(indexes());
    }

    public Coordinate reverse() {
        return new Coordinate(indexes.stream().map(Index::reverse).collect(Collectors.toList()));
    }

    public Coordinate shift(int adjust) {
        return new Coordinate(indexes().stream().map(i -> i.shift(adjust)).collect(Collectors.toList()));
    }

    public Coordinate sample(int period, int offset) {
        List<Index> indexes = indexes().stream().map(i -> i.sample(period, offset)).collect(Collectors.toList());
        return indexes.contains(null) ? null : new Coordinate(indexes);
    }

    public Coordinate interpolate(int period, int offset) {
        return new Coordinate(indexes().stream().map(i -> i.interpolate(period, offset)).collect(Collectors.toList()));
    }

    public Coordinate transpose() {
        List<Index> indexes = new ArrayList<>(indexes());
        Collections.reverse(indexes);
        return new Coordinate(indexes);
    }

    public static class D1 extends Coordinate {
        public D1(List<Index> index) {
            super(require(index, 1));
        }

        public Index index() {
            return indexes().get(0);
        }
    }

    public static class D2 extends Coordinate {
        public D2(List<Index> index) {
            super(require(index, 2));
        }

        public Index index0() {
            return indexes().get(0);
        }

        public Index index1() {
            return indexes().get(1);
        }
    }

    public static D1 d1(Index index) {
        return new D1(singletonList(index));
    }

    public static D2 d2(Index index0, Index index1) {
        return new D2(asList(index0, index1));
    }

    private static List<Index> require(List<Index> indexes, int size) {
        if (indexes.size() != size)
            throw new IllegalArgumentException("Coordinate size not match");
        return indexes;
    }
}
