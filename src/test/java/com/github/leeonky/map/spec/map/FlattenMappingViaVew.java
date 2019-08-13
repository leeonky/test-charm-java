package com.github.leeonky.map.spec.map;

import com.github.leeonky.map.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.Test;

import java.util.*;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

class FlattenMappingViaVew {
    private final Mapper mapper = new Mapper(getClass().getPackage().getName());
    private final Teacher teacherTom = new Teacher().setName("Tom");
    private final Teacher teacherSmith = new Teacher().setName("Smith");
    private final Student studentMike = new Student().setName("Mike").setTeacher(teacherTom);
    private final Student studentJohn = new Student().setName("John").setTeacher(teacherSmith);
    private final School school = new School().setStudentList(asList(studentMike, studentJohn))
            .setStudentMap(new HashMap<String, Student>() {{
                put("Mike", studentMike);
                put("John", studentJohn);
            }});

    @Test
    void support_map_list_element_property_to_collection_with_from_property_and_map_view() {
        ListToCollection listToCollection = mapper.map(school, ListToCollection.class);

        assertThat(listToCollection.studentList).hasSize(2);
        assertThat(listToCollection.studentList.get(0))
                .isInstanceOf(StudentDTO.class)
                .hasFieldOrPropertyWithValue("name", "Mike");
        assertThat(listToCollection.studentList.get(1))
                .isInstanceOf(StudentDTO.class)
                .hasFieldOrPropertyWithValue("name", "John");

        assertThat(listToCollection.studentArray).hasSize(2);
        assertThat(listToCollection.studentArray[0])
                .isInstanceOf(StudentDTO.class)
                .hasFieldOrPropertyWithValue("name", "Mike");
        assertThat(listToCollection.studentArray[1])
                .isInstanceOf(StudentDTO.class)
                .hasFieldOrPropertyWithValue("name", "John");

        assertThat(listToCollection.studentTeacherSet).hasSize(2);
        assertThat(listToCollection.studentTeacherSet.toArray()[0])
                .isInstanceOf(TeacherDTO.class)
                .hasFieldOrPropertyWithValue("name", "Tom");
        assertThat(listToCollection.studentTeacherSet.toArray()[1])
                .isInstanceOf(TeacherDTO.class)
                .hasFieldOrPropertyWithValue("name", "Smith");

        assertThat(listToCollection.studentTeacherLinkedList).isInstanceOf(LinkedList.class).hasSize(2);
        assertThat(listToCollection.studentTeacherLinkedList.get(0))
                .isInstanceOf(TeacherDTO.class)
                .hasFieldOrPropertyWithValue("name", "Tom");
        assertThat(listToCollection.studentTeacherLinkedList.get(1))
                .isInstanceOf(TeacherDTO.class)
                .hasFieldOrPropertyWithValue("name", "Smith");
    }

    @Test
    void support_map_list_element_property_to_map_with_from_property_and_map_view() {
        ListToMap listToMap = mapper.map(school, ListToMap.class);

        assertThat(listToMap.studentMap).hasSize(2).isInstanceOf(LinkedHashMap.class);
        assertThat(listToMap.studentMap.get("Mike")).isInstanceOf(StudentDTO.class)
                .hasFieldOrPropertyWithValue("name", "Mike");
        assertThat(listToMap.studentMap.get("John")).isInstanceOf(StudentDTO.class)
                .hasFieldOrPropertyWithValue("name", "John");

        assertThat(listToMap.studentTeacherMap).isInstanceOf(HashMap.class).hasSize(2);
        assertThat(listToMap.studentTeacherMap.get("Mike")).isInstanceOf(TeacherDTO.class)
                .hasFieldOrPropertyWithValue("name", "Tom");
        assertThat(listToMap.studentTeacherMap.get("John")).isInstanceOf(TeacherDTO.class)
                .hasFieldOrPropertyWithValue("name", "Smith");
    }

    @Test
    void support_map_map_property_to_collection_with_from_property_and_map_view() {
        MapToCollection mapToCollection = mapper.map(school, MapToCollection.class);

        assertThat(mapToCollection.studentList).hasSize(2);
        assertThat(mapToCollection.studentList.get(0))
                .isInstanceOf(StudentDTO.class)
                .hasFieldOrPropertyWithValue("name", "Mike");
        assertThat(mapToCollection.studentList.get(1))
                .isInstanceOf(StudentDTO.class)
                .hasFieldOrPropertyWithValue("name", "John");

        assertThat(mapToCollection.studentArray).hasSize(2);
        assertThat(mapToCollection.studentArray[0])
                .isInstanceOf(StudentDTO.class)
                .hasFieldOrPropertyWithValue("name", "Mike");
        assertThat(mapToCollection.studentArray[1])
                .isInstanceOf(StudentDTO.class)
                .hasFieldOrPropertyWithValue("name", "John");

        assertThat(mapToCollection.studentTeacherSet).hasSize(2);
        assertThat(mapToCollection.studentTeacherSet.toArray()[0])
                .isInstanceOf(TeacherDTO.class)
                .hasFieldOrPropertyWithValue("name", "Tom");
        assertThat(mapToCollection.studentTeacherSet.toArray()[1])
                .isInstanceOf(TeacherDTO.class)
                .hasFieldOrPropertyWithValue("name", "Smith");

        assertThat(mapToCollection.studentTeacherLinkedList).isInstanceOf(LinkedList.class).hasSize(2);
        assertThat(mapToCollection.studentTeacherLinkedList.get(0))
                .isInstanceOf(TeacherDTO.class)
                .hasFieldOrPropertyWithValue("name", "Tom");
        assertThat(mapToCollection.studentTeacherLinkedList.get(1))
                .isInstanceOf(TeacherDTO.class)
                .hasFieldOrPropertyWithValue("name", "Smith");
    }

    @Test
    void support_map_map_property_to_map_with_from_property_and_map_view() {
        MapToMap mapToMap = mapper.map(school, MapToMap.class);

        assertThat(mapToMap.studentMap).hasSize(2).isInstanceOf(LinkedHashMap.class);
        assertThat(mapToMap.studentMap.get("Mike")).isInstanceOf(StudentDTO.class)
                .hasFieldOrPropertyWithValue("name", "Mike");
        assertThat(mapToMap.studentMap.get("John")).isInstanceOf(StudentDTO.class)
                .hasFieldOrPropertyWithValue("name", "John");

        assertThat(mapToMap.studentTeacherMap).isInstanceOf(HashMap.class).hasSize(2);
        assertThat(mapToMap.studentTeacherMap.get("Mike")).isInstanceOf(TeacherDTO.class)
                .hasFieldOrPropertyWithValue("name", "Tom");
        assertThat(mapToMap.studentTeacherMap.get("John")).isInstanceOf(TeacherDTO.class)
                .hasFieldOrPropertyWithValue("name", "Smith");
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Student {
        private String name;
        private Teacher teacher;
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Teacher {
        private String name;
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    public static class School {
        private List<Student> studentList;
        private Map<String, Student> studentMap;
    }

    @Mapping(from = Student.class, view = Simple.class)
    static class StudentDTO {
        public String name;
    }

    @Mapping(from = Teacher.class, view = Simple.class)
    static class TeacherDTO {
        public String name;
    }

    @MappingFrom(School.class)
    public static class ListToCollection {
        @FromProperty("studentList{}")
        @MappingView(Simple.class)
        public List<Object> studentList;

        @FromProperty("studentList{}")
        @MappingView(Simple.class)
        public Object[] studentArray;

        @FromProperty("studentList{teacher}")
        @MappingView(Simple.class)
        public Set<Object> studentTeacherSet;

        @FromProperty("studentList{teacher}")
        @MappingView(Simple.class)
        public LinkedList<Object> studentTeacherLinkedList;
    }

    @MappingFrom(School.class)
    public static class ListToMap {
        @FromProperty(key = "studentList{name}", value = "studentList{}")
        @MappingView(Simple.class)
        public Map<String, Object> studentMap;

        @FromProperty(key = "studentList{name}", value = "studentList{teacher}")
        @MappingView(Simple.class)
        public HashMap<String, Object> studentTeacherMap;
    }

    @MappingFrom(School.class)
    public static class MapToCollection {
        @FromProperty("studentMap{value}")
        @MappingView(Simple.class)
        public List<Object> studentList;

        @FromProperty("studentMap{value}")
        @MappingView(Simple.class)
        public Object[] studentArray;

        @FromProperty("studentMap{value.teacher}")
        @MappingView(Simple.class)
        public Set<Object> studentTeacherSet;

        @FromProperty("studentMap{value.teacher}")
        @MappingView(Simple.class)
        public LinkedList<Object> studentTeacherLinkedList;
    }

    @MappingFrom(School.class)
    public static class MapToMap {
        @FromProperty(key = "studentMap{key}", value = "studentMap{value}")
        @MappingView(Simple.class)
        public Map<String, Object> studentMap;

        @FromProperty(key = "studentMap{key}", value = "studentMap{value.teacher}")
        @MappingView(Simple.class)
        public HashMap<String, Object> studentTeacherMap;
    }
}
