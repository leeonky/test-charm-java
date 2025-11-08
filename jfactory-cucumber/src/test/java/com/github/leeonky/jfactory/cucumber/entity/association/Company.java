package com.github.leeonky.jfactory.cucumber.entity.association;

import com.github.leeonky.jfactory.cucumber.EntityFactory;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.persistence.*;
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
    private List<Department> departments;

    public Collection<Department> getDepartments() {
//        TODO departments!=null return departments?
        return EntityFactory.runtimeInstance.type(Department.class).property("company.id", id).queryAll();
    }
}
