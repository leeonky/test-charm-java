package com.github.leeonky.jfactory.cucumber;

import com.github.leeonky.jfactory.JFactory;
import com.github.leeonky.jfactory.cucumber.entity.association.Company;
import com.github.leeonky.jfactory.cucumber.entity.association.Department;
import com.github.leeonky.jfactory.cucumber.factory.Association;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.leeonky.dal.Assertions.expect;

class MockTest {
    private JFactory jFactory = new JFactory();
    private CompanyService companyService = new CompanyService(new MockCompanyRepo(), new MockDepartmentRepo());

    @Test
    void no_departments() {
        jFactory.spec(Association.Company.class).property("name", "Acem").property("id", 100L).create();

        expect(companyService.dumpDepartments(100L)).isEqualTo("Acem:");
    }

    @Test
    void one_department() {
        jFactory.spec(Association.Department.class)
                .property("company.name", "Acem")
                .property("company.id", 100L)
                .property("name", "hr").create();

        expect(companyService.dumpDepartments(100L)).isEqualTo("Acem: hr");
    }

    @Test
    void two_departments() {
        jFactory.spec(Association.Department.class)
                .property("company.name", "Acem")
                .property("company.id", 100L)
                .property("name", "hr").create();

        jFactory.spec(Association.Department.class)
                .property("company.name", "Acem")
                .property("company.id", 100L)
                .property("name", "rd").create();

        expect(companyService.dumpDepartments(100L)).isEqualTo("Acem: hr, rd");
    }

    class MockCompanyRepo implements CompanyRepo {
        @Override
        public Company findById(long id) {
            return jFactory.type(Company.class).property("id", id).query();
        }
    }

    class MockDepartmentRepo implements DepartmentRepo {

        @Override
        public List<Department> findByCompanyId(long id) {
            return new ArrayList<>(jFactory.type(Department.class).property("company.id", id).queryAll());
        }
    }
}

class CompanyService {
    private final CompanyRepo companyRepo;
    private final DepartmentRepo departmentRepo;

    public CompanyService(CompanyRepo companyRepo, DepartmentRepo departmentRepo) {
        this.companyRepo = companyRepo;
        this.departmentRepo = departmentRepo;
    }

    public String dumpDepartments(long companyId) {
        Company company = companyRepo.findById(companyId);
        List<Department> departments = departmentRepo.findByCompanyId(companyId);
        return company.getName() + ":" + (departments.isEmpty() ? "" :
                departments.stream().map(Department::getName).collect(Collectors.joining(", ", " ", "")));
    }
}

interface CompanyRepo {
    Company findById(long id);
}

interface DepartmentRepo {
    List<Department> findByCompanyId(long id);
}
