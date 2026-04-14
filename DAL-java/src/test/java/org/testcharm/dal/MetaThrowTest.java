package org.testcharm.dal;

import org.junit.jupiter.api.Test;
import org.testcharm.dal.runtime.Data;
import org.testcharm.dal.runtime.JavaClassPropertyAccessor;
import org.testcharm.dal.runtime.RuntimeContextBuilder;
import org.testcharm.dal.runtime.UserRuntimeException;

import static org.testcharm.dal.Assertions.expect;

public class MetaThrowTest {

    @Test
    void should_catch_exception_thrown_in_property_accessor() {
        DAL dal = new DAL().extend();
        RuntimeContextBuilder builder = dal.getRuntimeContextBuilder();
        builder.registerPropertyAccessor(Bean.class, new JavaClassPropertyAccessor<Bean>() {
            @Override
            public Object getValue(Data<Bean> data, Object property) {
                throw new UserRuntimeException(new java.lang.RuntimeException("hello"));
            }
        });

        expect(new Bean()).use(dal).should("any::throw.message= hello");
    }

    @Test
    void should_catch_exception_thrown_in_method() {
        expect(new Bean()).should("raise::throw.message= raise-error");
    }

    public static class Bean {
        public Object raise() {
            throw new java.lang.RuntimeException("raise-error");
        }
    }
}
