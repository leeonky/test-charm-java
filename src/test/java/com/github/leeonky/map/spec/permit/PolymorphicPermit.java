package com.github.leeonky.map.spec.permit;

import com.github.leeonky.map.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SuppressWarnings("unchecked")
class PolymorphicPermit {
    private PermitMapper permitMapper = new PermitMapper(getClass().getPackage().getName());

    @Test
    void should_support_polymorphic_permit() {
        Map<String, ?> value = permitMapper.permit(new HashMap<String, Object>() {{
            put("id", new HashMap<String, Object>() {{
                put("type", "PASSPORT");
                put("name", "tom");
                put("number", "123");
            }});
        }}, User.class, Create.class);

        assertThat(value.get("id")).isEqualTo(new HashMap<String, Object>() {{
            put("type", "PASSPORT");
            put("name", "tom");
        }});
    }

    @Test
    void should_support_polymorphic_permit_in_list() {
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

        assertThat(value.get("ids")).isEqualTo(asList(new HashMap<String, Object>() {{
            put("type", "PASSPORT");
            put("name", "tom");
        }}, new HashMap<String, Object>() {{
            put("type", "IDENTITY");
            put("number", "123");
        }}));
    }

    @Test
    void should_raise_error_when_no_specify_SubPermitProperty() {
        RuntimeException runtimeException = assertThrows(RuntimeException.class, () -> permitMapper.permit(new HashMap<String, Object>() {{
            put("error1", new HashMap<>());
        }}, InvalidPermit.class, Create.class));

        assertThat(runtimeException).hasMessage("Should specify property name via @PolymorphicPermitIdentity in 'java.lang.Object'");
    }

    @Test
    void should_raise_error_when_list_element_type_is_not_specified() {
        RuntimeException runtimeException = assertThrows(RuntimeException.class, () -> permitMapper.permit(new HashMap<String, Object>() {{
            put("error2", new ArrayList<>());
        }}, InvalidPermit.class, Create.class));

        assertThat(runtimeException).hasMessage("Should specify element type in 'com.github.leeonky.map.spec.permit.PolymorphicPermit$InvalidPermit::error2'");
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

        assertThat(runtimeException).hasMessage("Cannot find permit for type[UNKNOWN] in 'com.github.leeonky.map.spec.permit.PolymorphicPermit$UserPermit::ids'");
    }

    @Test
    void should_support_scope_in_polymorphism_permit() {
        permitMapper.setScope(NewScope.class);
        Map<String, ?> value = permitMapper.permit(new HashMap<String, Object>() {{
            put("ids", singletonList(new HashMap<String, Object>() {{
                put("type", "PASSPORT");
                put("name", "tom");
                put("age", 22);
            }}));
        }}, User.class, Create.class);

        assertThat((List) value.get("ids")).containsOnly(new HashMap<String, Object>() {{
            put("type", "PASSPORT");
            put("age", 22);
        }});
    }

    @PolymorphicPermitIdentity("type")
    public abstract static class IdPermit {
        public String type;
    }

    @Permit(target = Void.class, action = Create.class)
    @PolymorphicPermitIdentityString("PASSPORT")
    public static class PassportPermit extends IdPermit {
        public String name;
    }

    @Permit(target = Void.class, action = Create.class)
    @PolymorphicPermitIdentityString("IDENTITY")
    public static class IdentityPermit extends IdPermit {
        public String number;
    }

    static class User {
    }

    @Permit(target = User.class, action = Create.class)
    public static class UserPermit {

        @PermitAction(Create.class)
        public IdPermit id;

        @PermitAction(Create.class)
        public List<IdPermit> ids;
    }

    @Permit(target = InvalidPermit.class, action = Create.class)
    public static class InvalidPermit {
        @PermitAction(Create.class)
        public Object error1;

        @PermitAction(Create.class)
        public List error2;
    }

    static class NewScope {
    }

    @Permit(target = Void.class, action = Create.class, scope = NewScope.class)
    @PolymorphicPermitIdentityString("PASSPORT")
    public static class NewScopePassportPermit extends IdPermit {
        public int age;
    }
}

