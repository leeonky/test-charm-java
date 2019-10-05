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

    @Test
    void permit_scope_has_high_priority() {
        permitMapper.setScope(Frontend.class);
        Map<String, Object> value = permitMapper.permit(new HashMap<String, Object>() {{
            put("name", "tom");
            put("lastName", "smith");
        }}, User.class, Action.Update.class);

        assertThat(value).hasSize(1);
        assertThat(value.get("name")).isEqualTo("tom");
    }

    @Test
    void get_permit_scope_from_permit() {
        permitMapper.setScope(UpdateUser.class);
        Map<String, Object> value = permitMapper.permit(new HashMap<String, Object>() {{
            put("name", "tom");
            put("age", 10);
        }}, User.class, Action.Update.class);

        assertThat(value).hasSize(1);
        assertThat(value.get("age")).isEqualTo(10);
    }

    @Test
    void get_permit_scope_from_declaring_class() {
        permitMapper.setScope(Frontend.class);
        Map<String, Object> value = permitMapper.permit(new HashMap<String, Object>() {{
            put("name", "tom");
            put("age", 10);
        }}, User.class, Action.Create.class);

        assertThat(value).hasSize(1);
        assertThat(value.get("age")).isEqualTo(10);
    }

    @Test
    void get_permit_scope_from_super_class() {
        permitMapper.setScope(Frontend.class);
        Map<String, Object> value = permitMapper.permit(new HashMap<String, Object>() {{
            put("name", "tom");
            put("age", 10);
        }}, User.class, UserPermit2.class);

        assertThat(value).hasSize(1);
        assertThat(value.get("age")).isEqualTo(10);
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

    @Permit(target = User.class, action = Action.Update.class, scope = Void.class)
    @PermitScope(Frontend.class)
    public static class FrontendUserPermit {
        public String name;
    }

    @Permit(target = User.class, action = Action.Update.class, scope = UpdateUser.class)
    public static class UpdateUser {
        public int age;
    }

    @PermitScope(Frontend.class)
    public static class Frontend {
        @PermitTarget(User.class)
        @PermitAction(Action.Create.class)
        public static class UserPermit {
            public int age;
        }
    }

    @PermitTarget(User.class)
    @PermitAction(UserPermit2.class)
    public static class UserPermit2 extends Frontend {
        public int age;
    }
}
