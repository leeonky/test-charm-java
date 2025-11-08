package com.github.leeonky.jfactory.cucumber.entity.association;

import com.github.leeonky.jfactory.cucumber.EntityFactory;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@Getter
@Setter
@Accessors(chain = true)
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String name;

    @Transient
    private List<Department> departments = new ArrayList<>();

    public Collection<Department> getDepartments() {
        return EntityFactory.runtimeInstance.type(Department.class).property("companyId", id).queryAll();
    }

    public Company setDepartments(List<Department> departments) {
        this.departments = departments;
        return this;
    }
}
