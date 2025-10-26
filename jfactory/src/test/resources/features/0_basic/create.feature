Feature: Create Test Objects Using JFactory

  Rule: Simple Bean Creation

    Background:
      Given the following bean definition:
        """
        public class Bean {
          public String stringValue;
          public int intValue;
        }
        """

    Scenario: Simple Creation - Create an Object with All Default Values
      When evaluating the following code:
        """
        new JFactory().create(Bean.class);
        """
      Then the result should be:
        """
        : {
          stringValue= stringValue#1
          intValue= 1
        }
        """

    Scenario: Property-Based Creation - Create an Object with One or More Specified Property Values
      When evaluating the following code:
        """
        new JFactory().type(Bean.class).property("intValue", 100).create()
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
        new JFactory().type(Bean.class)
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
      When evaluating the following code:
        """
        new JFactory().type(Bean.class).properties(new HashMap<String, Object>() {{
          put("stringValue", "world");
          put("intValue", 250);
        }}).create();
        """
      Then the result should be:
        """
        : {
          stringValue= world
          intValue= 250
        }
        """

  Rule: Default Value Handling

    Background:
      Given the following declarations:
        """
        JFactory jFactory = new JFactory();
        """

    Scenario: Default Value Types - Demonstrate All Supported Default Value Types With Auto-Incremented Values
      Given the following bean definition:
        """
        import java.math.*;
        import java.util.*;
        public class Bean {
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
        """
      When evaluating the following code:
        """
        jFactory.create(Bean.class);
        """
      Then the result should be:
        """
        : {
          stringValue= stringValue#1
          intValue= 1
          boxedIntValue= 1
          shortValue= 1s
          boxedShortValue= 1s
          byteValue= 1y
          boxedByteValue= 1y
          longValue= 1L
          boxedLongValue= 1L
          floatValue= 1.0f
          boxedFloatValue= 1.0f
          doubleValue= 1.0d
          boxedDoubleValue= 1.0d
          boolValue= true
          boxedBoolValue= true
          bigInt= 1bi
          bigDec= 1bd
          uuid: '00000000-0000-0000-0000-000000000001'
          date.toInstant: '1996-01-24T00:00:00Z'
          instant: '1996-01-23T00:00:01Z'
          localDate: '1996-01-24'
          localTime: '00:00:01'
          localDateTime: '1996-01-23T00:00:01'
          offsetDateTime.toInstant: '1996-01-23T00:00:01Z'
          zonedDateTime.toInstant: '1996-01-23T00:00:01Z'
          enumValue: A
        }
        """
      When evaluating the following code:
        """
        jFactory.create(Bean.class);
        """
      Then the result should be:
        """
        : {
          stringValue= stringValue#2
          intValue= 2
          boxedIntValue= 2
          shortValue= 2s
          boxedShortValue= 2s
          byteValue= 2y
          boxedByteValue= 2y
          longValue= 2L
          boxedLongValue= 2L
          floatValue= 2.0f
          boxedFloatValue= 2.0f
          doubleValue= 2.0d
          boxedDoubleValue= 2.0d
          boolValue= false
          boxedBoolValue= false
          bigInt= 2bi
          bigDec= 2bd
          uuid: '00000000-0000-0000-0000-000000000002'
          date.toInstant: '1996-01-25T00:00:00Z'
          instant: '1996-01-23T00:00:02Z'
          localDate: '1996-01-25'
          localTime: '00:00:02'
          localDateTime: '1996-01-23T00:00:02'
          offsetDateTime.toInstant: '1996-01-23T00:00:02Z'
          zonedDateTime.toInstant: '1996-01-23T00:00:02Z'
          enumValue: B
        }
        """

    Scenario: Sequence Reset - Reset Sequence Back to Its Initial Value
      Given the following bean definition:
        """
        public class Bean {
          public String str;
        }
        """
      And register as follows:
        """
        jFactory.create(Bean.class); // sequence increased
        jFactory.resetSequence();
        """
      When evaluating the following code:
        """
        jFactory.create(Bean.class);
        """
      Then the result should be:
        """
        str= 'str#1'
        """

    Scenario: Sequence Start - Each Created Object Is Numbered Starting from This Value
      Given the following bean definition:
        """
        public class Bean {
          public String str;
        }
        """
      And register as follows:
        """
        jFactory.setSequenceStart(100);
        """
      When evaluating the following code:
        """
        jFactory.create(Bean.class);
        """
      Then the result should be:
        """
        str= 'str#100'
        """

    Scenario: Custom Default Value Strategy - Define a Custom Default Value Factory by Type
      Given the following bean definition:
        """
        public class Bean {
          public String str;
        }
        """
      When register as follows:
        """
        jFactory.registerDefaultValueFactory(String.class, new DefaultValueFactory<String>() {
          @Override
            public <T> String create(BeanClass<T> beanType, ObjectProperty<T> objectProperty) {
              return  "hello-" + beanType.getSimpleName();
            }
          });
        """
      And evaluating the following code:
        """
        jFactory.create(Bean.class);
        """
      Then the result should be:
        """
        str= hello-Bean
        """

    Scenario: Default Value Skipping - Support Ignoring Default Value Generation
      Given the following bean definition:
        """
        public class Bean {
          public String str;
        }
        """
      When register as follows:
        """
        jFactory.ignoreDefaultValue(propertyWriter -> "str".equals(propertyWriter.getName()));
        """
      And evaluating the following code:
        """
        jFactory.type(Bean.class).create();
        """
      Then the result should be:
        """
        str= null
        """

  Rule: Specify a custom constructor

    Background:
      Given the following bean definition:
        """
        public class Bean {
          private int i;
          public Bean(int i) { this.i = i; }
          public int getI() { return i; }
        }
        """
      Given the following declarations:
        """
        JFactory jFactory = new JFactory();
        """

    Scenario: customize constructor - create bean use customer constructor
      And register as follows:
        """
        jFactory.factory(Bean.class).constructor(arg -> new Bean(100));
        """
      When evaluating the following code:
        """
        jFactory.type(Bean.class).create();
        """
      Then the result should be:
        """
        i= 100
        """
