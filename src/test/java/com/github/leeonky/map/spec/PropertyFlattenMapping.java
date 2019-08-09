package com.github.leeonky.map.spec;

import com.github.leeonky.map.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

class PropertyFlattenMapping {
    private final Mapper mapper = new Mapper(getClass().getPackage().getName());
    private final Teacher teacherTom = new Teacher();
    private final Student studentMike = new Student();
    private final Student studentJohn = new Student();

    private final Bicycle tomBicycle = new Bicycle().setOwner(teacherTom).setColor("blue");
    private final Bicycle johnBicycle = new Bicycle().setOwner(studentJohn).setColor("red");
    private final School school = new School().setBicycles(asList(tomBicycle, johnBicycle));
    private final StudentMap studentMap = new StudentMap();

    @BeforeEach
    void setUpData() {
        teacherTom.setName("Tom");

        studentMike.setTeacher(teacherTom)
                .setAge(19)
                .setName("Mike");

        studentJohn.setTeacher(teacherTom)
                .setAge(20)
                .setName("John");

        teacherTom.setStudents(asList(studentMike, studentJohn));

        studentMap.studentMap = new HashMap<>();
        studentMap.studentMap.put(studentMike.getName(), studentMike);
        studentMap.studentMap.put(studentJohn.getName(), studentJohn);
    }

    @Test
    void support_map_from_child_property() {
        assertThat((Object) mapper.map(studentMike, Simple.class))
                .hasFieldOrPropertyWithValue("teacherName", "Tom");
    }

    @Test
    void support_map_list_element_property_to_list() {
        assertThat((Object) mapper.map(teacherTom, Simple.class))
                .hasFieldOrPropertyWithValue("studentNames", asList("Mike", "John"));
    }

    @Test
    void support_map_list_element_property_to_new_map() {
        assertThat((Object) mapper.map(teacherTom, TeacherStudentAgeDTO.class))
                .hasFieldOrPropertyWithValue("studentAges", new HashMap<String, Integer>() {{
                    put("Mike", 19);
                    put("John", 20);
                }});

    }

    @Test
    void support_map_list_element_property_to_list_with_from_property_and_map_view() {
        List<Object> bicycleOwners = ((SchoolBicycleOwnersDTO) mapper.map(school, SchoolBicycleOwnersDTO.class)).bicycleOwnerList;

        assertThat(bicycleOwners).hasSize(2);

        assertThat(bicycleOwners.get(0))
                .isInstanceOf(SimpleTeacherDTO.class)
                .hasFieldOrPropertyWithValue("name", "Tom");

        assertThat(bicycleOwners.get(1))
                .isInstanceOf(SimpleStudentDTO.class)
                .hasFieldOrPropertyWithValue("name", "John");
    }

    @Test
    void support_map_list_element_property_to_array_with_from_property_and_map_view() {
        Object[] bicycleOwners = ((SchoolBicycleOwnersDTO) mapper.map(school, SchoolBicycleOwnersDTO.class)).bicycleOwnerArray;

        assertThat(bicycleOwners).hasSize(2);

        assertThat(bicycleOwners[0])
                .isInstanceOf(SimpleTeacherDTO.class)
                .hasFieldOrPropertyWithValue("name", "Tom");

        assertThat(bicycleOwners[1])
                .isInstanceOf(SimpleStudentDTO.class)
                .hasFieldOrPropertyWithValue("name", "John");
    }

    @Test
    void support_map_list_element_property_to_map_with_from_property_and_map_view() {
        Map<String, Object> bicycleOwners = ((SchoolBicycleOwnersDTO) mapper.map(school, SchoolBicycleOwnersDTO.class)).bicycleOwnerMap;

        assertThat(bicycleOwners).hasSize(2);

        assertThat(bicycleOwners.get("Tom"))
                .isInstanceOf(SimpleTeacherDTO.class)
                .hasFieldOrPropertyWithValue("name", "Tom");

        assertThat(bicycleOwners.get("John"))
                .isInstanceOf(SimpleStudentDTO.class)
                .hasFieldOrPropertyWithValue("name", "John");

        Map<String, Object> bicycleMap = ((SchoolBicycleOwnersDTO) mapper.map(school, SchoolBicycleOwnersDTO.class)).bicycleMap;

        assertThat(bicycleMap).hasSize(2);

        assertThat(bicycleMap.get("Tom"))
                .isInstanceOf(SimpleBicycleDTO.class)
                .hasFieldOrPropertyWithValue("color", "blue");

        assertThat(bicycleMap.get("John"))
                .isInstanceOf(SimpleBicycleDTO.class)
                .hasFieldOrPropertyWithValue("color", "red");
    }

    @Test
    void should_support_remap_map_element() {
        StudentMapDTO object = mapper.map(studentMap, StudentMapDTO.class);

        assertThat(object.ages).hasSize(2);

        assertThat(object.ages.get(0)).isEqualTo(19);
        assertThat(object.ages.get(1)).isEqualTo(20);
    }

    @Test
    void should_support_remap_map_element_with_view() {
        StudentMapDTO object = mapper.map(studentMap, StudentMapDTO.class);

        assertThat(object.students).hasSize(2);

        assertThat(object.students.get(0))
                .isInstanceOf(SimpleStudentDTO.class)
                .hasFieldOrPropertyWithValue("name", "Mike");

        assertThat(object.students.get(1))
                .isInstanceOf(SimpleStudentDTO.class)
                .hasFieldOrPropertyWithValue("name", "John");
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    static abstract class Person {
        private String Name;
        private Integer age;
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Student extends Person {
        private Teacher teacher;
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Teacher extends Person {
        private List<Student> students;
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Bicycle {
        private Person owner;
        private String color;
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    public static class School {
        private List<Bicycle> bicycles;
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    public static class StudentMap {
        private Map<String, Student> studentMap;
    }

    @Mapping(from = Student.class, view = Simple.class)
    static class SimpleStudentDTO {
        public String name;

        @FromProperty("teacher.name")
        public String teacherName;
    }

    @Mapping(from = Teacher.class, view = Simple.class)
    static class SimpleTeacherDTO {
        public String name;

        @FromProperty("students{name}")
        public List<String> studentNames;
    }

    @Mapping(from = Bicycle.class, view = Simple.class)
    static class SimpleBicycleDTO {
        public String color;
    }

    @MappingFrom(Teacher.class)
    static class TeacherStudentAgeDTO {
        @FromProperty(key = "students{name}", value = "students{age}")
        public Map<String, Integer> studentAges;
    }

    @MappingFrom(StudentMap.class)
    static class StudentMapDTO {
        @FromProperty("studentMap{value.age}")
        public List<Integer> ages;

        @FromProperty("studentMap{value}")
        @MappingView(Simple.class)
        public List<Object> students;
    }

    @MappingFrom(School.class)
    static class SchoolBicycleOwnersDTO {

        @FromProperty("bicycles{owner}")
        @MappingView(Simple.class)
        public List<Object> bicycleOwnerList;

        @FromProperty("bicycles{owner}")
        @MappingView(Simple.class)
        public Object[] bicycleOwnerArray;

        @FromProperty(key = "bicycles{owner.name}", value = "bicycles{owner}")
        @MappingView(Simple.class)
        public Map<String, Object> bicycleOwnerMap;

        @FromProperty(key = "bicycles{owner.name}", value = "bicycles{}")
        @MappingView(Simple.class)
        public Map<String, Object> bicycleMap;
    }
}
