package com.github.leeonky.jfactory;

import java.util.ArrayList;
import java.util.List;

public class Coordinate {
    final List<Index> index = new ArrayList<>();

    public Coordinate(List<Index> indexes) {
        index.addAll(indexes);
    }
}
