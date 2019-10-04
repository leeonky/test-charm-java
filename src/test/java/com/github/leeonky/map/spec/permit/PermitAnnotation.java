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

    @Test
    void get_permit_target_from_declaring_class() {
        Map<String, Object> value = permitMapper.permit(new HashMap<String, Object>() {{
            put("name", "tom");
            put("lastName", "smith");
        }}, User.class, Action.Update.class);

        assertThat(value).hasSize(1);
        assertThat(value.get("lastName")).isEqualTo("smith");
    }

    @Test
    void get_permit_target_from_supper_class() {
        Map<String, Object> value = permitMapper.permit(new HashMap<String, Object>() {{
            put("name", "tom");
            put("lastName", "smith");
        }}, User.class, TargetFromSuper.class);

        assertThat(value).hasSize(2);
        assertThat(value.get("name")).isEqualTo("tom");
        assertThat(value.get("lastName")).isEqualTo("smith");
    }

    static class User {
    }

    @Permit(target = String.class, action = Action.Update.class)
    @PermitTarget(User.class)
    @PermitAction(Action.Create.class)
    public static class UserPermit {
        public String name;

        @PermitAction(Action.Update.class)
        public static class TargetFromDeclaring {
            public String lastName;
        }
    }

    @PermitAction(TargetFromSuper.class)
    public static class TargetFromSuper extends UserPermit {
        public String lastName;
    }
}
