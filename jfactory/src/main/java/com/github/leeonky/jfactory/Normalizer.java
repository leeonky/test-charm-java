package com.github.leeonky.jfactory;

import java.util.stream.Collectors;

public interface Normalizer<C> {
    C align(Coordinate coordinate);

    Coordinate deAlign(C coordinate);

    static Normalizer<Coordinate> reverse() {
        return new Normalizer<Coordinate>() {
            @Override
            public Coordinate align(Coordinate coordinate) {
                return coordinate.reverse();
            }

            @Override
            public Coordinate deAlign(Coordinate coordinate) {
                return coordinate.reverse();
            }
        };
    }

    static Normalizer<Coordinate> adjust(int adjust) {
        return new Normalizer<Coordinate>() {
            @Override
            public Coordinate align(Coordinate coordinate) {
                return new Coordinate(coordinate.indexes().stream().map(i -> i.adjust(adjust)).collect(Collectors.toList()));
            }

            @Override
            public Coordinate deAlign(Coordinate coordinate) {
                return new Coordinate(coordinate.indexes().stream().map(i -> i.adjust(-adjust)).collect(Collectors.toList()));
            }
        };
    }
}
