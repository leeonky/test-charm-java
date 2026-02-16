Feature: Super => Sub
  Specify property to Sub Type

  Background:
    Given the following declarations:
      """
      JFactory jFactory = new JFactory();
      """
    Given the following bean definition:
      """
      public class Super {}
      """
    And the following bean definition:
      """
      public class Sub extends Super{
        public String value1, value2;
      }
      """
    And the following bean definition:
      """
      public class Bean {
        public Super object;
        public String name;
      }
      """
    And the following spec definition:
      """
      public class SubSpec extends Spec<Sub> {
        @Trait
        public void v2() {
          property("value2").value("v2");
        }
      }
      """
    And register as follows:
      """
      jFactory.register(SubSpec.class);
      """

  Rule: In Parent Spec by is

    Background:
      Given the following spec definition:
        """
        public class BeanSpec extends Spec<Bean> {
          public void main() {
            property("object").is(SubSpec.class);
          }
        }
        """

    Scenario: Create with Default Sub
      When evaluating the following code:
        """
        jFactory.spec(BeanSpec.class).create();
        """
      Then the result should be:
        """
        object: {
          value1= /^value1.*/
          value2= /^value2.*/
          class.simpleName= Sub
        }
        """

    Scenario: Create with Specified Default Sub
      When evaluating the following code:
        """
        jFactory.spec(BeanSpec.class).property("object", new java.util.HashMap()).create();
        """
      Then the result should be:
        """
        object: {
          value1= /^value1.*/
          value2= /^value2.*/
          class.simpleName= Sub
        }
        """

    Scenario: Create with Sub Property
      When evaluating the following code:
        """
        jFactory.spec(BeanSpec.class).property("object.value1", "v1").create();
        """
      Then the result should be:
        """
        object: {
          value1= v1
          value2= /^value2.*/
          class.simpleName= Sub
        }
        """

    Scenario: Create with Sub Property Query
      Given register as follows:
        """
        jFactory.type(Sub.class).property("value1", "v1").property("value2", "v2").create();
        """
      When evaluating the following code:
        """
        jFactory.spec(BeanSpec.class).property("object.value1", "v1").create();
        """
      Then the result should be:
        """
        object: {
          value1= v1
          value2= v2
          class.simpleName= Sub
        }
        """

    Scenario: Query with Sub Property
      Given register as follows:
        """
        Sub sub = jFactory.type(Sub.class).property("value1", "v1").property("value2", "v2").create();
        jFactory.type(Bean.class).property("object", sub).create();
        """
      When evaluating the following code:
        """
        jFactory.spec(BeanSpec.class).property("object.value1", "v1").query();
        """
      Then the result should be:
        """
        object: {
          value1= v1
          value2= v2
          class.simpleName= Sub
        }
        """

    Scenario: Use Trait in SubSpec
      Given the following spec definition:
        """
        public class BeanSpec extends Spec<Bean> {
          public void main() {
            property("object").is("v2", "SubSpec");
          }
        }
        """
      When evaluating the following code:
        """
        jFactory.spec(BeanSpec.class).create();
        """
      Then the result should be:
        """
        object: {
          value1= /^value1.*/
          value2= v2
          class.simpleName= Sub
        }
        """

#  Rule: In Parent Spec by apply
