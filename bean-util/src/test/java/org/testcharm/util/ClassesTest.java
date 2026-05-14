package org.testcharm.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.testcharm.util.JavaExecutor.executor;

class ClassesTest {

    @BeforeEach
    void reset() {
        executor().reset();
    }


    @Nested
    class ReflectiveTypes {

        @Test
        void public_top_level_class_is_reflective() {
            givenClass(String.join("\n",
                    "public class Public {",
                    "}"
            ));
            assertTrue(Classes.isReflective(typeOf("Public")));
        }

        @Test
        void public_top_level_interface_is_reflective() {
            givenClass(String.join("\n",
                    "public interface Public {",
                    "}"
            ));
            assertTrue(Classes.isReflective(typeOf("Public")));
        }

        @Test
        void public_static_nested_class_in_public_class_is_reflective() {
            givenClass(String.join("\n",
                    "public class Public {",
                    "    public static class StaticInner {",
                    "    }",
                    "}"
            ));
            assertTrue(Classes.isReflective(typeOf("Public.StaticInner")));
        }

        @Test
        void public_inner_class_in_public_class_is_reflective() {
            givenClass(String.join("\n",
                    "public class Public {",
                    "    public class Inner {",
                    "    }",
                    "}"
            ));
            assertTrue(Classes.isReflective(typeOf("Public.Inner")));
        }

        @Test
        void public_class_deeply_nested_in_public_classes_is_reflective() {
            givenClass(String.join("\n",
                    "public class Public {",
                    "    public static class StaticInner {",
                    "        public static class DeepInner {",
                    "        }",
                    "    }",
                    "}"
            ));
            assertTrue(Classes.isReflective(typeOf("Public.StaticInner.DeepInner")));
        }

        @Test
        void package_private_top_level_class_is_not_reflective() {
            givenClass(String.join("\n",
                    "class PackagePrivate {",
                    "}"
            ));
            givenClass(String.join("\n",
                    "public class Holder {",
                    "    public static Class<?> packagePrivateClass() { return PackagePrivate.class; }",
                    "}"
            ));
            assertFalse(Classes.isReflective((Class<?>) valueOf("Holder.packagePrivateClass()")));
        }

        @Test
        void package_private_nested_class_is_not_reflective() {
            givenClass(String.join("\n",
                    "public class Public {",
                    "    static class PackagePrivate {",
                    "    }",
                    "    public static Class<?> innerClass() { return PackagePrivate.class; }",
                    "}"
            ));
            assertFalse(Classes.isReflective((Class<?>) valueOf("Public.innerClass()")));
        }

        @Test
        void private_nested_class_is_not_reflective() {
            givenClass(String.join("\n",
                    "public class Public {",
                    "    private static class Private {",
                    "    }",
                    "    public static Class<?> innerClass() { return Private.class; }",
                    "}"
            ));
            assertFalse(Classes.isReflective((Class<?>) valueOf("Public.innerClass()")));
        }

        @Test
        void protected_nested_class_is_not_reflective() {
            givenClass(String.join("\n",
                    "public class Public {",
                    "    protected static class Protected {",
                    "    }",
                    "    public static Class<?> innerClass() { return Protected.class; }",
                    "}"
            ));
            assertFalse(Classes.isReflective((Class<?>) valueOf("Public.innerClass()")));
        }

        @Test
        void public_class_inside_package_private_class_is_not_reflective() {
            givenClass(String.join("\n",
                    "public class Holder {",
                    "    public static Class<?> innerClass() { return PackagePrivate.Public.class; }",
                    "}"
            ));
            givenClass(String.join("\n",
                    "class PackagePrivate {",
                    "    public static class Public {",
                    "    }",
                    "}"
            ));
            assertFalse(Classes.isReflective((Class<?>) valueOf("Holder.innerClass()")));
        }

        @Test
        void public_class_inside_private_class_is_not_reflective() {
            givenClass(String.join("\n",
                    "public class Public {",
                    "    private static class Private {",
                    "        public static class Inner {",
                    "        }",
                    "    }",
                    "    public static Class<?> innerClass() { return Private.Inner.class; }",
                    "}"
            ));
            assertFalse(Classes.isReflective((Class<?>) valueOf("Public.innerClass()")));
        }

        @Test
        void anonymous_class_is_not_reflective() {
            givenClass(String.join("\n",
                    "public class Public {",
                    "    public static Class<?> anonymousClass() {",
                    "        return new Runnable() { public void run() {} }.getClass();",
                    "    }",
                    "}"
            ));
            assertFalse(Classes.isReflective((Class<?>) valueOf("Public.anonymousClass()")));
        }

        @Test
        void local_class_is_not_reflective() {
            givenClass(String.join("\n",
                    "public class Public {",
                    "    public static Class<?> localClass() {",
                    "        class Local implements Runnable { public void run() {} }",
                    "        return new Local().getClass();",
                    "    }",
                    "}"
            ));
            assertFalse(Classes.isReflective((Class<?>) valueOf("Public.localClass()")));
        }

        @Test
        void lambda_class_is_not_reflective() {
            givenClass(String.join("\n",
                    "public class Public {",
                    "    public static Class<?> lambdaClass() {",
                    "        Runnable lambda = () -> {};",
                    "        return lambda.getClass();",
                    "    }",
                    "}"
            ));
            assertFalse(Classes.isReflective((Class<?>) valueOf("Public.lambdaClass()")));
        }
    }

    @SuppressWarnings("unchecked")
    private Class<Object> typeOf(String expression) {
        return (Class<Object>) executor().main().returnExpression(expression + ".class").evaluate();
    }

    @SuppressWarnings("unchecked")
    private Class<?> classOf(String expression) {
        return (Class<?>) executor().main().returnExpression(expression).evaluate();
    }

    @SuppressWarnings("unchecked")
    private Object valueOf(String expression) {
        return executor().main().returnExpression(expression).evaluate();
    }

    private void givenClass(String code) {
        executor().addClass(code);
    }
}
