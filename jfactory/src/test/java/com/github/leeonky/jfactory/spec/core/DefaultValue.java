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

class SupportedBuildInDefaultValueTypes {

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

    private final JFactory jFactory = new JFactory();

    @Test
    void default_value_depends_on_sequence() {
        expect(jFactory.create(Bean.class))
                .should("= {\n" +
                        "  stringValue= stringValue#1\n" +
                        "  intValue= 1\n" +
                        "  boxedIntValue= 1\n" +
                        "  shortValue= 1s\n" +
                        "  boxedShortValue= 1s\n" +
                        "  byteValue= 1y\n" +
                        "  boxedByteValue= 1y\n" +
                        "  longValue= 1L\n" +
                        "  boxedLongValue= 1L\n" +
                        "  floatValue= 1.0f\n" +
                        "  boxedFloatValue= 1.0f\n" +
                        "  doubleValue= 1.0d\n" +
                        "  boxedDoubleValue= 1.0d\n" +
                        "  boolValue= true\n" +
                        "  boxedBoolValue= true\n" +
                        "  bigInt= 1bi\n" +
                        "  bigDec= 1bd\n" +
                        "  uuid: '00000000-0000-0000-0000-000000000001'\n" +
                        "  date.toInstant: '1996-01-24T00:00:00Z'\n" +
                        "  instant: '1996-01-23T00:00:01Z'\n" +
                        "  localDate: '1996-01-24'\n" +
                        "  localTime: '00:00:01'\n" +
                        "  localDateTime: '1996-01-23T00:00:01'\n" +
                        "  offsetDateTime.toInstant: '1996-01-23T00:00:01Z'\n" +
                        "  zonedDateTime.toInstant: '1996-01-23T00:00:01Z'\n" +
                        "  enumValue: A\n" +
                        "}");

        expect(jFactory.create(Bean.class))
                .should(": {\n" +
                        "  stringValue= stringValue#2\n" +
                        "  intValue= 2\n" +
                        "  boxedIntValue= 2\n" +
                        "  shortValue= 2s\n" +
                        "  boxedShortValue= 2s\n" +
                        "  byteValue= 2y\n" +
                        "  boxedByteValue= 2y\n" +
                        "  longValue= 2L\n" +
                        "  boxedLongValue= 2L\n" +
                        "  floatValue= 2.0f\n" +
                        "  boxedFloatValue= 2.0f\n" +
                        "  doubleValue= 2.0d\n" +
                        "  boxedDoubleValue= 2.0d\n" +
                        "  boolValue= false\n" +
                        "  boxedBoolValue= false\n" +
                        "  bigInt= 2bi\n" +
                        "  bigDec= 2bd\n" +
                        "  uuid: '00000000-0000-0000-0000-000000000002'\n" +
                        "  date.toInstant: '1996-01-25T00:00:00Z'\n" +
                        "  instant: '1996-01-23T00:00:02Z'\n" +
                        "  localDate: '1996-01-25'\n" +
                        "  localTime: '00:00:02'\n" +
                        "  localDateTime: '1996-01-23T00:00:02'\n" +
                        "  offsetDateTime.toInstant: '1996-01-23T00:00:02Z'\n" +
                        "  zonedDateTime.toInstant: '1996-01-23T00:00:02Z'\n" +
                        "  enumValue: B\n" +
                        "}");
    }

}

class OtherTypeOfDefaultValue {
    private final JFactory jFactory = new JFactory();

    public static class AnyType {
    }

    public static class Bean {
        public AnyType anyType;
    }

    @Test
    void default_value_should_be_null() {
        Bean bean = jFactory.create(Bean.class);

        expect(bean).should("anyType= null");
    }
}

class ResetSequence {
    private final JFactory jFactory = new JFactory();

    public static class Bean {
        public String str;
    }

    @Test
    void set_sequence_start() {
        jFactory.setSequenceStart(100);

        expect(jFactory.create(Bean.class)).should("str= str#100");
    }

    @Test
    void should_use_new_sequence_start_after_sequence_reset() {
        jFactory.setSequenceStart(100);
        jFactory.create(Bean.class);

        jFactory.resetSequence();

        expect(jFactory.create(Bean.class)).should("str= str#100");
    }
}

class SequenceWrapAround {
    private final JFactory jFactory = new JFactory();

    public static class Bean {
        public byte b;
        public short s;
        public int i;
        public long l;
        public E e;
        public boolean bool;

        public enum E {A, B}
    }

    @Test
    void byte_property() {
        jFactory.setSequenceStart(Byte.MAX_VALUE);

        expect(jFactory.create(Bean.class)).should("b= 127y");
        expect(jFactory.create(Bean.class)).should("b= -128y");
    }

    @Test
    void short_property() {
        jFactory.setSequenceStart(Short.MAX_VALUE);

        expect(jFactory.create(Bean.class)).should("s= 32767s");
        expect(jFactory.create(Bean.class)).should("s= -32768s");
    }

    @Test
    void int_property() {
        jFactory.setSequenceStart(Integer.MAX_VALUE);

        expect(jFactory.create(Bean.class)).should("i= 2147483647");
        expect(jFactory.create(Bean.class)).should("i= -2147483648");
    }

    @Test
    void long_property() {
        jFactory.setSequenceStart(Integer.MAX_VALUE);

        expect(jFactory.create(Bean.class)).should("l= 2147483647L");
        expect(jFactory.create(Bean.class)).should("l= -2147483648L");
    }

    @Test
    void enum_property() {
        expect(jFactory.create(Bean.class)).should("e: A");
        expect(jFactory.create(Bean.class)).should("e: B");
        expect(jFactory.create(Bean.class)).should("e: A");
    }

    @Test
    void boolean_property() {
        expect(jFactory.create(Bean.class)).should("bool= true");
        expect(jFactory.create(Bean.class)).should("bool= false");
        expect(jFactory.create(Bean.class)).should("bool= true");
    }
}

class IgnoreDefaultValue {

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

    private final JFactory jFactory = new JFactory();

    @Test
    void default_value_when_ignore_supported_build_in_default_value_types() {
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
    void ignore_specific_one() {
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
    void ignore_some_in_type_spec() {
        jFactory.factory(Bean.class).spec(ins ->
                ins.spec().ignore("stringValue", "boxedIntValue"));

        expect(jFactory.create(Bean.class))
                .should(": {\n" +
                        "  stringValue: null\n" +
                        "  boxedIntValue: null\n" +
                        "  intValue= 1\n" +
                        "}");
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

    @Test
    void ignore_in_class_spec() {
        expect(jFactory.createAs(IgnoreStringValue.class))
                .should(": {\n" +
                        "  stringValue: null\n" +
                        "  intValue= 1\n" +
                        "}");
    }

    @Test
    void ignore_some_in_class_spec() {
        expect(jFactory.createAs(IgnoreStringValueAndBoxedIntValue.class))
                .should(": {\n" +
                        "  stringValue: null\n" +
                        "  boxedIntValue: null\n" +
                        "  intValue= 1\n" +
                        "}");
    }
}

class DefineDefaultValue {
    public static class Bean {
        public String str;
    }

    public static class BeanHolder {
        public Bean bean;
    }

    @Test
    void define_default_value_by_type_from_bean_type_and_property_name() {
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
    void define_property_default_value_by_lambda() {
        JFactory jFactory = new JFactory();

        jFactory.factory(Bean.class).spec(ins -> ins.spec().property("str").defaultValue(() -> "from_lambda"));

        expect(jFactory.create(Bean.class)).should("str= from_lambda");
    }

    @Test
    void do_not_allow_define_sub_property_default_value() {
        JFactory jFactory = new JFactory();

        jFactory.factory(BeanHolder.class).spec(ins -> ins.spec().property("bean.str").defaultValue("hello"));

        expectRun(() -> jFactory.create(BeanHolder.class)).should("::throw.message: 'Property chain `bean.str` is not supported in the current operation'");
    }
}