package com.github.leeonky.map.spec;

import com.github.leeonky.map.FromProperty;
import com.github.leeonky.map.Mapper;
import com.github.leeonky.map.Mapping;
import com.github.leeonky.map.Simple;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

class PropertyFlattenMapping {
    private final Mapper mapper = new Mapper(getClass().getPackage().getName());
    private final Teacher teacherTom = new Teacher();
    private final Student studentMike = new Student();
    private final Student studentJohn = new Student();

    @BeforeEach
    void setUpData() {
        teacherTom.setName("Tom");

        studentMike.setName("Mike");
        studentMike.setTeacher(teacherTom);

        studentJohn.setName("John");
        studentJohn.setTeacher(teacherTom);

        teacherTom.setStudents(asList(studentMike, studentJohn));
    }

    @Test
    void support_map_from_child_property() {
        assertThat((Object) mapper.map(studentMike, Simple.class))
                .hasFieldOrPropertyWithValue("teacherName", "Tom");
    }

    @Test
    void support_map_child_property_to_list() {
        assertThat((Object) mapper.map(teacherTom, Simple.class))
                .hasFieldOrPropertyWithValue("studentNames", asList("Mike", "John"));
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    static abstract class Person {
        private String Name;
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    static class Student extends Person {
        private Teacher teacher;
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    static class Teacher extends Person {
        private List<Student> students;
    }

    @Mapping(from = Student.class, view = Simple.class)
    static class SimpleStudentDTO {

        @FromProperty("teacher.name")
        public String teacherName;
    }

    @Mapping(from = Teacher.class, view = Simple.class)
    static class SimpleTeacherDTO {
        @FromProperty("students{name}")
        public List<String> studentNames;
    }
}
