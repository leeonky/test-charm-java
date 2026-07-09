package org.testcharm.dal.runtime;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.testcharm.dal.extensions.MapPropertyAccessor;

import java.util.Collection;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.*;
import static org.testcharm.dal.Assertions.expect;

class DALCollectionTest {

    @Nested
    class Filter {

        @Nested
        class CollectionList {

            @Test
            void filter() {
                CollectionDALCollection<Integer> collection = new CollectionDALCollection<>(asList(1, 2, 3));

                expect(collection.filter(i -> i == 2)).should("values= [2]");
            }

            @Test
            void should_has_same_first_index() {
                CollectionDALCollection<Integer> collection = new CollectionDALCollection<Integer>(asList(1, 2, 3)) {
                    @Override
                    public int firstIndex() {
                        return 1;
                    }
                };

                expect(collection.filter(i -> i == 2)).should(": { values= [2] firstIndex: 1}");
            }
        }

        @Nested
        class IterableList {

            @Nested
            class NonInfiniteList {

                @Test
                void filter() {
                    IterableDALCollection<Integer> collection = new IterableDALCollection<>(asList(1, 2, 3));

                    expect(collection.filter(i -> i == 2)).should("values= [2]");
                }

                @Test
                void should_has_same_first_index() {
                    IterableDALCollection<Integer> collection = new IterableDALCollection<Integer>(asList(1, 2, 3)) {
                        @Override
                        public int firstIndex() {
                            return 1;
                        }
                    };

                    expect(collection.filter(i -> i == 2)).should(": { values= [2] firstIndex: 1}");
                }
            }

            @Nested
            class InfiniteList {
                int seed = 0;

                @Test
                void filter() {
                    InfiniteDALCollection<Integer> collection = new InfiniteDALCollection<>(() -> seed++);

                    expect(collection.filter(i -> i < 2)).should(": [0 1 ...]");
                }

                @Test
                void should_has_same_first_index() {
                    InfiniteDALCollection<Integer> collection = new InfiniteDALCollection<Integer>(() -> seed++) {
                        @Override
                        public int firstIndex() {
                            return 1;
                        }
                    };

                    expect(collection.filter(i -> i < 2)).should(": { values= [0 1 ...] firstIndex: 1}");
                }

                @Test
                void always_not_empty() {
                    InfiniteDALCollection<Integer> collection = new InfiniteDALCollection<>(() -> 0);

                    assertFalse(collection.isEmpty());
                }
            }
        }

        @Nested
        class DataListList {

            @Test
            void filter() {
                RuntimeContextBuilder builder = new RuntimeContextBuilder()
                        .registerPropertyAccessor(Map.class, new MapPropertyAccessor())
                        .registerDALCollectionFactory(Collection.class, CollectionDALCollection::new);
                RuntimeContextBuilder.DALRuntimeContext context = builder.build(asList(1, 2, 3));

                Data.DataList collection = context.getThis().list();

                expect(collection.filter(i -> (int) i == 2)).should("values= [2]");
            }

            @Test
            void should_has_same_first_index() {
                RuntimeContextBuilder builder = new RuntimeContextBuilder()
                        .registerPropertyAccessor(Map.class, new MapPropertyAccessor())
                        .registerDALCollectionFactory(Collection.class, collection -> new CollectionDALCollection<Object>(collection) {
                            @Override
                            public int firstIndex() {
                                return 1;
                            }
                        });
                RuntimeContextBuilder.DALRuntimeContext context = builder.build(asList(1, 2, 3));

                Data.DataList collection = context.getThis().list();

                expect(collection.filter(i -> (int) i == 2)).should(": { values= [2] firstIndex: 1}");
            }
        }
    }

    @Nested
    class Decorated {
        private final DALCollection<String> collection = mock(DALCollection.class);
        private final DALCollection.Decorated<String> decorated = new DALCollection.Decorated<>(collection);

        @Test
        void size() {
            decorated.size();

            verify(collection).size();
        }

        @Test
        void getByIndex() {
            when(collection.getByIndex(100)).thenReturn("test");

            assertEquals("test", decorated.getByIndex(100));

            verify(collection).getByIndex(100);
        }

        @Test
        void iterator() {
            decorated.iterator();

            verify(collection).iterator();
        }

        @Test
        void firstIndex() {
            when(collection.firstIndex()).thenReturn(5);

            assertEquals(5, decorated.firstIndex());
        }

        @Test
        void collect() {
            when(collection.collect()).thenReturn(asList("a", "b", "c"));

            assertEquals(asList("a", "b", "c"), decorated.collect());
        }

        @Test
        void filter() {
            DALCollection filter = mock(DALCollection.class);
            when(collection.filter(any())).thenReturn(filter);

            Predicate p = mock(Predicate.class);

            assertEquals(filter, decorated.filter(p));

            verify(collection).filter(p);
        }

        @Test
        void infinite() {
            when(collection.infinite()).thenReturn(true);

            assertEquals(true, decorated.infinite());
        }

        @Test
        void not_infinite() {
            when(collection.infinite()).thenReturn(false);

            assertEquals(false, decorated.infinite());
        }

        @Test
        void limit() {
            DALCollection limit = mock(DALCollection.class);
            when(collection.limit(10)).thenReturn(limit);

            assertEquals(limit, decorated.limit(10));

            verify(collection).limit(10);
        }

        @Test
        void values() {
            Stream<String> stream = mock(Stream.class);
            when(collection.values()).thenReturn(stream);

            assertEquals(stream, decorated.values());
        }

        @Test
        void indexes() {
            Stream<Integer> stream = mock(Stream.class);
            when(collection.indexes()).thenReturn(stream);

            assertEquals(stream, decorated.indexes());
        }

        @Test
        void map() {
            DALCollection map = mock(DALCollection.class);
            IndexedElement.Mapper<String, String> mapper = mock(IndexedElement.Mapper.class);
            when(collection.map(mapper)).thenReturn(map);

            assertEquals(map, decorated.map(mapper));

            verify(collection).map(mapper);
        }

        @Test
        void stream() {
            Stream<IndexedElement<String>> stream = mock(Stream.class);
            when(collection.stream()).thenReturn(stream);

            assertEquals(stream, decorated.stream());
        }

        @Test
        void requireLimitedCollection() {
            assertEquals(decorated, decorated.requireLimitedCollection("message"));

            verify(collection).requireLimitedCollection("message");
        }

        @Test
        void is_empty() {
            when(collection.isEmpty()).thenReturn(true);

            assertEquals(true, decorated.isEmpty());
        }

        @Test
        void not_empty() {
            when(collection.isEmpty()).thenReturn(false);

            assertEquals(false, decorated.isEmpty());
        }
    }
}