package com.github.leeonky.jfactory.cucumber.factory;

import com.github.leeonky.jfactory.Spec;

public class Association {
    public static class Company extends Spec<com.github.leeonky.jfactory.cucumber.entity.association.Company> {
        @Override
        public void main() {
            property("departments[]").is(Department.class);
            property("departments").reverseAssociation("company");
        }
    }

    public static class Department extends Spec<com.github.leeonky.jfactory.cucumber.entity.association.Department> {
        @Override
        public void main() {
            property("company").is(Company.class);

            property("employees[]").is(Employee.class);
            property("employees").reverseAssociation("department");
        }
    }

    public static class Employee extends Spec<com.github.leeonky.jfactory.cucumber.entity.association.Employee> {

        @Override
        public void main() {
            property("department").is(Department.class);
        }
    }
}
