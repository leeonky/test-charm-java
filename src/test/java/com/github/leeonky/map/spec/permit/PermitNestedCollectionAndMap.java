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

@SuppressWarnings("unchecked")
class PermitNestedCollectionAndMap {

    private PermitMapper permitMapper = new PermitMapper(getClass().getPackage().getName());

    @Test
    void should_support_permit_nested_list() {
        Map<String, ?> value = permitMapper.permit(new HashMap<String, Object>() {{
            put("users", singletonList(new HashMap<String, Object>() {{
                put("name", "tom");
                put("age", 22);
            }}));
        }}, UserList.class, Action.Create.class);
        assertThat(value.get("users")).isInstanceOf(List.class);
        assertThat((List) value.get("users")).containsOnly(new HashMap<String, String>() {{
            put("name", "tom");
        }});
    }

    @Test
    void should_support_permit_nested_list_list() {
        Map<String, ?> value = permitMapper.permit(new HashMap<String, Object>() {{
            put("usersList", singletonList(singletonList(new HashMap<String, Object>() {{
                put("name", "tom");
                put("age", 22);
            }})));
        }}, UserList.class, Action.Create.class);
        assertThat(value.get("usersList")).isInstanceOf(List.class);
        assertThat((List) value.get("usersList")).containsOnly(singletonList(new HashMap<String, String>() {{
            put("name", "tom");
        }}));
    }

    @Test
    void should_support_permit_nested_map() {
        Map<String, ?> value = permitMapper.permit(new HashMap<String, Object>() {{
            put("product", new HashMap<String, Object>() {{
                put("seller", new HashMap<String, Object>() {{
                    put("name", "tom");
                    put("age", 22);
                }});
            }});
        }}, Order.class, Action.Create.class);

        assertThat(value).isInstanceOf(LinkedHashMap.class);
        assertThat(value).containsOnly(new SimpleEntry("product", new HashMap<String, Object>() {{
            put("seller", new HashMap<String, Object>() {{
                put("name", "tom");
            }});
        }}));
    }

    static class UserList {
    }

    static class UserPermit {
        public String name;
    }

    @Permit(target = UserList.class, action = Action.Create.class)
    static class UserListPermit {
        public List<UserPermit> users;
        public List<List<UserPermit>> usersList;
    }

    static class ProductPermit {
        public UserPermit seller;
    }

    static class Order {
    }

    @Permit(target = Order.class, action = Action.Create.class)
    static class OrderPermit {
        public ProductPermit product;
    }
}
