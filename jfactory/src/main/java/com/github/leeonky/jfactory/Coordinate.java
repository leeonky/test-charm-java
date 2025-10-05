package com.github.leeonky.jfactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Coordinate {
    final List<Index> indexes = new ArrayList<>();

    public Coordinate(List<Index> indexes) {
        this.indexes.addAll(indexes);
    }

    public Coordinate reverse() {
        return new Coordinate(indexes.stream().map(Index::reverse).collect(Collectors.toList()));
    }
}
