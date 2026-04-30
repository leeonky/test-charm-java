package org.testcharm.pf;

import org.testcharm.dal.extensions.basic.sync.Retryer;
import org.testcharm.dal.runtime.AdaptiveList;
import org.testcharm.dal.runtime.DALCollection;
import org.testcharm.dal.runtime.InvalidAdaptiveListException;
import org.testcharm.util.IndentBuffer;

import java.util.function.Predicate;

import static java.lang.System.identityHashCode;

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
                String objectId = Integer.toHexString(identityHashCode(this)).toUpperCase();
                Element.logger.info(String.format("Filtering by %s(@%s)", name, objectId));
                DALCollection<T> list = Elements.this.list();
                DALCollection<T> filtered = list.filter(predicate);
                Element.logger.info(String.format("Filtered from %d to %d elements by %s(@%s)", list.size(),
                        filtered.size(), name, objectId));
                return filtered;
            }

            @Override
            public IndentBuffer locateInfo(IndentBuffer indentBuffer) {
                String objectId = Integer.toHexString(identityHashCode(this)).toUpperCase();
                indentBuffer.append("Filtering by ").append(name).append(String.format("(@%s)", objectId));
                return Elements.this.locateInfo(indentBuffer.indent().newLine());
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
            IndentBuffer buffer = IndentBuffer.create()
                    .append("Operations can only be performed on a single located element at:");
            locateInfo(buffer.indent().newLine());
            buffer.newLine().append("but found ").append(ig.list().size());
            throw new InvalidAdaptiveListException(buffer.content(), ig.list());
        }
    }

    int timeout();

    IndentBuffer locateInfo(IndentBuffer indentBuffer);
}
