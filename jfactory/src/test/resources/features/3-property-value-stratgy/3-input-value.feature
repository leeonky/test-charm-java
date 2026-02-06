Feature: Input Value

  Background:
    Given the following declarations:
      """
      JFactory jFactory = new JFactory();
      """
    And the following bean definition:
      """
      public class Bean {
        public String stringValue;
        public int intValue;
      }
      """

  Scenario: Specify Single Value - Create an Object with One or More Specified Property Values
    When evaluating the following code:
      """
      jFactory.type(Bean.class).property("intValue", 100).create()
      """
    Then the result should be:
      """
      : {
        stringValue= stringValue#1
        intValue= 100
      }
      """
    When evaluating the following code:
      """
      jFactory.type(Bean.class)
        .property("stringValue", "hello")
        .property("intValue", 43)
        .create();
      """
    Then the result should be:
      """
      : {
        stringValue= hello
        intValue= 43
      }
      """

  @import(java.util.*)
  Scenario: Specify Multiple Values - Create an Object with Specified Property Values
    When evaluating the following code:
      """
      jFactory.type(Bean.class)
        .properties(new HashMap<String, Object>(){{
          put("stringValue", "hello");
          put("intValue", 43);
        }}).create();
      """
    Then the result should be:
      """
      : {
        stringValue= hello
        intValue= 43
      }
      """

  Scenario: Customer Input - Input Value with a Custom Function
    When evaluating the following code:
      """
      jFactory.type(Bean.class).properties(new PropertyValue() {
            @Override
            public <T> Builder<T> applyToBuilder(String property, Builder<T> builder) {
                return builder.property("stringValue", "hello").property("intValue", 43);
            }
        }).create();
      """
    Then the result should be:
      """
      : {
        stringValue= hello
        intValue= 43
      }
      """

  Scenario: Convert From String – Demonstrate All Supported Types Automatically Converted From String Input
    Given the following bean class:
      """
      public class Bean {
        public int intValueFromString;
        public Integer integerValueFromString;
        public short shortValueFromString;
        public Short shortObjectValueFromString;
        public byte byteValueFromString;
        public Byte byteObjectValueFromString;
        public long longValueFromString;
        public Long longObjectValueFromString;
        public float floatValueFromString;
        public Float floatObjectValueFromString;
        public double doubleValueFromString;
        public Double doubleObjectValueFromString;
        public boolean booleanValueFromString;
        public Boolean booleanObjectValueFromString;
        public java.math.BigInteger bigIntegerValueFromString;
        public java.math.BigDecimal bigDecimalValueFromString;
        public String stringValue;
        public java.util.UUID uuidValueFromString;
        public java.time.Instant instantValueFromString;
        public java.time.LocalDate localDateValueFromString;
        public java.time.LocalTime localTimeValueFromString;
        public java.time.LocalDateTime localDateTimeValueFromString;
        public java.time.OffsetDateTime offsetDateTimeValueFromString;
        public java.time.ZonedDateTime zonedDateTimeValueFromString;
        public java.time.YearMonth yearMonthValue;
        public java.util.Date dateValueFromString;
      }
      """

    When build:
      """
      jFactory.type(Bean.class).properties(new HashMap(){{
        put("stringValue", "input-value");
        put("intValueFromString", "42");
        put("integerValueFromString", "42");
        put("shortValueFromString", "42");
        put("shortObjectValueFromString", "42");
        put("byteValueFromString", "42");
        put("byteObjectValueFromString", "42");
        put("longValueFromString", "42");
        put("longObjectValueFromString", "42");
        put("floatValueFromString", "123.06");
        put("floatObjectValueFromString", "123.06");
        put("doubleValueFromString", "123.06");
        put("doubleObjectValueFromString", "123.06");
        put("booleanValueFromString", "true");
        put("booleanObjectValueFromString", "true");
        put("bigIntegerValueFromString", "42");
        put("bigDecimalValueFromString", "123.06");
        put("uuidValueFromString", "123e4567-e89b-12d3-a456-426655440000");
        put("instantValueFromString", "2009-07-24T02:03:04Z");
        put("localDateValueFromString", "1978-05-03");
        put("localTimeValueFromString", "12:30:45");
        put("localDateTimeValueFromString", "1978-05-03T12:30:45");
        put("offsetDateTimeValueFromString", "1978-05-03T12:30:45Z");
        put("zonedDateTimeValueFromString", "1978-05-03T12:30:45Z");
        put("yearMonthValue", "1978-05");
        put("dateValueFromString", "1978-05-03");
      }}).create();
      """
    Then the result should:
      """
      : {
        stringValue= input-value
        intValueFromString= 42
        integerValueFromString= 42
        shortValueFromString= 42s
        shortObjectValueFromString= 42s
        byteValueFromString= 42y
        byteObjectValueFromString= 42y
        longValueFromString= 42l
        longObjectValueFromString= 42l
        floatValueFromString= 123.06f
        floatObjectValueFromString= 123.06f
        doubleValueFromString= 123.06d
        doubleObjectValueFromString= 123.06d
        booleanValueFromString= true
        booleanObjectValueFromString= true
        bigIntegerValueFromString= 42bi
        bigDecimalValueFromString= 123.06bd
        uuidValueFromString: '123e4567-e89b-12d3-a456-426655440000'
        instantValueFromString: '2009-07-24T02:03:04Z'
        localDateValueFromString: '1978-05-03'
        localTimeValueFromString: '12:30:45'
        localDateTimeValueFromString: '1978-05-03T12:30:45'
        offsetDateTimeValueFromString: '1978-05-03T12:30:45Z'
        zonedDateTimeValueFromString: '1978-05-03T12:30:45Z'
        yearMonthValue: '1978-05'
        dateValueFromString.toInstant: '1978-05-03T00:00:00Z'
      }
      """

  Scenario: Override Spec Value - Input Value Overrides Spec Value
    Given the following spec definition:
      """
      public class BeanSpec extends Spec<Bean> {
        @Override
        public void main() {
          property("stringValue").value("from_spec");
          property("intValue").value(99);
        }
      }
      """
    When evaluating the following code:
      """
      jFactory.spec(BeanSpec.class)
        .property("stringValue", "from_input")
        .property("intValue", 43)
        .create();
      """
    Then the result should be:
      """
      : {
        stringValue= from_input
        intValue= 43
      }
      """

  Scenario: Override Default Value - Input Value Overrides Default Value
    Given the following spec definition:
      """
      public class BeanSpec extends Spec<Bean> {
        @Override
        public void main() {
          property("stringValue").defaultValue("from_default");
          property("intValue").defaultValue(88);
        }
      }
      """
    When evaluating the following code:
      """
      jFactory.spec(BeanSpec.class)
        .property("stringValue", "from_input")
        .property("intValue", 43)
        .create();
      """
    Then the result should be:
      """
      : {
        stringValue= from_input
        intValue= 43
      }
      """
