Feature: Custom Default Value

  Background:
    Given the following declarations:
      """
      JFactory jFactory = new JFactory();
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

#      TODO move to another feature
    Scenario: Value Override - Prioritize Input Property Values Over Any Defaults
      Given the following bean definition:
        """
        public class Bean {
          public String str1, str2, str3;
        }
        """
      And register as follows:
        """
        jFactory.factory(Bean.class).spec(spec -> spec
          .property("str1").ignore()
          .property("str2").defaultValue("any")
        );
        """
      When evaluating the following code:
        """
        jFactory.type(Bean.class)
          .property("str1", "hello")
          .property("str2", "world")
          .property("str3", "!")
          .create();
        """
      Then the result should be:
        """
        : {str1= hello, str2= world, str3= '!'}
        """
