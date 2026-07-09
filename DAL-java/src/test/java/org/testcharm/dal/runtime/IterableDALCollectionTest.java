package org.testcharm.dal.runtime;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.testcharm.dal.Assertions.expect;
import static org.testcharm.dal.Assertions.expectRun;

class IterableDALCollectionTest {

    private int start = 0;


    IterableDALCollection<Integer> collection = new IterableDALCollection<>(() -> new java.util.Iterator<Integer>() {
        @Override
        public boolean hasNext() {
            return true;
        }

        @Override
        public Integer next() {
            return start++;
        }
    });

    @Test
    void do_not_cache_elements_when_iterating() {
        List<IndexedElement<Integer>> first10 = first(collection, 3);
        List<IndexedElement<Integer>> second10 = first(collection, 3);

        expect(first10).should("index[]: [0 1 2]");
        expect(first10).should("value[]: [0 1 2]");
        expect(second10).should("index[]: [0 1 2]");
        expect(second10).should("value[]: [3 4 5]");
    }

    @Test
    void cache_elements_when_accessing_by_position_after_iterator_called() {
        List<IndexedElement<Integer>> first10 = first(collection, 3);

        expect(first10).should("index[]: [0 1 2]");
        expect(first10).should("value[]: [0 1 2]");

        expect(collection).should("getByIndex: {0: 0, 1: 1, 2: 2}");
    }

    @Test
    void get_by_index() {
        expect(collection).should("getByIndex: {0: 0, 3: 3}");
    }

    @Test
    void index_out_of_bounds() {
        expectRun(() -> new IterableDALCollection<>(() -> new Iterator<Object>() {
            @Override
            public boolean hasNext() {
                return false;
            }

            @Override
            public Object next() {
                return null;
            }
        }).getByIndex(1)).should("::throw.class.simpleName= IndexOutOfBoundsException");
    }


    private <E> List<IndexedElement<E>> first(IterableDALCollection<E> iterableDALCollection, int size) {
        int count = 0;
        List<IndexedElement<E>> result = new ArrayList<>();
        for (IndexedElement<E> element : iterableDALCollection) {
            result.add(element);
            if (++count >= size)
                break;
        }
        return result;
    }
}