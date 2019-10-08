package com.github.leeonky.map.spec.permit;

import com.github.leeonky.map.*;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class PermitAnnotation {
    private PermitMapper permitMapper = new PermitMapper(getClass().getPackage().getName());
    private HashMap<String, Object> inputMap = new HashMap<String, Object>() {{
        put("f1", 1);
        put("f2", 2);
        put("f3", 3);
        put("f4", 4);
    }};

    public interface Scope1 {
    }

    public interface Scope2 {
    }

    public interface Scope3 {
    }

    public interface Scope4 {
    }

    public static class Entity1 {
    }

    public static class Entity2 {
    }

    public static class Entity3 {
    }

    @Permit(target = String.class, action = Action.Update.class)
    @PermitTarget(Entity1.class)
    @PermitAction(Action.Create.class)
    public static class PermitTarget1 {
        public int f1;
    }

    @PermitTarget(Entity2.class)
    public static class PermitTarget2 {
        @PermitAction(Action.Create.class)
        public static class PermitTarget3 {
            public int f2;
        }
    }

    @PermitTarget(Entity3.class)
    public static class PermitTarget4 {
    }

    @PermitAction(Action.Update.class)
    public static class PermitTarget5 extends PermitTarget4 {
        public int f3;
    }

    @Permit(target = Entity1.class, action = Action.Update.class, scope = String.class)
    @PermitScope(Scope1.class)
    public static class PermitScope1 {
        public int f1;
    }

    @Permit(target = Entity1.class, action = Action.Update.class, scope = Scope2.class)
    public static class PermitScope2 {
        public int f2;
    }

    @PermitScope(Scope3.class)
    public static class PermitScope3 {

        @Permit(target = Entity1.class, action = Action.Update.class)
        public static class PermitScope4 {
            public int f3;
        }
    }

    @PermitScope(Scope4.class)
    public static class PermitScope5 {
    }

    @Permit(target = Entity1.class, action = Action.Update.class)
    public static class PermitScope6 extends PermitScope5 {
        public int f4;
    }

    @Nested
    class GuessPermitTarget {

        @Test
        void permit_target_and_permit_action_has_higher_priority() {
            Map<String, Object> value = permitMapper.permit(inputMap, Entity1.class, Action.Create.class);

            assertThat(value).hasSize(1);
            assertThat(value.get("f1")).isEqualTo(1);
        }

        @Test
        void get_permit_target_from_declaring_class() {
            Map<String, Object> value = permitMapper.permit(inputMap, Entity2.class, Action.Create.class);

            assertThat(value).hasSize(1);
            assertThat(value.get("f2")).isEqualTo(2);
        }

        @Test
        void get_permit_target_from_supper_class() {
            Map<String, Object> value = permitMapper.permit(inputMap, Entity3.class, Action.Update.class);

            assertThat(value).hasSize(1);
            assertThat(value.get("f3")).isEqualTo(3);
        }
    }

    @Nested
    class GuessPermitScope {

        @Test
        void permit_scope_has_high_priority() {
            permitMapper.setScope(Scope1.class);
            Map<String, Object> value = permitMapper.permit(inputMap, Entity1.class, Action.Update.class);

            assertThat(value).hasSize(1);
            assertThat(value.get("f1")).isEqualTo(1);
        }

        @Test
        void get_permit_scope_from_permit() {
            permitMapper.setScope(Scope2.class);
            Map<String, Object> value = permitMapper.permit(inputMap, Entity1.class, Action.Update.class);

            assertThat(value).hasSize(1);
            assertThat(value.get("f2")).isEqualTo(2);
        }

        @Test
        void get_permit_scope_from_declaring_class() {
            permitMapper.setScope(Scope3.class);
            Map<String, Object> value = permitMapper.permit(inputMap, Entity1.class, Action.Update.class);

            assertThat(value).hasSize(1);
            assertThat(value.get("f3")).isEqualTo(3);
        }

        @Test
        void get_permit_scope_from_super_class() {
            permitMapper.setScope(Scope4.class);
            Map<String, Object> value = permitMapper.permit(inputMap, Entity1.class, Action.Update.class);

            assertThat(value).hasSize(1);
            assertThat(value.get("f4")).isEqualTo(4);
        }
    }
}
