package com.github.leeonky.map;

import com.github.leeonky.map.schemas.User;
import org.junit.jupiter.api.Test;

import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

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
}
