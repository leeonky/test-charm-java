package com.github.leeonky.jfactory.cucumber.entity.association;

import com.github.leeonky.jfactory.cucumber.EntityFactory;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.persistence.*;

@Entity
@Getter
@Setter
@Accessors(chain = true)
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String name;

    @Access(AccessType.PROPERTY)
    private long departmentId;

    @Transient
    private Department department;

    public long getDepartmentId() {
        Department department = getDepartment();
        return department != null ? department.getId() : departmentId;
    }

    public Department getDepartment() {
        return department != null ? department : EntityFactory.runtimeInstance.type(Department.class).property("id", departmentId).query();
    }
}
