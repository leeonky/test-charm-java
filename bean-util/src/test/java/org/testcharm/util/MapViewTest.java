package org.testcharm.util;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.testcharm.dal.Assertions.expect;
import static org.testcharm.util.MapView.mapView;
import static org.testcharm.util.MapView.string;

class MapViewTest {

    private enum ExecutionStatus {
        PASSED, FAILED
    }

    @Nested
    class MapInterface {

        @Test
        void is_empty() {
            assertTrue(new MapView().isEmpty());
            assertTrue(mapView().isEmpty());
            assertTrue(new MapView(new HashMap<>()).isEmpty());
            assertFalse(new MapView(new HashMap<String, Object>() {{
                put("any-key", new Object());
            }}).isEmpty());
        }

        @Test
        void size() {
            assertEquals(0, new MapView(new HashMap<>()).size());
            assertEquals(1, new MapView(new HashMap<String, Object>() {{
                put("any-key", new Object());
            }}).size());
        }

        @Test
        void contains_key() {
            assertTrue(new MapView(new HashMap<String, Object>() {{
                put("key", new Object());
            }}).containsKey("key"));

            assertFalse(new MapView(new HashMap<String, Object>() {{
                put("key", new Object());
            }}).containsKey("not-exist-key"));
        }

        @Test
        void contains_value() {
            assertTrue(new MapView(new HashMap<String, Object>() {{
                put("key", "value");
            }}).containsValue("value"));

            assertFalse(new MapView(new HashMap<String, Object>() {{
                put("key", "value");
            }}).containsValue("not-exist-value"));
        }

        @Test
        void get_by_key() {
            assertEquals("value", new MapView(new HashMap<String, Object>() {{
                put("key", "value");
            }}).get((Object) "key"));

            assertEquals("value", new MapView(new HashMap<String, Object>() {{
                put("key", "value");
            }}).get("key"));

            assertNull(new MapView(new HashMap<String, Object>() {{
                put("key", "value");
            }}).get("not-exist-key"));
        }

        @Test
        void entry_set() {
            Map<String, Object> source = new LinkedHashMap<>();
            source.put("key1", "value1");
            source.put("key2", 123);
            MapView mapView = new MapView(source);

            assertThat(mapView.entrySet()).isSameAs(source.entrySet());
        }

        @Test
        void key_set() {
            Map<String, Object> source = new LinkedHashMap<>();
            source.put("key1", "value1");
            source.put("key2", 123);
            MapView mapView = new MapView(source);

            assertThat(mapView.keySet()).isSameAs(source.keySet());
        }

        @Test
        void value_set() {
            Map<String, Object> source = new LinkedHashMap<>();
            source.put("key1", "value1");
            source.put("key2", 123);
            MapView mapView = new MapView(source);

            assertThat(mapView.values()).isSameAs(source.values());
        }

        @Test
        void put_value() {
            Map<String, Object> source = new LinkedHashMap<>();
            MapView mapView = new MapView(source);

            Object previousValue = mapView.put("key", "value");

            assertNull(previousValue);
            assertEquals("value", mapView.get("key"));

            assertThat(source).containsEntry("key", "value");
        }

        @Test
        void put_to_exist() {
            Map<String, Object> source = new LinkedHashMap<>();
            source.put("key", "oldValue");
            MapView mapView = new MapView(source);

            Object previousValue = mapView.put("key", "newValue");

            assertEquals("oldValue", previousValue);
            assertEquals("newValue", mapView.get("key"));

            assertThat(source).containsEntry("key", "newValue");
        }

        @Test
        void put_all() {
            Map<String, Object> source = new LinkedHashMap<>();
            MapView mapView = new MapView(source);

            Map<String, Object> newValues = new LinkedHashMap<>();
            newValues.put("key1", "value1");
            newValues.put("key2", 123);

            mapView.putAll(newValues);

            assertEquals("value1", mapView.get("key1"));
            assertEquals(123, (int) mapView.get("key2"));

            assertThat(source).containsAllEntriesOf(newValues);
        }

        @Test
        void remove() {
            Map<String, Object> source = new LinkedHashMap<>();
            source.put("key", "value");
            MapView mapView = new MapView(source);

            Object removedValue = mapView.remove("key");

            assertEquals("value", removedValue);
            assertNull(mapView.get("key"));

            assertThat(source).doesNotContainKey("key");
        }

        @Test
        void clear() {
            Map<String, Object> source = new LinkedHashMap<>();
            source.put("key1", "value1");
            source.put("key2", 123);
            MapView mapView = new MapView(source);

            mapView.clear();

            assertTrue(mapView.isEmpty());
            assertThat(source).isEmpty();
        }
    }

    @Nested
    class MapViewSpecificApi {

        @Nested
        class GetAndMap {

            @Test
            void return_null_when_key_is_absent() {
                MapView mapView = mapView();
                assertNull(mapView.get("non-existent-key", Object::toString));
            }

            @Test
            void return_and_map() {
                MapView mapView = mapView();
                mapView.put("key", 100);
                assertEquals("100", mapView.get("key", Object::toString));
            }
        }

        @Nested
        class Get {

            Object getValue(MapView map) {
                return map.get("key");
            }

            @Test
            void return_null_when_key_is_absent() {
                MapView mapView = mapView();
                assertNull(mapView.get(this::getValue));
            }

            @Test
            void return_value() {
                MapView mapView = mapView();
                mapView.put("key", 100);
                assertEquals(100, mapView.get(this::getValue));
            }
        }

        @Nested
        class Set {

            @Test
            void set_entry() {
                MapView mapView = mapView()
                        .set("key", "value");

                expect(mapView).should("= {key: value}");
            }

            @Test
            void set_modify_original_map() {
                Map<String, Object> source = new LinkedHashMap<>();
                MapView mapView = new MapView(source)
                        .set("key", "value");

                expect(source).should("= {key: value}");
            }
        }
    }

    @Nested
    class Functions {

        @Nested
        class CastString {

            @Test
            void return_null_when_no_value() {
                MapView mapView = mapView();
                assertNull(mapView.get("key", string()));
            }

            @Test
            void return_string_value() {
                MapView mapView = mapView();
                mapView.put("key", "world");
                assertEquals("world", mapView.get("key", string()));
            }

            @Test
            void raise_error_when_not_a_string() {
                MapView mapView = mapView();
                mapView.put("key", 100);
                assertThrows(ClassCastException.class, () -> mapView.get("key", string()));
            }
        }

        @Nested
        class ToLong {

            @Test
            void return_null_when_no_value() {
                MapView mapView = mapView();
                assertNull(mapView.get("key", MapView.toLong()));
            }

            @Test
            void return_long_value() {
                MapView mapView = mapView();
                mapView.put("key", 100L);
                assertEquals(100L, (long) mapView.get("key", MapView.toLong()));
            }

            @Test
            void return_long_from_number() {
                MapView mapView = mapView();
                mapView.put("key", 100);
                assertEquals(100L, (long) mapView.get("key", MapView.toLong()));
            }

            @Test
            void raise_error_when_not_a_number() {
                MapView mapView = mapView();
                mapView.put("key", "not a number");
                assertThrows(ClassCastException.class, () -> mapView.get("key", MapView.toLong()));
            }
        }

        @Nested
        class ToInt {

            @Test
            void return_null_when_no_value() {
                MapView mapView = mapView();
                assertNull(mapView.get("key", MapView.toInt()));
            }

            @Test
            void return_int_value() {
                MapView mapView = mapView();
                mapView.put("key", 100);
                assertEquals(100, (int) mapView.get("key", MapView.toInt()));
            }

            @Test
            void return_int_from_number() {
                MapView mapView = mapView();
                mapView.put("key", 100L);
                assertEquals(100, (int) mapView.get("key", MapView.toInt()));
            }

            @Test
            void raise_error_when_not_a_number() {
                MapView mapView = mapView();
                mapView.put("key", "not a number");
                assertThrows(ClassCastException.class, () -> mapView.get("key", MapView.toInt()));
            }
        }

        @Nested
        class EnumOf {

            @Test
            void return_null_when_no_value() {
                MapView mapView = mapView();
                assertNull(mapView.get("key", MapView.enumOf(ExecutionStatus.class)));
            }

            @Test
            void return_enum_value_from_string() {
                MapView mapView = mapView();
                mapView.put("key", "PASSED");
                assertEquals(ExecutionStatus.PASSED, mapView.get("key", MapView.enumOf(ExecutionStatus.class)));
            }

            @Test
            void raise_error_when_not_a_valid_enum_constant() {
                MapView mapView = mapView();
                mapView.put("key", "NOT_A_STATUS");
                assertThrows(IllegalArgumentException.class, () -> mapView.get("key", MapView.enumOf(ExecutionStatus.class)));
            }
        }
    }

    @Nested
    class AsList {

        @Test
        void return_null_when_no_value() {
            MapView mapView = mapView();
            assertNull(mapView.get("key", MapView.list(MapView.string())));
        }

        @Test
        void return_string_list() {
            MapView mapView = mapView().set("key", asList("hello", "world"));
            List<String> stringList = mapView.get("key", MapView.list(MapView.string()));

            expect(stringList).should("= [hello, world]");
        }
    }

    @Nested
    class IndexedList {

        @Test
        void return_null_when_no_value() {
            MapView mapView = mapView();
            assertNull(mapView.get("key", MapView.indexedList(index -> MapView.string())));
        }

        @Test
        void return_string_list_with_index() {
            MapView mapView = mapView().set("key", asList("hello", "world"));
            List<String> indexedList = mapView.get("key", MapView.indexedList(index -> value -> value + "_" + index));

            expect(indexedList).should("= [hello_0, world_1]");
        }
    }

    @Nested
    class SubMap {

        @Test
        void return_null_when_no_value() {
            MapView mapView = mapView();
            assertNull(mapView.get("key", MapView.map()));
        }

        String subKey(MapView subMap) {
            return subMap.get("subKey", string());
        }

        @Test
        void return_from_sub_map_view() {
            MapView mapView = mapView().set("key", new HashMap<String, Object>() {{
                put("subKey", "subValue");
            }});

            String subValue = mapView.get("key", MapView.map().andThen(this::subKey));

            expect(subValue).should("= subValue");
        }

        @Test
        void return_from_list_map() {
            MapView mapView = mapView().set("key", asList(new HashMap<String, Object>() {{
                put("subKey", "hello");
            }}, new HashMap<String, Object>() {{
                put("subKey", "world");
            }}));

            List<String> strings = mapView.get("key", MapView.list(MapView.map().andThen(this::subKey)));

            expect(strings).should("= [hello world]");
        }
    }

    @Nested
    class Composite {
        String firstName(MapView map) {
            return map.get("firstName", string());
        }

        String lastName(MapView map) {
            return map.get("lastName", string());
        }

        @Test
        void combine_multiple_readers_to_build_a_summary() {
            MapView mapView = mapView()
                    .set("firstName", "John")
                    .set("lastName", "Doe");

            expect(mapView.get(fullName())).isEqualTo("John Doe");
        }

        @Test
        void return_null_if_any_of_the_combine_reader_return_null() {
            assertNull(mapView()
                    .set("lastName", "Doe").get(fullName()));

            assertNull(mapView()
                    .set("firstName", "John").get(fullName()));

            assertNull(mapView().get(fullName()));
        }

        private Function<MapView, String> fullName() {
            return MapView.composite(this::firstName, this::lastName, (firstName, lastName) -> firstName + " " + lastName);
        }
    }
}
