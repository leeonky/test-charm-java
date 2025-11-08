package com.github.leeonky.jfactory.cucumber.entity.association;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.persistence.*;

@Entity
@Getter
@Setter
@Accessors(chain = true)
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String name;

    @Access(AccessType.PROPERTY)
    private long companyId;

    @Transient
    private Company company;

    public long getCompanyId() {
//        if (company == null)
//            return 0;
        return company.getId();
    }
}
