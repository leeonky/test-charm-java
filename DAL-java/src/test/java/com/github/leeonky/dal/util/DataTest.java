package com.github.leeonky.dal.util;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.cucumber.JSONArrayDALCollectionFactory;
import com.github.leeonky.dal.cucumber.JSONObjectAccessor;
import com.github.leeonky.dal.runtime.*;
import com.github.leeonky.dal.runtime.RuntimeContextBuilder.DALRuntimeContext;
import com.github.leeonky.dal.type.FieldAlias;
import com.github.leeonky.dal.type.FieldAliases;
import com.github.leeonky.dal.type.Partial;
import com.github.leeonky.util.BeanClass;
import com.github.leeonky.util.ThrowingSupplier;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class DataTest {

    @Test
    void check_null_for_customer_schema() {
        RuntimeContextBuilder runtimeContextBuilder = new RuntimeContextBuilder().registerPropertyAccessor(AlwaysNull.class, new PropertyAccessor<AlwaysNull>() {
            @Override
            public Object getValue(AlwaysNull instance, Object name) {
                return null;
            }

            @Override
            public Set<Object> getPropertyNames(AlwaysNull instance) {
                return null;
            }

            @Override
            public boolean isNull(AlwaysNull instance) {
                return true;
            }
        });

        assertTrue(runtimeContextBuilder.build(AlwaysNull::new).getThis().resolved().isNull());
        assertTrue(runtimeContextBuilder.build(() -> null).getThis().resolved().isNull());
        assertFalse(runtimeContextBuilder.build(Object::new).getThis().resolved().isNull());
    }

    private static class AlwaysNull {
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Bean {
        private int intValue;
    }

    @Partial
    @FieldAliases({
            @FieldAlias(alias = "aliasOfAge", field = "age"),
    })
    public static class User {
    }

    @Nested
    class GetPropertyOrIndexValue {
        RuntimeContextBuilder runtimeContextBuilder = new DAL().extend().getRuntimeContextBuilder()
                .registerPropertyAccessor(JSONObject.class, new JSONObjectAccessor())
                .registerDALCollectionFactory(JSONArray.class, new JSONArrayDALCollectionFactory());

        @Test
        void access_java_class_property() {
            assertDataAccess(new Bean().setIntValue(1), 1, "intValue");
        }

        @Test
        void access_map_element() {
            assertDataAccess(new HashMap<String, Object>() {{
                put("intValue", 1);
            }}, 1, "intValue");
        }

        @SneakyThrows
        @Test
        void access_via_property_accessor() {
            assertDataAccess(new JSONObject().put("intValue", 1), 1, "intValue");
        }

        @Test
        void get_list_size() {
            assertListSize(emptyList(), 0);
            assertListSize(new String[]{"a"}, 1);
            assertListSize(new JSONArray().put(100).put(200), 2);
        }

        @Test
        void get_list_element_from_array() {
            assertDataAccess(new String[]{"a", "b"}, "b", 1);
        }

        @Test
        void get_list_element_from_iterable() {
            assertDataAccess(asList("a", "b"), "b", 1);
        }

        @Test
        void get_list_element_from_accessor() {
            assertDataAccess(new JSONArray().put("a").put("b"), "b", 1);
        }

        @Test
        void should_raise_error_when_index_out_of_range() {
            assertThrows(DalRuntimeException.class, () ->
                    runtimeContextBuilder.build(new String[0]).getThis().getValue(0).instance());
        }

        @Test
        void support_get_value_via_property_chain() {
            assertDataAccess(new JSONArray().put("a").put(new Bean().setIntValue(100)), 100, 1, "intValue");
        }

        @Test
        void support_invoke_bean_no_args_method() {
            assertDataAccess(new Bean().setIntValue(100), 100, "getIntValue");
        }

        @Test
        void support_stream_size_as_list() {
            assertListSize(Stream.of(1, 2), 2);
        }

        @Test
        void support_get_value_via_field_alias() {
            assertThat(new Data(() -> new HashMap<String, Object>() {{
                put("age", 100);
            }}, runtimeContextBuilder.build(null), SchemaType.create(BeanClass.create(User.class)))
                    .getValue("aliasOfAge").instance()).isEqualTo(100);
        }

        private void assertDataAccess(Object object, Object expected, Object... properties) {
            assertThat(runtimeContextBuilder.build(object).getThis().getValue(asList(properties)).instance())
                    .isEqualTo(expected);
        }

        private void assertListSize(Object object, int size) {
            assertThat(runtimeContextBuilder.build(object).getThis().resolved().list().size()).isEqualTo(size);
        }
    }

    @Nested
    class DumpData {

        @Test
        void dump_null_value() {
            assertThat(new RuntimeContextBuilder().build(() -> null).getThis().dumpValue()).isEqualTo("null");
        }
    }

    @Nested
    class CurringMethodArgs {
        private final DALRuntimeContext context = new RuntimeContextBuilder().build(null);

        @Test
        void return_null_when_property_is_not_string() {
            Data data = data(Object::new);

            assertThat(context.currying(data.instance(), 1)).isEmpty();
        }

        @Test
        void return_currying_method_with_property() {
            Data data = data(Currying::new);

            assertThat(context.currying(data.instance(), "currying1").get().call("hello").resolve()).isEqualTo("hello");
        }

        @Test
        void currying_of_currying() {
            Data data = data(Currying::new);
            CurryingMethod currying = context.currying(data.instance(), "currying2").get();

            assertThat(((CurryingMethod) currying.call(2).resolve()).call("hello").resolve()).isEqualTo("hello2");
        }

        @Test
        void should_choose_min_parameter_size_method() {
            Data data = data(Currying::new);
            CurryingMethod currying = context.currying(data.instance(), "overrideMethod").get();

            assertThat(currying.call(2).resolve()).isEqualTo(2);
        }

        private Data data(ThrowingSupplier<Object> supplier) {
            return new RuntimeContextBuilder().build(supplier::get).getThis();
        }
    }

    @Nested
    class StaticCurringMethodArgs {
        private final DALRuntimeContext context = new RuntimeContextBuilder()
                .registerStaticMethodExtension(StaticMethod.class).build(new Currying());

        @Test
        void return_currying_method_with_property() {
            assertThat(context.currying(context.getThis().resolved().value(), "staticCurrying1").get().call("hello").resolve()).isEqualTo("hello");
        }

        @Test
        void return_currying_method_with_property_in_super_instance_type() {
            assertThat(context.currying(context.getThis().resolved().value(), "baseMatchCurrying").get().call("hello").resolve()).isEqualTo("hello");
        }

        @Test
        void currying_of_currying() {
            CurryingMethod currying = context.currying(context.getThis().resolved().value(), "staticCurrying2").get();

            assertThat(((CurryingMethod) currying.call(2).resolve()).call("hello").resolve()).isEqualTo("hello2");
        }

        @Test
        void should_choose_min_parameter_size_method() {
            CurryingMethod currying = context.currying(context.getThis().resolved().value(), "staticOverrideMethod").get();

            assertThat(currying.call(2).resolve()).isEqualTo(2);
        }

        @Test
        void use_same_instance_type_first_when_more_than_one_candidate() {
            CurryingMethod currying = context.currying(context.getThis().resolved().value(), "baseCurrying").get();

            assertThat(currying.call("a").resolve()).isEqualTo("A");
        }
    }

    @Nested
    class Lazy {
        private final DALRuntimeContext context = new RuntimeContextBuilder().build(null);

        private int i = 0;

        @Test
        void should_resolved_value_when_get_value() {
//            f()
//              return result
//            v = f()
//            print v
            Data data = data(() -> i += 1);

            assertThat(i).isEqualTo(0);
            assertThat(data.instance()).isEqualTo(1);
            assertThat(i).isEqualTo(1);
        }

        @Test
        void should_resolve_only_once_when_get_value_multiple_times() {
//            f()
//              return result
//            v = f()
//            print v
//            print v

            Data data = data(() -> i += 1);
            assertThat(data.instance()).isEqualTo(1);
            assertThat(data.instance()).isEqualTo(1);
        }

        @Test
        void should_throw_error_when_resolve_error() {
//            f()
//              throw e

//            v = f() //throw
//            print v

            Exception e = new Exception();
            Data data = errorData(e);

            assertThat(assertThrows(Exception.class, data::instance))
                    .isSameAs(e);
        }

        @Test
        void map_error_when_resolve_error() {
//            f()
//              throw e
//            try
//              v=f()
//            catch e
//              throw mapError(e)
//            print v
            Exception e = new Exception();
            Data data = errorData(e);
            data.onError(RuntimeException::new);

            assertThat(assertThrows(Exception.class, data::instance))
                    .isInstanceOf(RuntimeException.class)
                    .hasCause(e);
        }

        @Test
        void nested_map_error_when_resolve_error() {
//            f()
//              throw e
//            try
//              try
//                v=f()
//              catch e
//                throw mapError(e)
//              print1 v
//            catch e
//              throw mapError2(e)
//            print2 v
            Exception e = new Exception();
            Data data = errorData(e);
            data.onError(throwable -> new RuntimeException("1", throwable))
                    .onError(throwable1 -> new RuntimeException("2", throwable1));

            Exception actual = assertThrows(Exception.class, data::instance);
            assertThat(actual).isInstanceOf(RuntimeException.class).hasMessage("2");
            assertThat(actual.getCause()).isInstanceOf(RuntimeException.class).hasMessage("1");
            assertThat(actual.getCause().getCause()).isSameAs(e);
        }

        private Data errorData(Exception e) {
            return new Data(() -> {
                throw e;
            }, context, SchemaType.create(null));
        }

        @Test
        void should_only_map_once_when_resolve_error() {
//            f()
//              throw e
//            try
//              v=f()
//            catch e
//              throw mapError(e)
//            print v
//            print v

            Exception e = new Exception();
            Data data = errorData(e);
            data.onError(throwable -> {
                i++;
                return new RuntimeException(throwable);
            });

            assertThat(assertThrows(Exception.class, data::instance))
                    .isInstanceOf(RuntimeException.class)
                    .hasCause(e);

            assertThat(assertThrows(Exception.class, data::instance))
                    .isInstanceOf(RuntimeException.class)
                    .hasCause(e);

            assertThat(i).isEqualTo(1);
        }

        @Test
        void peek_before_resolve() {
//            f()
//              return result
//            v = f()
//            process(v)
//            print v
            Data data = data(() -> new StringBuilder().append(i += 1));

            data.peek(resolved -> ((StringBuilder) resolved.value()).append("-peek"));

            assertThat(data.instance().toString()).isEqualTo("1-peek");
        }

        private Data data(ThrowingSupplier<Object> supplier) {
            return new RuntimeContextBuilder().build(supplier::get).getThis();
        }

        @Test
        void peek_twice_before_resolve() {
//            f()
//              return result
//            v = f()
//            process1(v)
//            process2(v)
//            print v
            Data data = data(() -> new StringBuilder().append(i += 1));

            data.peek(resolved -> ((StringBuilder) resolved.value()).append("-peek1"));
            data.peek(resolved -> ((StringBuilder) resolved.value()).append("-peek2"));

            assertThat(data.instance().toString()).isEqualTo("1-peek1-peek2");
        }

        @Test
        void should_call_handler_at_once_when_instance_has_already_resolved() {
//            f()
//              return result
//            v = f()
//            process1(v)
//            print v
//            process2(v)
//            print v
            Data data = data(() -> new StringBuilder().append(i += 1));

            data.peek(resolved -> assertThat(resolved.value().toString()).isEqualTo("1"));
            Object instance = data.instance();
            assertThat(instance.toString()).isEqualTo("1");

            data.peek(resolved -> ((StringBuilder) resolved.value()).append("-peek1"));

            assertThat(instance.toString()).isEqualTo("1-peek1");
        }
    }

    public static class BaseCurrying {

    }

    public static class Currying extends BaseCurrying {
        public Object unexpected(String str) {
            return null;
        }

        public Object currying1(String str) {
            return str;
        }

        public Object currying2(int i, String str) {
            return str + i;
        }

        public Object overrideMethod(int i, String str) {
            return str + i;
        }

        public Object overrideMethod(int i) {
            return i;
        }

        public Object invalidCurrying(String str) {
            return null;
        }

        public Object invalidCurrying(int str) {
            return null;
        }
    }

    public static class StaticMethod {
        public static Object staticCurrying1(Currying currying, String str) {
            return str;
        }

        public static Object staticCurrying2(Currying currying, int i, String str) {
            return str + i;
        }

        public static Object staticOverrideMethod(Currying currying, int i, String str) {
            return str + i;
        }

        public static Object staticOverrideMethod(Currying currying, int i) {
            return i;
        }

        public static Object baseCurrying(Currying currying, String str) {
            return str.toUpperCase();
        }

        public static Object baseCurrying(BaseCurrying currying, String str) {
            return str;
        }

        public static Object invalidStaticCurrying(Currying currying, String str) {
            return null;
        }

        public static Object invalidStaticCurrying(Currying currying, int str) {
            return null;
        }

        public static Object baseMatchCurrying(BaseCurrying currying, String str) {
            return str;
        }
    }
}
