Feature: Default Value

  Background:
    Given the following declarations:
      """
      JFactory jFactory = new JFactory();
      """

  Rule: By Type Sequence

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
          public java.time.YearMonth yearMonth;
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
          yearMonth: '1996-02'
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
          yearMonth: '1996-03'
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
        jFactory.setSequenceStart(100);
        jFactory.create(Bean.class); // sequence increased
        jFactory.resetSequence();
        """
      When evaluating the following code:
        """
        jFactory.create(Bean.class);
        """
      Then the result should be:
        """
        str= 'str#100'
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

    Scenario Outline: Sequence Wrap Around - <type> Property Wraps Around From Max to Min Value
      Given the following bean definition:
        """
        public class Bean {
          public <type> value;
        }
        """
      And register as follows:
        """
        jFactory.setSequenceStart(<maxValue>);
        """
      When evaluating the following code:
        """
        jFactory.create(Bean.class);
        """
      Then the result should be:
        """
        value= <first>
        """
      When evaluating the following code:
        """
        jFactory.create(Bean.class);
        """
      Then the result should be:
        """
        value= <second>
        """

      Examples:
        | type  | maxValue          | first       | second       |
        | byte  | Byte.MAX_VALUE    | 127y        | -128y        |
        | short | Short.MAX_VALUE   | 32767s      | -32768s      |
        | int   | Integer.MAX_VALUE | 2147483647  | -2147483648  |
        | long  | Integer.MAX_VALUE | 2147483647L | -2147483648L |

    Scenario: Sequence Wrap Around - Enum Property Cycles Through All Values
      Given the following bean definition:
        """
        public class Bean {
          public E value;

          public enum E {A, B}
        }
        """
      When evaluating the following code:
        """
        jFactory.create(Bean.class);
        """
      Then the result should be:
        """
        value: A
        """
      When evaluating the following code:
        """
        jFactory.create(Bean.class);
        """
      Then the result should be:
        """
        value: B
        """
      When evaluating the following code:
        """
        jFactory.create(Bean.class);
        """
      Then the result should be:
        """
        value: A
        """

    Scenario: Sequence Wrap Around - Boolean Property Alternates Between True and False
      Given the following bean definition:
        """
        public class Bean {
          public boolean value;
        }
        """
      When evaluating the following code:
        """
        jFactory.create(Bean.class);
        """
      Then the result should be:
        """
        value= true
        """
      When evaluating the following code:
        """
        jFactory.create(Bean.class);
        """
      Then the result should be:
        """
        value= false
        """
      When evaluating the following code:
        """
        jFactory.create(Bean.class);
        """
      Then the result should be:
        """
        value= true
        """

  Rule: Define Custom Default Values

    Scenario: Other Type Default Value - Returns Null for Types Without Custom Default Value Factory
      Given the following bean definition:
        """
        public class Bean {
          public AnyType anyType;
        }
        """
      And the following bean definition:
        """
        public class AnyType {}
        """
      When evaluating the following code:
        """
        jFactory.create(Bean.class);
        """
      Then the result should be:
        """
        anyType= null
        """

    @import(com.github.leeonky.util.*)
    Scenario: Custom Default Value Factory - Define a Custom Default Value by Type via DefaultValueFactory
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
            return beanType.getSimpleName() + "_" + objectProperty.getProperty().getName();
          }
        });
        """
      And evaluating the following code:
        """
        jFactory.create(Bean.class);
        """
      Then the result should be:
        """
        str= Bean_str
        """

    Scenario: Custom Property Default Value - Define Default Value for a Property in Class Spec by Object Creation Sequence
      Given the following bean definition:
        """
        public class Bean {
          public String str;
        }
        """
      And the following spec definition:
        """
        public class BeanSpec extends Spec<Bean> {
          @Override
          public void main() {
            property("str").defaultValue("hello_" + instance().getSequence());
          }
        }
        """
      When evaluating the following code:
        """
        jFactory.createAs(BeanSpec.class);
        """
      Then the result should be:
        """
        str= hello_1
        """

    Scenario: Lazy Mode Default Value - Define Default Value for a Property in Class Spec in Lazy Mode by Lambda Expression
      Given the following bean definition:
        """
        public class Bean {
          public String str;
        }
        """
      And the following spec definition:
        """
        public class BeanSpec extends Spec<Bean> {
          @Override
          public void main() {
            property("str").defaultValue(() -> "hello_lazy");
          }
        }
        """
      When evaluating the following code:
        """
        jFactory.createAs(BeanSpec.class);
        """
      Then the result should be:
        """
        str= hello_lazy
        """

    Scenario: Null Default Value — Treat Null as a Literal Null Default, not as a Null Supplier<Object>
      Given the following bean definition:
        """
        public class Bean {
          public String str;
        }
        """
      And the following spec definition:
        """
        public class BeanSpec extends Spec<Bean> {
          @Override
          public void main() {
            property("str").defaultValue(null);
          }
        }
        """
      When evaluating the following code:
        """
        jFactory.createAs(BeanSpec.class);
        """
      Then the result should be:
        """
        str= null
        """

    Scenario: Sub Property Default Value - Not Allowed to Define Sub Property Default Value
      Given the following bean definition:
        """
        public class Bean {
          public String str;
        }
        """
      And the following bean definition:
        """
        public class BeanHolder {
          public Bean bean;
        }
        """
      And the following spec definition:
        """
        public class BeanHolderSpec extends Spec<BeanHolder> {
          @Override
          public void main() {
            property("bean.str").defaultValue("hello");
          }
        }
        """
      When evaluating the following code:
        """
        jFactory.createAs(BeanHolderSpec.class);
        """
      Then the result should be:
        """
        ::throw.message: 'Property chain `bean.str` is not supported in the current operation'
        """

    Scenario: Rotate values - Define Default Value by Rotating Given Values
      Given the following bean definition:
        """
        public class Bean {
          public String str;
        }
        """
      And the following spec definition:
        """
        public class BeanSpec extends Spec<Bean> {
          @Override
          public void main() {
            property("str").defaultValue(instance().rotate("A", "B", "C"));
          }
        }
        """
      When evaluating the following code:
        """
        jFactory.createAs(BeanSpec.class);
        """
      Then the result should be:
        """
        str= A
        """
      When evaluating the following code:
        """
        jFactory.createAs(BeanSpec.class);
        """
      Then the result should be:
        """
        str= B
        """
      When evaluating the following code:
        """
        jFactory.createAs(BeanSpec.class);
        """
      Then the result should be:
        """
        str= C
        """
      When evaluating the following code:
        """
        jFactory.createAs(BeanSpec.class);
        """
      Then the result should be:
        """
        str= A
        """

  Rule: Ignore Default Value Generation

    Scenario: Ignore All Properties - All Default Values Become Null or Default Primitive Values
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
      When register as follows:
        """
        jFactory.ignoreDefaultValue(p -> true);
        """
      And evaluating the following code:
        """
        jFactory.create(Bean.class);
        """
      Then the result should be:
        """
        = {
          stringValue= null
          intValue= 0
          boxedIntValue= null
          shortValue= 0s
          boxedShortValue= null
          byteValue= 0y
          boxedByteValue= null
          longValue= 0L
          boxedLongValue= null
          floatValue= 0.0f
          boxedFloatValue= null
          doubleValue= 0.0d
          boxedDoubleValue= null
          boolValue= false
          boxedBoolValue= null
          bigInt= null
          bigDec= null
          uuid: null
          date: null
          instant: null
          localDate: null
          localTime: null
          localDateTime: null
          offsetDateTime: null
          zonedDateTime: null
          enumValue: null
        }
        """

    Scenario: Ignore Specific Property - Ignore Default Value Generation by Property Name
      Given the following bean definition:
        """
        public class Bean {
          public String stringValue;
          public int intValue;
        }
        """
      When register as follows:
        """
        jFactory.ignoreDefaultValue(p -> p.getName().equals("stringValue"));
        """
      And evaluating the following code:
        """
        jFactory.create(Bean.class);
        """
      Then the result should be:
        """
        : {
          stringValue: null
          intValue= 1
        }
        """

    Scenario: Ignore one Property - Ignore Default Value Generation in Spec Class
      Given the following bean definition:
        """
        public class Bean {
          public String stringValue;
          public int intValue;
        }
        """
      And the following spec definition:
        """
        public class IgnoreStringValue extends Spec<Bean> {
          @Override
          public void main() {
            property("stringValue").ignore();
          }
        }
        """
      When evaluating the following code:
        """
        jFactory.createAs(IgnoreStringValue.class);
        """
      Then the result should be:
        """
        : {
          stringValue: null
          intValue= 1
        }
        """

    Scenario: Ignore Multiple Properties - Ignore Multiple Properties in Spec Class
      Given the following bean definition:
        """
        public class Bean {
          public String stringValue;
          public int intValue;
          public Integer boxedIntValue;
        }
        """
      And the following spec definition:
        """
        public class IgnoreStringValueAndBoxedIntValue extends Spec<Bean> {
          @Override
          public void main() {
            ignore("stringValue", "boxedIntValue");
          }
        }
        """
      When evaluating the following code:
        """
        jFactory.createAs(IgnoreStringValueAndBoxedIntValue.class);
        """
      Then the result should be:
        """
        : {
          stringValue: null
          boxedIntValue: null
          intValue= 1
        }
        """
