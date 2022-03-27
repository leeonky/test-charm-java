package com.github.leeonky.util;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.github.leeonky.util.BeanClass.create;
import static org.assertj.core.api.Assertions.assertThat;

class SearchProperty {
    public static class SupperField {
        public static int supperField;
    }

    public static class SubWithSupperField extends SupperField {
    }

    public static class SupperGetter extends SupperField {
        public static int supperGetterValue;

        public static int getSupperGetter() {
            return supperGetterValue;
        }
    }

    public static class SubWithSupperGetter extends SupperGetter {
    }


    public static class SupperSetter extends SupperField {

        public static int supperSetterValue;

        public static void setSupperSetter(int v) {
            supperSetterValue = v;
        }
    }

    public static class SubWithSupperSetter extends SupperSetter {
    }

    @Nested
    public class Reader {

        @Test
        void get_reader_by_static_supper_field() {
            SubWithSupperField sub = new SubWithSupperField();
            sub.supperField = 100;

            assertThat(create(SubWithSupperField.class).getPropertyValue(sub, "supperField")).isEqualTo(100);
        }

        @Test
        void get_reader_by_static_supper_getter() {
            SubWithSupperGetter sub = new SubWithSupperGetter();
            sub.supperGetterValue = 200;

            assertThat(create(SubWithSupperGetter.class).getPropertyValue(sub, "supperGetter")).isEqualTo(200);
        }
    }

    @Nested
    public class Writer {

        @Test
        void get_writer_by_static_supper_field() {
            SubWithSupperField sub = new SubWithSupperField();
            create(SubWithSupperField.class).setPropertyValue(sub, "supperField", 100);

            assertThat(sub.supperField).isEqualTo(100);
        }

        @Test
        void get_writer_by_static_supper_getter() {
            SubWithSupperSetter sub = new SubWithSupperSetter();
            create(SubWithSupperSetter.class).setPropertyValue(sub, "supperSetter", 200);

            assertThat(sub.supperSetterValue).isEqualTo(200);
        }
    }

//            TODO sub class subgetter  > supper getter > subclass field > supper field (contains static and non static)
//            TODO sub class subsetter  > supper setter > subclass field > supper field (fields should not const)
}
