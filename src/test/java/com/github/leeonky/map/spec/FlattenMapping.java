package com.github.leeonky.map.spec;

import com.github.leeonky.map.FromProperty;
import com.github.leeonky.map.Mapper;
import com.github.leeonky.map.MappingFrom;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.Test;

import java.util.*;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

class FlattenMapping {
    private final Mapper mapper = new Mapper(getClass().getPackage().getName());
    private final Teacher teacherTom = new Teacher().setName("Tom");
    private final Teacher teacherSmith = new Teacher().setName("Smith");
    private final Student studentMike = new Student().setTeacher(teacherTom).setName("Mike");
    private final Student studentJohn = new Student().setTeacher(teacherSmith).setName("John");
    private final School school = new School().setStudentList(asList(studentMike, studentJohn)).setStudentMap(new HashMap<String, Student>() {{
        put(studentMike.getName(), studentMike);
        put(studentJohn.getName(), studentJohn);
    }});

    @Test
    void support_map_from_child_property() {
        assertThat((Object) mapper.map(new BestStudent().setStudent(studentMike), BestStudentTeacherNameDTO.class))
                .isInstanceOf(BestStudentTeacherNameDTO.class)
                .hasFieldOrPropertyWithValue("teacherName", "Tom");
    }

    @Test
    void support_map_list_element_property_to_collection() {
        ListToCollection listToCollection = mapper.map(school, ListToCollection.class);

        assertThat(listToCollection)
                .hasFieldOrPropertyWithValue("studentNames", asList("Mike", "John"))
                .hasFieldOrPropertyWithValue("studentNameArray", new String[]{"Mike", "John"})
                .hasFieldOrPropertyWithValue("studentNameSet", new HashSet<>(asList("Mike", "John")));

        assertThat(listToCollection.studentNameLinkedList)
                .isInstanceOf(LinkedList.class)
                .isEqualTo(asList("Mike", "John"));
    }

    @Test
    void support_map_list_element_property_to_map() {
        ListToMap listToMap = mapper.map(school, ListToMap.class);

        assertThat(listToMap)
                .hasFieldOrPropertyWithValue("teacherNameMap", new HashMap<String, String>() {{
                    put("Mike", "Tom");
                    put("John", "Smith");
                }});

        assertThat(listToMap.teacherNameLinkedHashMap).isInstanceOf(LinkedHashMap.class)
                .isEqualTo(new HashMap<String, String>() {{
                    put("Mike", "Tom");
                    put("John", "Smith");
                }})
// TODO
//                .hasFieldOrPropertyWithValue("studentMap", new HashMap<String, Student>() {{
//                    put("Mike", studentMike);
//                    put("John", studentJohn);
//                }})
        ;
    }

    @Test
    void support_map_map_element_property_to_collection() {
        MapToCollection mapToCollection = mapper.map(school, MapToCollection.class);

        assertThat(mapToCollection)
                .hasFieldOrPropertyWithValue("studentNames", asList("Mike", "John"))
                .hasFieldOrPropertyWithValue("studentNameArray", new String[]{"Mike", "John"})
                .hasFieldOrPropertyWithValue("studentNameSet", new HashSet<>(asList("Mike", "John")));

        assertThat(mapToCollection.studentNameLinkedList)
                .isInstanceOf(LinkedList.class)
                .isEqualTo(asList("Mike", "John"));
    }

    @Test
    void support_map_map_element_property_to_map() {
        MapToMap mapToMap = mapper.map(school, MapToMap.class);

        assertThat(mapToMap)
                .hasFieldOrPropertyWithValue("teacherNameMap", new HashMap<String, String>() {{
                    put("Mike", "Tom");
                    put("John", "Smith");
                }});

        assertThat(mapToMap.teacherNameLinkedHashMap).isInstanceOf(LinkedHashMap.class)
                .isEqualTo(new HashMap<String, String>() {{
                    put("Mike", "Tom");
                    put("John", "Smith");
                }});
//        TODO
//        assertThat(mapToMap.studentTeacherMap).isInstanceOf(Map.class)
//                .isEqualTo(new HashMap<String, Teacher>() {{
//                    put("Mike", teacherTom);
//                    put("John", teacherSmith);
//                }});

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
    public static class BestStudent {
        private Student student;
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    public static class School {
        private List<Student> studentList;
        private Map<String, Student> studentMap;
    }

    @MappingFrom(BestStudent.class)
    static class BestStudentTeacherNameDTO {

        @FromProperty("student.teacher.name")
        public String teacherName;
    }

    @MappingFrom(School.class)
    public static class ListToCollection {
        @FromProperty("studentList{name}")
        public List<String> studentNames;

        @FromProperty("studentList{name}")
        public String[] studentNameArray;

        @FromProperty("studentList{name}")
        public Set<String> studentNameSet;

        @FromProperty("studentList{name}")
        public LinkedList<String> studentNameLinkedList;
    }

    @MappingFrom(School.class)
    public static class ListToMap {

        @FromProperty(key = "studentList{name}", value = "studentList{teacher.name}")
        public Map<String, String> teacherNameMap;

        @FromProperty(key = "studentList{name}", value = "studentList{teacher.name}")
        public LinkedHashMap<String, String> teacherNameLinkedHashMap;

//        TODO
//        @FromProperty(key = "studentList{name}", value = "studentList{}")
//        public Map<String, Student> studentMap;
    }

    @MappingFrom(School.class)
    public static class MapToCollection {

        @FromProperty("studentMap{key}")
        public List<String> studentNames;

        @FromProperty("studentMap{key}")
        public String[] studentNameArray;

        @FromProperty("studentMap{value.name}")
        public Set<String> studentNameSet;

        @FromProperty("studentMap{value.name}")
        public LinkedList<String> studentNameLinkedList;
    }

    @MappingFrom(School.class)
    public static class MapToMap {
        @FromProperty(key = "studentMap{key}", value = "studentMap{value.teacher.name}")
        public Map<String, String> teacherNameMap;

        @FromProperty(key = "studentMap{key}", value = "studentMap{value.teacher.name}")
        public LinkedHashMap<String, String> teacherNameLinkedHashMap;

//        TODO
//        @FromProperty(key = "studentMap{key}", value = "studentMap{value.teacher}")
//        public Map<String, Teacher> studentTeacherMap;
    }
}
