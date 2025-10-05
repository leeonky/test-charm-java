package com.github.leeonky.jfactory;

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
}
