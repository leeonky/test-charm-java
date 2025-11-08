package com.github.leeonky.jfactory.cucumber.entity.association;

import com.github.leeonky.jfactory.cucumber.EntityFactory;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Accessors(chain = true)
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String name;

    /**
     * This property is only used by JPA for persistence operations and for querying
     * the associated Company (foreign key handling).
     */
    @Access(AccessType.PROPERTY)
    private long companyId;

    /**
     * Delegates the company relationship via the companyId field.
     */
    @Transient
    private Company company;

    /**
     * JPA uses this getter to persist and load the foreign key column (company_id).
     */
    public long getCompanyId() {
        return company == null ? 0 : company.getId();
    }

    /**
     * Used for runtime association queries to retrieve the Company entity
     * based on the companyId.
     */
    public Company getCompany() {
//        TODO company!=null return company?
        return EntityFactory.runtimeInstance.type(Company.class).property("id", companyId).query();
    }

    @Transient
    private List<Employee> employees = new ArrayList<>();
}
