package com.github.leeonky.map.spec.permit;

import com.github.leeonky.map.Action;
import com.github.leeonky.map.Permit;
import com.github.leeonky.map.PermitMapper;
import org.junit.jupiter.api.Test;

import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertSame;

@SuppressWarnings("unchecked")
class BasicPermit {
    private PermitMapper permitMapper = new PermitMapper(getClass().getPackage().getName());

    @Test
    void empty_may_should_return_empty_linked_hash_map() {
        Map<String, ?> value = permitMapper.permit(new HashMap<>(), User.class, Action.Create.class);

        assertThat(value).isInstanceOf(LinkedHashMap.class);
        assertThat(value).isEmpty();
    }

    @Test
    void should_ignore_fields_not_exist_in_schema() {
        Map<String, ?> value = permitMapper.permit(new HashMap<String, Object>() {{
            put("name", "tom");
            put("age", 22);
        }}, User.class, Action.Create.class);

        assertThat(value).isInstanceOf(LinkedHashMap.class);
        assertThat(value).containsOnly(new SimpleEntry("name", "tom"));
    }

    @Test
    void should_support_type_convert() {
        Map<String, ?> value = permitMapper.permit(new HashMap<String, Object>() {{
            put("name", 100);
        }}, User.class, Action.Create.class);

        assertThat(value).isInstanceOf(LinkedHashMap.class);
        assertThat(value).containsOnly(new SimpleEntry("name", "100"));
    }

    @Test
    void should_support_scoped_permit() {
        permitMapper.setScope(NewScope.class);
        Map<String, ?> value = permitMapper.permit(new HashMap<String, Object>() {{
            put("name", "tom");
            put("age", 22);
        }}, User.class, Action.Create.class);

        assertThat(value).isInstanceOf(LinkedHashMap.class);
        assertThat(value).containsOnly(new SimpleEntry("age", 22));
    }

    @Test
    void should_support_permit_in_list() {
        List value = permitMapper.permit(singletonList(new HashMap<String, Object>() {{
            put("name", "tom");
        }}), User.class, Action.Create.class);

        assertThat((Map) value.get(0)).containsOnly(new SimpleEntry("name", "tom"));
    }

    @Test
    void should_return_original_when_no_available_permit() {
        HashMap<String, Object> map = new HashMap<String, Object>() {{
            put("Hello", "world");
        }};

        assertSame(permitMapper.permit(map, NotExistTarget.class, Action.Create.class), map);
    }

    static class User {
    }

    @Permit(target = User.class, action = Action.Create.class)
    public static class UserPermit {
        public String name;
    }

    static class NewScope {
    }

    private static class NotExistTarget {
    }

    @Permit(target = User.class, action = Action.Create.class, scope = NewScope.class)
    public class NewScopeUserPermit {
        public int age;
    }
}
