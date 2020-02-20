package com.github.leeonky.map.spec.permit;

import com.github.leeonky.map.*;
import org.junit.jupiter.api.Test;

import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("unchecked")
public class PermitAndWrap {

    private PermitMapper permitMapper = new PermitMapper(getClass().getPackage().getName());

    @Test
    void should_support_rename_property() {
        Map<String, ?> value = permitMapper.permit(new HashMap<String, Object>() {{
            put("nickName", "tom");
        }}, User.class, Action.Create.class);

        assertThat(value).isInstanceOf(LinkedHashMap.class);
        assertThat(value).containsOnly(new SimpleEntry("name", "tom"));
    }

    @Test
    void should_support_map_property_to_new_map() {
        Map<String, ?> value = permitMapper.permit(new HashMap<String, Object>() {{
            put("identityId", "001");
        }}, User.class, Action.Create.class);

        assertThat(value).isInstanceOf(LinkedHashMap.class);
        assertThat(value).containsOnly(new SimpleEntry("identity", new HashMap<String, Object>() {{
            put("id", "001");
        }}));
    }

    @Test
    void should_support_map_list_property_to_list_map() {
        Map<String, ?> value = permitMapper.permit(new HashMap<String, Object>() {{
            put("members", asList("001"));
            put("leaders", asList("001"));
        }}, User.class, Action.Create.class);

        assertThat(value).isInstanceOf(LinkedHashMap.class);

        assertThat(value).contains(new SimpleEntry("members", asList(new HashMap<String, Object>() {{
            put("id", "001");
        }}))).contains(new SimpleEntry("boss", asList(new HashMap<String, Object>() {{
            put("id", "001");
        }})));
    }

    @Test
    void should_support_transform_field() {
        Map<String, ?> value = permitMapper.permit(new HashMap<String, Object>() {{
            put("gender", "female");
        }}, User.class, Action.Create.class);

        assertThat(value).containsOnly(new SimpleEntry("gender", "FEMALE"));
    }

    public static class User {
    }

    @Permit(target = User.class, action = Action.Create.class)
    public static class UserPermit {

        @ToProperty("name")
        public String nickName;

        @ToProperty("identity.id")
        public String identityId;

        @ToProperty("{id}")
        public List<String> members;

        @ToProperty("boss{id}")
        public List<String> leaders;

        @Transform(ToUpper.class)
        public String gender;
    }

    public static class ToUpper implements Transformer<String> {
        @Override
        public String transform(String object) {
            return object != null ? object.toUpperCase() : null;
        }
    }
}
