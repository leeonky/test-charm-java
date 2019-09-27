package com.github.leeonky.map.spec.permit;

import com.github.leeonky.map.*;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class PermitAnnotation {
    private PermitMapper permitMapper = new PermitMapper(getClass().getPackage().getName());

    @Test
    void permit_target_and_permit_action_has_higher_priority() {

        Map<String, Object> value = permitMapper.permit(new HashMap<String, Object>() {{
            put("name", "tom");
            put("age", 1);
        }}, User.class, Action.Create.class);

        assertThat(value).hasSize(1);
        assertThat(value.get("name")).isEqualTo("tom");
    }

    static class User {
    }

    @Permit(target = String.class, action = Action.Update.class)
    @PermitTarget(User.class)
    @PermitAction(Action.Create.class)
    public static class UserPermit {
        public String name;
    }
}
