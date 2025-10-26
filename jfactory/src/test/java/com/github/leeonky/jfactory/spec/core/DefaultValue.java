package com.github.leeonky.jfactory.spec.core;

import com.github.leeonky.jfactory.JFactory;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.UUID;

import static com.github.leeonky.dal.Assertions.expect;

class ResetSequence {

    public static class Bean {
        public String str;
    }

    private final JFactory jFactory = new JFactory();

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