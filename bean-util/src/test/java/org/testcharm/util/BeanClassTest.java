package org.testcharm.util;

import hastype.One;
import hastype.Two;
import hastype.pkg.Five;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.testcharm.dal.extensions.ExtensionInSrcFolder;
import org.testcharm.dal.extensions.basic.string.StringExtension;
import subtype.Base;
import subtype.Sub1;
import subtype.Sub2;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.testcharm.util.BeanClass.create;
import static org.testcharm.util.Classes.named;

class BeanClassTest {

    @Test
    void get_type() {
        assertThat(create(String.class).getType()).isEqualTo(String.class);
    }

    @Test
    void get_name() {
        assertThat(create(String.class).getName()).isEqualTo(String.class.getName());
    }

    @Test
    void get_simple_name() {
        assertThat(create(String.class).getSimpleName()).isEqualTo(String.class.getSimpleName());
    }

    @Test
    void new_instance() {
        assertThat(create(String.class).newInstance()).isEqualTo("");
    }

    @Test
    void new_instance_with_arg() {
        assertThat(create(String.class).newInstance("hello")).isEqualTo("hello");
    }

    @Test
    void new_instance_failed_when_no_candidate_constructor() {
        assertThrows(NoAppropriateConstructorException.class,
                () -> create(BeanClassTest.class).newInstance("hello"));
    }

    @Test
    void get_class_name() {
        assertThat(Classes.getClassName("")).isEqualTo(String.class.getName());

        assertThat(Classes.getClassName(null)).isEqualTo(null);
    }

    @Test
    void create_default_value() {
        assertThat(create(int.class).createDefault()).isEqualTo(0);
        assertThat(create(Integer.class).createDefault()).isNull();
    }

    @Test
    void get_generic_params() {
        assertThat(create(Integer.class).hasTypeArguments()).isFalse();
        assertThat(create(Integer.class).getTypeArguments(0)).isEmpty();
    }

    @Test
    void hash_code() {
        assertThat(create(Integer.class).hashCode())
                .isEqualTo(Objects.hash(BeanClass.class, Integer.class));
    }

    @Test
    void bean_class_equal() {
        assertThat(new BeanClass<>(Integer.class)).isEqualTo(new BeanClass<>(Integer.class));
    }

    @Test
    void get_class_from_instance() {
        assertThat(BeanClass.getClass(new BeanClassTest())).isEqualTo(BeanClassTest.class);
    }

    @Test
    void create_from_instance() {
        assertThat(BeanClass.createFrom(new BeanClassTest())).isEqualTo(create(BeanClassTest.class));
    }

    static class StringList extends ArrayList<String> {
    }

    static class SubStringList extends StringList {
    }

    static class StringSupplier implements Supplier<String> {
        @Override
        public String get() {
            return null;
        }
    }

    static class SubStringSupplier extends StringSupplier {
    }

    @Nested
    class Annotation {

        @Test
        void get_annotation() {
            assertThat(create(Annotation.class).getAnnotation(Nested.class)).isInstanceOf(Nested.class);
            assertThat(create(Annotation.class).annotation(Nested.class).get()).isInstanceOf(Nested.class);

            assertThat(create(Annotation.class).getAnnotation(Deprecated.class)).isNull();
            assertThat(create(Annotation.class).annotation(Deprecated.class)).isEmpty();
        }
    }

    @Nested
    class GetSuper {

        @Test
        void should_return_bean_class_by_given_class() {
            BeanClass<ArrayList> beanClass = create(StringList.class).getSuper(ArrayList.class);

            assertThat(beanClass.getType()).isEqualTo(ArrayList.class);
            assertThat(beanClass.getTypeArguments(0).get().getType()).isEqualTo(String.class);
        }

        @Test
        void should_return_bean_class_by_given_grand_farther_class() {
            BeanClass<ArrayList> beanClass = create(SubStringList.class).getSuper(ArrayList.class);

            assertThat(beanClass.getType()).isEqualTo(ArrayList.class);
            assertThat(beanClass.getTypeArguments(0).get().getType()).isEqualTo(String.class);
        }

        @Test
        void should_return_bean_class_by_given_interface() {
            BeanClass<Supplier> beanClass = create(StringSupplier.class).getSuper(Supplier.class);

            assertThat(beanClass.getType()).isEqualTo(Supplier.class);
            assertThat(beanClass.getTypeArguments(0).get().getType()).isEqualTo(String.class);
        }

        @Test
        void should_return_bean_class_by_given_grand_farther_interface() {
            BeanClass<Supplier> beanClass = create(SubStringSupplier.class).getSuper(Supplier.class);

            assertThat(beanClass.getType()).isEqualTo(Supplier.class);
            assertThat(beanClass.getTypeArguments(0).get().getType()).isEqualTo(String.class);
        }
    }

    @Nested
    class GetTypesInPackage {

        @Test
        void empty_when_no_type() {
            assertThat(Classes.allTypesIn("empty")).isEmpty();
        }

        @Test
        void types_in_package_and_sub_package() {
            assertThat(new HashSet<>(Classes.allTypesIn("hastype")))
                    .containsOnly(One.class, Two.class, Two.Three.class, Two.Four.class, Five.class);
        }

        @Test
        void sub_types_in_package() {
            assertThat(new HashSet<>(Classes.subTypesOf(Base.class, "subtype"))).containsOnly(Sub1.class, Sub2.class);
        }

        @Test
        void sub_types_in_package_include_super() {
            assertThat(new HashSet<>(Classes.assignableTypesOf(Base.class, "subtype")))
                    .containsOnly(Base.class, Sub1.class, Sub2.class);
        }
    }

    @Nested
    class AllClassesInJar {

        @Test
        void empty_when_no_type() {
            List<Class<?>> classes = Classes.allTypesIn("org.testcharm.dal.extensions");
            assertThat(classes).contains(StringExtension.class, ExtensionInSrcFolder.class);
        }
    }

    @Nested
    class RuntimeType {

        @Test
        void is_instance() {
            assertThat(create(String.class).isInstance("string")).isTrue();
            assertThat(create(String.class).isInstance(100)).isFalse();
        }

        @Test
        void is_inherited_from() {
            assertThat(create(String.class).isInheritedFrom(CharSequence.class)).isTrue();
            assertThat(create(String.class).isInheritedFrom(Integer.class)).isFalse();
        }
    }

    @Nested
    class NamedClass {

        @Nested
        class AnonymousClass {

            @Test
            void should_return_same_class_when_class_is_named_class() {
                assertThat(named(StringList.class)).isEqualTo(StringList.class);
            }

            @Test
            void should_return_same_class_when_instance_is_anonymous_class() {
                Class<? extends StringList> type = new StringList() {
                }.getClass();
                assertThat(named((Class<? extends StringList>) type)).isEqualTo(type);
            }

            @Test
            void should_use_super_class_when_instance_is_anonymous_class_fo_interface() {
                assertThat(named(new Supplier<Object>() {
                    @Override
                    public Object get() {
                        return null;
                    }
                }.getClass())).isEqualTo(Supplier.class);
            }
        }

        @Nested
        class Lambda {

            @Test
            void should_return_same_class_when_class_is_named_class() {
                assertThat(named(Supplier.class)).isEqualTo(Supplier.class);
            }

            @Test
            void should_use_super_class_when_instance_is_lambda() {
                assertThat(named(((Supplier<Object>) () -> null).getClass())).isEqualTo(Supplier.class);
            }
        }
    }

    @Nested
    class DynamicBeanClass {

        @Test
        void create_dynamic_bean_with_reader_decorator() {
            BeanClass<?> beanClass = new BeanClass<>(BeanDy.class, new PropertyProxyFactory<BeanDy>() {
                @Override
                public PropertyReader<BeanDy> proxyReader(PropertyReader<BeanDy> reader) {
                    if (reader.getName().equals("value"))
                        return new PropertyReaderDecorator<BeanDy>(reader) {
                            @Override
                            public Type getGenericType() {
                                return new TypeReference<List<String>>() {
                                }.getType().getGenericType();
                            }
                        };
                    return reader;
                }
            });

            PropertyReader<?> reader = beanClass.getPropertyReader("value");

            BeanClass<?> type = reader.getType();
            assertThat(type.getTypeArguments(0).get().getType()).isEqualTo(String.class);
        }

        @Test
        void create_dynamic_bean_with_writer_decorator() {
            BeanClass<?> beanClass = new BeanClass<>(BeanDy.class, new PropertyProxyFactory<BeanDy>() {
                @Override
                public PropertyWriter<BeanDy> proxyWriter(PropertyWriter<BeanDy> writer) {
                    if (writer.getName().equals("value"))
                        return new PropertyWriterDecorator<BeanDy>(writer) {
                            @Override
                            public Type getGenericType() {
                                return new TypeReference<List<String>>() {
                                }.getType().getGenericType();
                            }
                        };
                    return writer;
                }
            });

            PropertyWriter<?> writer = beanClass.getPropertyWriter("value");

            BeanClass<?> type = writer.getType();
            assertThat(type.getTypeArguments(0).get().getType()).isEqualTo(String.class);
        }
    }

    public static class BeanDy {
        public Object value;
    }
}