package com.github.leeonky.jfactory.cucumber;

import com.github.leeonky.jfactory.JFactory;
import com.github.leeonky.jfactory.cucumber.entity.association.Company;
import com.github.leeonky.jfactory.cucumber.entity.association.Department;
import com.github.leeonky.jfactory.cucumber.factory.Association;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.leeonky.dal.Assertions.expect;
import static com.github.leeonky.jfactory.DataParser.data;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MockTest {
    private JFactory jFactory = new JFactory();
    private CompanyService companyService;

    @BeforeEach
    void setUp() {
        CompanyRepo companyRepo = mock(CompanyRepo.class);
        when(companyRepo.findById(anyLong())).then(a ->
                jFactory.type(Company.class).property("id", a.getArgument(0)).query());

        DepartmentRepo departmentRepo = mock(DepartmentRepo.class);
        when(departmentRepo.findByCompanyId(anyLong())).then(a ->
                new ArrayList<>(jFactory.type(Department.class).property("company.id", a.getArgument(0)).queryAll()));

        companyService = new CompanyService(companyRepo, departmentRepo);

        // manual mock implement
        // companyService = new CompanyService(new MockCompanyRepo(), new MockDepartmentRepo());
    }

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

    @Test
    void no_departments_java15() {
        jFactory.spec(Association.Company.class).properties(data("""
                id: 100L
                name: Acem
                """)).create();

        expect(companyService.dumpDepartments(100L))
                .isEqualTo("Acem:");
    }

    @Test
    void one_department_java15() {
        jFactory.spec(Association.Department.class)
                .properties(data("""
                        company: {
                            id: 100L
                            name: Acem
                        }
                        name: hr
                        """)).create();

        expect(companyService.dumpDepartments(100L)).isEqualTo("Acem: hr");
    }

    @Test
    void two_departments_java15() {
        jFactory.spec(Association.Company.class).properties(data("""
                id: 100L
                name: Acem
                departments: | name |
                             | hr   |
                             | rd   |
                """)).create();

        expect(companyService.dumpDepartments(100L)).isEqualTo("Acem: hr, rd");
    }

    // manual mock implement
    class MockCompanyRepo implements CompanyRepo {
        @Override
        public Company findById(long id) {
            return jFactory.type(Company.class).property("id", id).query();
        }
    }

    // manual mock implement
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
