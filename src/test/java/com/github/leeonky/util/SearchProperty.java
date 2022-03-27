package com.github.leeonky.util;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.github.leeonky.util.BeanClass.create;
import static org.assertj.core.api.Assertions.assertThat;

class SearchProperty {
    public static class SuperField {
        public static int superField;
    }

    public static class SubWithSuperField extends SuperField {
    }

    public static class SuperGetter extends SuperField {
        public static int superGetterValue;

        public static int getSuperGetter() {
            return superGetterValue;
        }
    }

    public static class SubWithSuperGetter extends SuperGetter {
    }

    public static class SubGetter extends SuperGetter {
        public static int subGetterValue;
        public static int superField;

        public static int getSuperGetter() {
            return subGetterValue;
        }
    }

    public static class SuperSetter extends SuperField {

        public static int superSetterValue;

        public static void setSuperSetter(int v) {
            superSetterValue = v;
        }
    }

    public static class SubWithSuperSetter extends SuperSetter {
    }

    public static class SubSetter extends SuperSetter {
        public static int subSetterValue;
        public static int superField;

        public static void setSuperSetter(int v) {
            subSetterValue = v;
        }
    }

    @Nested
    public class Reader {

        @Test
        void get_reader_by_static_super_field() {
            SubWithSuperField sub = new SubWithSuperField();
            SuperField.superField = 100;

            assertThat(create(SubWithSuperField.class).getPropertyValue(sub, "superField")).isEqualTo(100);
        }

        @Test
        void get_reader_by_static_super_getter() {
            SubWithSuperGetter sub = new SubWithSuperGetter();
            SubWithSuperGetter.superGetterValue = 200;

            assertThat(create(SubWithSuperGetter.class).getPropertyValue(sub, "superGetter")).isEqualTo(200);
        }

        @Test
        void get_reader_by_static_sub_field() {
            SuperField.superField = 200;
            SubGetter sub = new SubGetter();
            SubGetter.superField = 100;

            assertThat(create(SubGetter.class).getPropertyValue(sub, "superField")).isEqualTo(100);
        }

        @Test
        void get_reader_by_static_sub_getter() {
            SubGetter sub = new SubGetter();
            SubGetter.subGetterValue = 100;
            SuperGetter.superGetterValue = 0;

            assertThat(create(SubGetter.class).getPropertyValue(sub, "superGetter")).isEqualTo(100);
        }
    }

    @Nested
    public class Writer {

        @Test
        void get_writer_by_static_super_field() {
            SubWithSuperField sub = new SubWithSuperField();
            create(SubWithSuperField.class).setPropertyValue(sub, "superField", 100);

            assertThat(SuperField.superField).isEqualTo(100);
        }

        @Test
        void get_writer_by_static_super_getter() {
            SubWithSuperSetter sub = new SubWithSuperSetter();
            create(SubWithSuperSetter.class).setPropertyValue(sub, "superSetter", 200);

            assertThat(SuperSetter.superSetterValue).isEqualTo(200);
        }

        @Test
        void set_reader_by_static_sub_field() {
            SubSetter sub = new SubSetter();
            SuperField.superField = 0;
            create(SubSetter.class).setPropertyValue(sub, "superField", 100);

            assertThat(SubSetter.superField).isEqualTo(100);
            assertThat(SuperField.superField).isEqualTo(0);
        }

        @Test
        void set_reader_by_static_sub_setter() {
            SubSetter sub = new SubSetter();
            SuperSetter.superSetterValue = 0;
            create(SubSetter.class).setPropertyValue(sub, "superSetter", 100);

            assertThat(SubSetter.subSetterValue).isEqualTo(100);
            assertThat(SuperSetter.superSetterValue).isEqualTo(0);
        }
    }
}
