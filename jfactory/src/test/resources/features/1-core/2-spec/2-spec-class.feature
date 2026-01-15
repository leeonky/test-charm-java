Feature: Define Spec in Separate Class

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

  Scenario: Spec Class - Define a Spec as a Class and Create an Object by Spec
    Given the following class definition:
      """
      import com.github.leeonky.jfactory.Spec;
      public class BeanSpec extends Spec<Bean> {}
      """
    When evaluating the following code:
      """
      jFactory.spec(BeanSpec.class).property("stringValue", "hello").create();
      """
    Then the result should be:
      """
      stringValue= hello
      """

  Scenario: Spec Name - Use a Spec by Name
    Given the following spec definition:
      """
      public class BeanSpec extends Spec<Bean> {}
      """
    And register as follows:
      """
      jFactory.register(BeanSpec.class);
      """
    When evaluating the following code:
      """
      jFactory.spec("BeanSpec").property("stringValue", "hello").create();
      """
    Then the result should be:
      """
      stringValue= hello
      """

  Scenario: Custom Spec Name - Define a New Spec Name in the Spec Class
    Given the following spec definition:
      """
      public class BeanSpec extends Spec<Bean> {
        protected String getName() { return "OneBean"; }
      }
      """
    And register as follows:
      """
      jFactory.register(BeanSpec.class);
      """
    When evaluating the following code:
      """
      jFactory.spec("OneBean").property("stringValue", "hello").create();
      """
    Then the result should be:
      """
      stringValue= hello
      """

  Scenario: Missing Spec Class - Use a Non-Existing Spec Class and Raise an Error
    When evaluating the following code:
      """
      jFactory.spec("NotExistSpec").create();
      """
    Then the result should be:
      """
      ::throw.message= "Spec `NotExistSpec` not exist"
      """

  Scenario: Trait in Spec Class - Define Traits in a Spec Class and Create an Object by Trait
    Given the following spec definition:
      """
      public class BeanSpec extends Spec<Bean> {

        @Trait
        public void helloTrait() {
          property("stringValue").value("hello");
        }
      }
      """
    When evaluating the following code:
      """
      jFactory.spec(BeanSpec.class).traits("helloTrait").create();
      """
    Then the result should be:
      """
      stringValue= hello
      """

  Scenario: Trait Name via Annotation — Use @Trait("name") instead of method name
    Given the following spec definition:
      """
      public class BeanSpec extends Spec<Bean> {

        @Trait("helloTrait") //Define Trait with a Name instead of method
        public void t1() {
          property("stringValue").value("hello");
        }
      }
      """
    When evaluating the following code:
      """
      jFactory.spec(BeanSpec.class).traits("helloTrait").create();
      """
    Then the result should be:
      """
      stringValue= hello
      """

  Scenario: Regex Trait in Spec Class - Match the Trait Name and Bind Captured Groups to Trait Method Parameters
    Given the following spec definition:
      """
      public class BeanSpec extends Spec<Bean> {

        @Trait("value_(.*)_(.*)")
        public void stringTrait(String s, int i) {
          property("stringValue").value(s);
          property("intValue").value(i);
        }
      }
      """
    When evaluating the following code:
      """
      jFactory.spec(BeanSpec.class).traits("value_hello_100").create();
      """
    Then the result should be:
      """
      : {
        stringValue= hello
        intValue= 100
      }
      """

  Scenario: Disallow generic Spec<T> registration — Type erasure prevents inferring target type; use Spec<Bean> or override Spec::getType
    Given the following spec definition:
      """
      public class BeanSpec<T> extends Spec<T> {}
      """
    When register as follows:
      """
      jFactory.register(BeanSpec.class);
      """
    Then the result should be:
      """
      ::throw.message= "Cannot guess type via generic type argument, please override Spec::getType"
      """

  Scenario: Spec Factory - Extend More Spec for an Exist Spec Class in Spec Factory Lambda
    Given the following spec definition:
      """
      public class BeanSpec extends Spec<Bean> {}
      """
    And register as follows:
      """
      jFactory.specFactory(BeanSpec.class).spec(spec -> spec
        .property("stringValue").value("from_factory"));
      """
    When evaluating the following code:
      """
      jFactory.spec(BeanSpec.class).create();
      """
    Then the result should be:
      """
      stringValue= from_factory
      """

  Scenario: Trait in Spec Factory - Extend More Traits for an Exist Spec Class in Spec Factory Lambda
    Given the following spec definition:
      """
      public class BeanSpec extends Spec<Bean> {}
      """
    And register as follows:
      """
      jFactory.specFactory(BeanSpec.class).spec("factoryTrait", spec -> spec
        .property("intValue").value(200));
      """
    When evaluating the following code:
      """
      jFactory.spec(BeanSpec.class).traits("factoryTrait").create();
      """
    Then the result should be:
      """
      intValue= 200
      """