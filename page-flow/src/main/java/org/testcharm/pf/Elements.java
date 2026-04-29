package org.testcharm.pf;

import org.testcharm.dal.extensions.basic.sync.Retryer;
import org.testcharm.dal.runtime.AdaptiveList;
import org.testcharm.dal.runtime.DALCollection;
import org.testcharm.dal.runtime.InvalidAdaptiveListException;

import java.util.function.Predicate;

public interface Elements<T extends Element<T, ?, ?>> extends AdaptiveList<T> {

    default Elements<T> filter(Predicate<T> predicate) {
        return new Elements<T>() {
            @Override
            public int timeout() {
                return Elements.this.timeout();
            }

            @Override
            public DALCollection<T> list() {
                return Elements.this.list().filter(predicate);
            }

            @Override
            public String locateInfo() {
                return Elements.this.locateInfo();
            }
        };
    }

    @Override
    default T single() {
        try {
            return new Retryer(timeout(), 100).get(AdaptiveList.super::single);
        } catch (InvalidAdaptiveListException ig) {
            throw new InvalidAdaptiveListException(String.format("Operations can only be performed on a single located element at:\n  %s\nbut found %d", locateInfo(), ig.list().size()), ig.list());
        }
    }

    int timeout();

    String locateInfo();
}
