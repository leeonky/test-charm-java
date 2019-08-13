package com.github.leeonky.map.spec.permit;

import com.github.leeonky.map.Create;
import com.github.leeonky.map.Permit;
import com.github.leeonky.map.PermitMapper;
import com.github.leeonky.map.ToProperty;
import org.junit.jupiter.api.Test;

import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("unchecked")
public class PermitAndWrap {

    private PermitMapper permitMapper = new PermitMapper(getClass().getPackage().getName());

    @Test
    void should_support_rename_property() {
        Map<String, ?> value = permitMapper.permit(new HashMap<String, Object>() {{
            put("nickName", "tom");
        }}, User.class, Create.class);

        assertThat(value).isInstanceOf(LinkedHashMap.class);
        assertThat(value).containsOnly(new SimpleEntry("name", "tom"));
    }

    @Test
    void should_support_map_property_to_new_map() {
        Map<String, ?> value = permitMapper.permit(new HashMap<String, Object>() {{
            put("identityId", "001");
        }}, User.class, Create.class);

        assertThat(value).isInstanceOf(LinkedHashMap.class);
        assertThat(value).containsOnly(new SimpleEntry("identity", new HashMap<String, Object>() {{
            put("id", "001");
        }}));
    }

    public static class User {
    }

    @Permit(target = User.class, action = Create.class)
    public static class UserPermit {

        @ToProperty("name")
        public String nickName;

        @ToProperty("identity.id")
        public String identityId;
    }
}
