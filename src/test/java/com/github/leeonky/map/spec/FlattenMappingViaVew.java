package com.github.leeonky.map.spec;

import com.github.leeonky.map.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

class FlattenMappingViaVew {
    private final Mapper mapper = new Mapper(getClass().getPackage().getName());
    private final Student studentMike = new Student().setName("Mike");
    private final Student studentJohn = new Student().setName("John");
    private final School school = new School().setStudents(asList(studentMike, studentJohn));

    @Test
    void support_map_list_element_property_to_collection_with_from_property_and_map_view() {
        StudentCollectionDTO studentCollectionDTO = mapper.map(school, StudentCollectionDTO.class);

        assertThat(studentCollectionDTO.studentList).hasSize(2);

        assertThat(studentCollectionDTO.studentList.get(0))
                .isInstanceOf(StudentTDO.class)
                .hasFieldOrPropertyWithValue("name", "Mike");

        assertThat(studentCollectionDTO.studentList.get(1))
                .isInstanceOf(StudentTDO.class)
                .hasFieldOrPropertyWithValue("name", "John");

        assertThat(studentCollectionDTO.studentArray).hasSize(2);

        assertThat(studentCollectionDTO.studentArray[0])
                .isInstanceOf(StudentTDO.class)
                .hasFieldOrPropertyWithValue("name", "Mike");

        assertThat(studentCollectionDTO.studentArray[1])
                .isInstanceOf(StudentTDO.class)
                .hasFieldOrPropertyWithValue("name", "John");

        assertThat(studentCollectionDTO.studentSet).hasSize(2);

        assertThat(studentCollectionDTO.studentSet.toArray()[0])
                .isInstanceOf(StudentTDO.class)
                .hasFieldOrPropertyWithValue("name", "Mike");

        assertThat(studentCollectionDTO.studentSet.toArray()[1])
                .isInstanceOf(StudentTDO.class)
                .hasFieldOrPropertyWithValue("name", "John");

        assertThat(studentCollectionDTO.studentLinkedList).isInstanceOf(LinkedList.class).hasSize(2);

        assertThat(studentCollectionDTO.studentLinkedList.get(0))
                .isInstanceOf(StudentTDO.class)
                .hasFieldOrPropertyWithValue("name", "Mike");

        assertThat(studentCollectionDTO.studentLinkedList.get(1))
                .isInstanceOf(StudentTDO.class)
                .hasFieldOrPropertyWithValue("name", "John");
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Student {
        private String name;
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    public static class School {
        private List<Student> students;
    }

    @Mapping(from = Student.class, view = Simple.class)
    static class StudentTDO {
        public String name;
    }

    @MappingFrom(School.class)
    public static class StudentCollectionDTO {
        @FromProperty("students{}")
        @MappingView(Simple.class)
        public List<Object> studentList;

        @FromProperty("students{}")
        @MappingView(Simple.class)
        public Object[] studentArray;

        @FromProperty("students{}")
        @MappingView(Simple.class)
        public Set<Object> studentSet;

        @FromProperty("students{}")
        @MappingView(Simple.class)
        public LinkedList<Object> studentLinkedList;
    }
}
