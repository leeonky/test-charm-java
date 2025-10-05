package com.github.leeonky.jfactory;

public interface Normalizer {
    Coordinate align(Coordinate coordinate);

    Coordinate deAlign(Coordinate coordinate);

    static Normalizer reverse() {
        return new Normalizer() {
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
