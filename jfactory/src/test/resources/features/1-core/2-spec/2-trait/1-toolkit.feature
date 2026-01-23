Feature: Trait Definition Toolkit

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

  Rule: Through Lambda for Data Type

    Scenario: Trait - Define Naming Spec as a Trait
      When register as follows:
        """
        jFactory.factory(Bean.class)
          .spec("hello", spec -> spec.property("stringValue").value("hello"));
        """
      And evaluating the following code:
        """
        jFactory.type(Bean.class).traits("hello").create();
        """
      Then the result should be:
        """
        stringValue= hello
        """

    Scenario: Regex Trait Registration — Define a Regex Trait, then Match and Bind Captured Params on Create
      When register as follows:
        """
        jFactory.factory(Bean.class)
          .spec("string_(.*)", spec -> spec.property("stringValue").value(spec.traitParam(0)));
        """
      And evaluating the following code:
        """
        jFactory.type(Bean.class).traits("string_hello").create();
        """
      Then the result should be:
        """
        stringValue= hello
        """

    Scenario: Missing Trait - Use a Non-Existing Trait and Raise an Error
      When evaluating the following code:
        """
        jFactory.type(Bean.class).traits("not_exist").create();
        """
      Then the result should be:
        """
        ::throw.message= "Trait `not_exist` not exist"
        """

    Scenario: Ambiguous Trait - Raise Error When More Than One Pattern Matched
      When register as follows:
        """
        jFactory.factory(Bean.class)
          .spec("value_(.*)", spec -> spec.property("stringValue").value(spec.traitParam(0)))
          .spec("value_(.*)_(.*)", spec -> spec.property("intValue").value(Integer.parseInt((String)spec.traitParam(1))));
        """
      And evaluating the following code:
        """
        jFactory.type(Bean.class).traits("value_hello_100").create();
        """
      Then the result should be:
        """
        ::throw.message= ```
                         Ambiguous trait pattern: value_hello_100, candidates are:
                           value_(.*)
                           value_(.*)_(.*)
                         ```
        """

  Rule: In Spec Class

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
