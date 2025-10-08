package com.github.leeonky.jfactory;

import com.github.leeonky.util.BeanClass;

import java.util.function.Function;

public interface ListStructure<T, C extends Coordinate> {

    D1<T, C> list(String property);

    D2<T, C> list(String property1, String property2);

    ListStructure<T, C> normalize(Normalizer<C> normalizer);

    class D1<T, C extends Coordinate> extends DecoratedListStructure<T, C> {

        public D1(DefaultListStructure<T, C> delegate) {
            super(delegate);
        }

        public D1<T, C> normalize(Function<Coordinate.D1, C> aligner,
                                  Function<C, Coordinate.D1> inverseAligner) {
            delegate.normalize(c -> aligner.apply(c.convertTo(BeanClass.create(Coordinate.D1.class))),
                    inverseAligner::apply);
            return this;
        }
    }

    class D2<T, C extends Coordinate> extends DecoratedListStructure<T, C> {

        public D2(DefaultListStructure<T, C> delegate) {
            super(delegate);
        }

        public D2<T, C> normalize(Function<Coordinate.D2, C> aligner,
                                  Function<C, Coordinate.D2> inverseAligner) {
            delegate.normalize(c -> aligner.apply(c.convertTo(BeanClass.create(Coordinate.D2.class))),
                    inverseAligner::apply);
            return this;
        }
    }
}
