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

    static Normalizer<Coordinate> shift(int adjust) {
        return new Normalizer<Coordinate>() {
            @Override
            public Coordinate align(Coordinate coordinate) {
                return coordinate.shift(adjust);
            }

            @Override
            public Coordinate deAlign(Coordinate coordinate) {
                return coordinate.shift(-adjust);
            }
        };
    }

    static Normalizer<Coordinate> sample(int period, int offset) {
        return new Normalizer<Coordinate>() {
            @Override
            public Coordinate align(Coordinate coordinate) {
                return coordinate.sample(period, offset);
            }

            @Override
            public Coordinate deAlign(Coordinate coordinate) {
                return coordinate.interpolate(period, offset);
            }
        };
    }

    static Normalizer<Coordinate> transpose() {
        return new Normalizer<Coordinate>() {
            @Override
            public Coordinate align(Coordinate coordinate) {
                return coordinate.transpose();
            }

            @Override
            public Coordinate deAlign(Coordinate coordinate) {
                return coordinate.transpose();
            }
        };
    }
}
