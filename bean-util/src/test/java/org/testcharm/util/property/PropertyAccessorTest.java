package org.testcharm.util.property;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.testcharm.util.BeanClass;
import org.testcharm.util.PropertyReader;
import org.testcharm.util.PropertyWriter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.testcharm.util.JavaExecutor.executor;

class PropertyAccessorTest {

    @Nested
    class Reader {

        @Nested
        class Decorate {

            @Test
            void same_decorate_type_do_not_decorate() {

                givenClass(String.join("\n",
                        "public class Clazz {",
                        "    public int field;",
                        "}"
                ));

                PropertyReader<Object> reader = createBeanClass("Clazz").getPropertyReader("field");

                assertThat(reader.decorateType(BeanClass.create(int.class))).isSameAs(reader);
            }

            @Test
            void decorate_with_different_type() {
                givenClass(String.join("\n",
                        "public class Clazz {",
                        "    public Object field = 100;",
                        "}"
                ));

                Object obj = valueOf("new Clazz()");

                assertEquals(100,
                        BeanClass.createFrom(obj).getPropertyReader("field").decorateType(BeanClass.create(int.class)).getValue(obj));
            }
        }
    }

    @Nested
    class Writer {

        @Nested
        class Decorate {

            @Test
            void same_decorate_type_do_not_decorate() {

                givenClass(String.join("\n",
                        "public class Clazz {",
                        "    public int field;",
                        "}"
                ));

                PropertyWriter<Object> writer = createBeanClass("Clazz").getPropertyWriter("field");

                assertThat(writer.decorateType(BeanClass.create(int.class))).isSameAs(writer);
            }

            @Test
            void decorate_with_different_type() {
                givenClass(String.join("\n",
                        "public class Clazz {",
                        "    public Object field;",
                        "}"
                ));

                Object obj = valueOf("new Clazz()");

                BeanClass.createFrom(obj).getPropertyWriter("field").decorateType(BeanClass.create(String.class)).setValue(obj, 100);

                assertEquals("100", BeanClass.createFrom(obj).getPropertyReader("field").getValue(obj));
            }
        }
    }

    private BeanClass<Object> createBeanClass(String name) {
        return BeanClass.create(typeOf(name));
    }

    private Class<Object> typeOf(String expression) {
        return (Class<Object>) executor().main().returnExpression(expression + ".class").evaluate();
    }

    private Object valueOf(String expression) {
        return executor().main().returnExpression(expression).evaluate();
    }

    private void givenClass(String code) {
        executor().addClass(code);
    }
}
