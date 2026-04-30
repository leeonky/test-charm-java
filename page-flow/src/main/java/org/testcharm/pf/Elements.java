package org.testcharm.pf;

import org.testcharm.dal.extensions.basic.sync.Retryer;
import org.testcharm.dal.runtime.AdaptiveList;
import org.testcharm.dal.runtime.DALCollection;
import org.testcharm.dal.runtime.InvalidAdaptiveListException;

import java.util.function.Predicate;

public interface Elements<T extends Element<T, ?, ?>> extends AdaptiveList<T> {

    default Elements<T> filter(Predicate<T> predicate) {
        return filter(predicate, "source code");
    }

    default Elements<T> filter(Predicate<T> predicate, String name) {
        return new Elements<T>() {
            @Override
            public int timeout() {
                return Elements.this.timeout();
            }

            @Override
            public DALCollection<T> list() {
                DALCollection<T> list = Elements.this.list();
                DALCollection<T> filtered = list.filter(predicate);
                Element.logger.info(String.format("Filtered from %d to %d elements by %s", list.size(), filtered.size(), name));
                return filtered;
            }

            @Override
            public String locateInfo() {
                return Elements.this.locateInfo();
            }
        };
    }

    default Elements<T> visible() {
        return filter(e -> e.isVisible(), "visible");
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
