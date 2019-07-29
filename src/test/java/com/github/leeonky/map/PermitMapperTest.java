package com.github.leeonky.map;

import com.github.leeonky.map.schemas.User;
import org.junit.jupiter.api.Test;

import java.util.AbstractMap.SimpleEntry;
import java.util.*;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SuppressWarnings("unchecked")
class PermitMapperTest {
    private PermitMapper permitMapper = new PermitMapper("com.github.leeonky.map.schemas");

    @Test
    void empty_may_should_return_empty() {
        Map<String, ?> value = permitMapper.permit(new HashMap<>(), User.class, Create.class);

        assertThat(value).isInstanceOf(LinkedHashMap.class);
        assertThat(value).isEmpty();
    }

    @Test
    void should_ignore_fields_not_exist_in_schema() {
        Map<String, ?> value = permitMapper.permit(new HashMap<String, Object>() {{
            put("name", "tom");
            put("age", 22);
        }}, User.class, Create.class);

        assertThat(value).isInstanceOf(LinkedHashMap.class);
        assertThat(value).containsOnly(new SimpleEntry("name", "tom"));
    }

    @Test
    void should_support_type_convert() {
        Map<String, ?> value = permitMapper.permit(new HashMap<String, Object>() {{
            put("name", 100);
        }}, User.class, Create.class);

        assertThat(value).isInstanceOf(LinkedHashMap.class);
        assertThat(value).containsOnly(new SimpleEntry("name", "100"));
    }

    @Test
    void should_support_permit_list() {
        List value = permitMapper.permit(singletonList(new HashMap<String, Object>() {{
            put("name", 100);
        }}), User.class, Create.class);

        assertThat((Map) value.get(0)).containsOnly(new SimpleEntry("name", "100"));
    }

    @Test
    void should_support_permit_nested_map() {
        Map<String, ?> value = permitMapper.permit(new HashMap<String, Object>() {{
            put("address", new HashMap<String, Object>() {{
                put("zipCode", "1000");
                put("country", "USA");
            }});
        }}, User.class, Create.class);

        assertThat(value).isInstanceOf(LinkedHashMap.class);
        assertThat(value).containsOnly(new SimpleEntry("address", new HashMap<String, Object>() {{
            put("zipCode", "1000");
        }}));
    }

    @Test
    void should_support_permit_nested_list() {
        Map<String, ?> value = permitMapper.permit(new HashMap<String, Object>() {{
            put("partners", singletonList(new HashMap<String, Object>() {{
                put("name", "tom");
                put("age", 22);
            }}));
        }}, User.class, Create.class);
        assertThat(value.get("partners")).isInstanceOf(List.class);
        assertThat((List) value.get("partners")).containsOnly(new HashMap<String, String>() {{
            put("name", "tom");
        }});
    }

    @Test
    void should_support_permit_nested_list_list() {
        Map<String, ?> value = permitMapper.permit(new HashMap<String, Object>() {{
            put("neighbors", singletonList(singletonList(new HashMap<String, Object>() {{
                put("name", "tom");
                put("age", 22);
            }})));
        }}, User.class, Create.class);
        assertThat(value.get("neighbors")).isInstanceOf(List.class);
        assertThat((List) value.get("neighbors")).containsOnly(singletonList(new HashMap<String, String>() {{
            put("name", "tom");
        }}));
    }

    @Test
    void should_support_permit_sub_class() {
        Map<String, ?> value = permitMapper.permit(new HashMap<String, Object>() {{
            put("ids", asList(new HashMap<String, Object>() {{
                put("type", "PASSPORT");
                put("name", "tom");
                put("number", "123");
            }}, new HashMap<String, Object>() {{
                put("type", "IDENTITY");
                put("name", "tom");
                put("number", "123");
            }}));
        }}, User.class, Create.class);

        assertThat((List) value.get("ids")).containsOnly(new HashMap<String, Object>() {{
            put("type", "PASSPORT");
            put("name", "tom");
        }}, new HashMap<String, Object>() {{
            put("type", "IDENTITY");
            put("number", "123");
        }});
    }

    @Test
    void should_raise_error_when_no_specify_SubPermitProperty() {
        RuntimeException runtimeException = assertThrows(RuntimeException.class, () -> permitMapper.permit(new HashMap<String, Object>() {{
            put("error1", new HashMap<>());
        }}, User.class, Create.class));

        assertThat(runtimeException).hasMessage("Should specify property name via @SubPermitProperty in 'java.lang.Object'");
    }

    @Test
    void should_return_original_when_no_available_permit() {
        HashMap<String, Object> map = new HashMap<String, Object>() {{
            put("Hello", "world");
        }};
        assertSame(permitMapper.permit(map, NotExistTarget.class, Create.class), map);
    }

    @Test
    void should_raise_error_when_list_element_type_is_not_specified() {
        RuntimeException runtimeException = assertThrows(RuntimeException.class, () -> permitMapper.permit(new HashMap<String, Object>() {{
            put("error2", new ArrayList<>());
        }}, User.class, Create.class));

        assertThat(runtimeException).hasMessage("Should specify element type in 'com.github.leeonky.map.schemas.UserPermit::error2'");
    }

    @Test
    void should_raise_error_when_no_sub_permit() {
        RuntimeException runtimeException = assertThrows(RuntimeException.class, () -> permitMapper.permit(new HashMap<String, Object>() {{
            put("ids", singletonList(new HashMap<String, Object>() {{
                put("type", "UNKNOWN");
                put("name", "tom");
                put("number", "123");
            }}));
        }}, User.class, Create.class));

        assertThat(runtimeException).hasMessage("Cannot find permit for type[UNKNOWN] in 'com.github.leeonky.map.schemas.UserPermit::ids'");
    }

    private static class NotExistTarget {
    }
}
