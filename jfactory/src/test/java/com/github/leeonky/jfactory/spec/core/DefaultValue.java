package com.github.leeonky.jfactory.spec.core;

import com.github.leeonky.jfactory.DefaultValueFactory;
import com.github.leeonky.jfactory.JFactory;
import com.github.leeonky.jfactory.ObjectProperty;
import com.github.leeonky.jfactory.Spec;
import com.github.leeonky.util.BeanClass;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.UUID;

import static com.github.leeonky.dal.Assertions.expect;
import static com.github.leeonky.dal.Assertions.expectRun;

class OtherTypeOfDefaultValue {
    private final JFactory jFactory = new JFactory();

    @Test
    void default_value_should_be_null() {
        Bean bean = jFactory.create(Bean.class);

        expect(bean).should("anyType= null");
    }

    public static class AnyType {
    }

    public static class Bean {
        public AnyType anyType;
    }
}

class IgnoreDefaultValue {

    private final JFactory jFactory = new JFactory();

    @Test
    void all_default_values_when_ignore_proprety() {
        jFactory.ignoreDefaultValue(p -> true);

        expect(jFactory.create(Bean.class))
                .should("= {\n" +
                        "  stringValue= null\n" +
                        "  intValue= 0\n" +
                        "  boxedIntValue= null\n" +
                        "  shortValue= 0s\n" +
                        "  boxedShortValue= null\n" +
                        "  byteValue= 0y\n" +
                        "  boxedByteValue= null\n" +
                        "  longValue= 0L\n" +
                        "  boxedLongValue= null\n" +
                        "  floatValue= 0.0f\n" +
                        "  boxedFloatValue= null\n" +
                        "  doubleValue= 0.0d\n" +
                        "  boxedDoubleValue= null\n" +
                        "  boolValue= false\n" +
                        "  boxedBoolValue= null\n" +
                        "  bigInt= null\n" +
                        "  bigDec= null\n" +
                        "  uuid: null\n" +
                        "  date: null\n" +
                        "  instant: null\n" +
                        "  localDate: null\n" +
                        "  localTime: null\n" +
                        "  localDateTime: null\n" +
                        "  offsetDateTime: null\n" +
                        "  zonedDateTime: null\n" +
                        "  enumValue: null\n" +
                        "}");

    }

    @Test
    void ignore_specific_property() {
        jFactory.ignoreDefaultValue(p -> p.getName().equals("stringValue"));

        expect(jFactory.create(Bean.class))
                .should(": {\n" +
                        "  stringValue: null\n" +
                        "  intValue= 1\n" +
                        "}");
    }

    @Test
    void ignore_in_type_spec() {
        jFactory.factory(Bean.class).spec(ins -> {
            ins.spec().property("stringValue").ignore();
        });

        expect(jFactory.create(Bean.class))
                .should(": {\n" +
                        "  stringValue: null\n" +
                        "  intValue= 1\n" +
                        "}");
    }

    @Test
    void ignore_multiple_properties_in_type_spec() {
        jFactory.factory(Bean.class).spec(ins ->
                ins.spec().ignore("stringValue", "boxedIntValue"));

        expect(jFactory.create(Bean.class))
                .should(": {\n" +
                        "  stringValue: null\n" +
                        "  boxedIntValue: null\n" +
                        "  intValue= 1\n" +
                        "}");
    }

    @Test
    void ignore_in_class_spec() {
        expect(jFactory.createAs(IgnoreStringValue.class))
                .should(": {\n" +
                        "  stringValue: null\n" +
                        "  intValue= 1\n" +
                        "}");
    }

    @Test
    void ignore_multiple_properties_in_class_spec() {
        expect(jFactory.createAs(IgnoreStringValueAndBoxedIntValue.class))
                .should(": {\n" +
                        "  stringValue: null\n" +
                        "  boxedIntValue: null\n" +
                        "  intValue= 1\n" +
                        "}");
    }

    public static class Bean {
        public String stringValue;
        public int intValue;
        public Integer boxedIntValue;
        public short shortValue;
        public Short boxedShortValue;
        public byte byteValue;
        public Byte boxedByteValue;
        public long longValue;
        public Long boxedLongValue;
        public float floatValue;
        public Float boxedFloatValue;
        public double doubleValue;
        public Double boxedDoubleValue;
        public boolean boolValue;
        public Boolean boxedBoolValue;
        public BigInteger bigInt;
        public BigDecimal bigDec;
        public UUID uuid;
        public Date date;
        public java.time.Instant instant;
        public java.time.LocalDate localDate;
        public java.time.LocalTime localTime;
        public java.time.LocalDateTime localDateTime;
        public java.time.OffsetDateTime offsetDateTime;
        public java.time.ZonedDateTime zonedDateTime;
        public EnumType enumValue;

        public enum EnumType {
            A, B
        }
    }

    public static class IgnoreStringValue extends Spec<Bean> {

        @Override
        public void main() {
            property("stringValue").ignore();
        }
    }

    public static class IgnoreStringValueAndBoxedIntValue extends Spec<Bean> {

        @Override
        public void main() {
            ignore("stringValue", "boxedIntValue");
        }
    }
}

class DefineDefaultValue {
    @Test
    void define_default_value_by_type() {
        JFactory jFactory = new JFactory();

        jFactory.registerDefaultValueFactory(String.class, new DefaultValueFactory<String>() {
            @Override
            public <T> String create(BeanClass<T> beanType, ObjectProperty<T> objectProperty) {
                return beanType.getSimpleName() + "_" + objectProperty.getProperty().getName();
            }
        });

        expect(jFactory.create(Bean.class)).should("str= Bean_str");
    }

    @Test
    void define_property_default_value() {
        JFactory jFactory = new JFactory();

        jFactory.factory(Bean.class).spec(ins -> ins.spec().property("str").defaultValue("hello_" + ins.getSequence()));

        expect(jFactory.create(Bean.class)).should("str= hello_1");
    }

    @Test
    void define_property_default_value_with_lambda() {
        JFactory jFactory = new JFactory();

        jFactory.factory(Bean.class).spec(ins -> ins.spec().property("str").defaultValue(() -> "from_lambda"));

        expect(jFactory.create(Bean.class)).should("str= from_lambda");
    }

    @Test
    void should_do_not_allow_define_sub_property_default_value() {
        JFactory jFactory = new JFactory();

        jFactory.factory(BeanHolder.class).spec(ins -> ins.spec().property("bean.str").defaultValue("hello"));

        expectRun(() -> jFactory.create(BeanHolder.class)).should("::throw.message: 'Property chain `bean.str` is not supported in the current operation'");
    }

    public static class Bean {
        public String str;
    }

    public static class BeanHolder {
        public Bean bean;
    }
}