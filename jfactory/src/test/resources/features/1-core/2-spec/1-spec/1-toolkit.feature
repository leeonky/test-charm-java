Feature: Spec Definition Toolkit

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

    Scenario: Base Spec for a Type - Define Default Base Rules for a Type and Create an Object with the Base Spec
      When register as follows:
        """
        jFactory.factory(Bean.class).spec(spec -> spec
          .property("stringValue").value("hello")
          .property("intValue").value(100)
        );
        """
      And evaluating the following code:
        """
        jFactory.create(Bean.class);
        """
      Then the result should be:
        """
        : {
          stringValue= hello
          intValue= 100
        }
        """

  Rule: In Separate Class

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

  Rule: Global Spec Class

    Scenario: Global Spec Class - Define a Global Spec Class to Apply to All Types
      Given the following spec definition:
        """
        @Global
        public class GlobalBeanSpec extends Spec<Bean> {
          public void main() {
            property("stringValue").value("globalHello");
          }
        }
        """
      And register as follows:
        """
        jFactory.register(GlobalBeanSpec.class);
        """
      When evaluating the following code:
        """
        jFactory.type(Bean.class).create();
        """
      Then the result should be:
        """
        stringValue= globalHello
        """

    Scenario: Remove Global Spec Class - Remove a Global Spec Class from the Factory
      Given the following spec definition:
        """
        @Global
        public class GlobalBeanSpec extends Spec<Bean> {
          public void main() {
            property("stringValue").value("globalHello");
          }
        }
        """
      And register as follows:
        """
        jFactory.register(GlobalBeanSpec.class);
        jFactory.removeGlobalSpec(Bean.class);
        """
      When evaluating the following code:
        """
        jFactory.type(Bean.class).create();
        """
      Then the result should be:
        """
        stringValue= /^stringValue.*/
        """

    Scenario: Duplicated Global Spec Class - Do not allow Duplicated Global Spec Class Registration
      Given the following spec definition:
        """
        @Global
        public class GlobalBeanSpec1 extends Spec<Bean> {}
        """
      And the following spec definition:
        """
        @Global
        public class GlobalBeanSpec2 extends Spec<Bean> {}
        """
      When register as follows:
        """
        jFactory.register(GlobalBeanSpec1.class);
        jFactory.register(GlobalBeanSpec2.class);
        """
      And evaluating the following code:
        """
        jFactory.createAs(GlobalBeanSpec2.class);
        """
      Then the result should be:
        """
        ::throw.message= "More than one @Global Spec class `GlobalBeanSpec1` and `GlobalBeanSpec2`"
        """

    Scenario: Type Factory Shadowed - Type Factory Returns the Global Spec Class Factory after the Global Spec Class was Registered
      Given the following spec definition:
        """
        @Global
        public class GlobalBeanSpec extends Spec<Bean> {
          public void main() {
            property("stringValue").value("globalHello");
          }
        }
        """
      And register as follows:
        """
        jFactory.register(GlobalBeanSpec.class);
        jFactory.factory(Bean.class).spec(spec -> spec
          .property("stringValue").value("from_type_factory"));
        """
      When evaluating the following code:
        """
        jFactory.create(Bean.class);
        """
      Then the result should be:
        """
        stringValue= from_type_factory
        """
      And register as follows:
        """
        jFactory.removeGlobalSpec(Bean.class);
        """
      When evaluating the following code:
        """
        jFactory.create(Bean.class);
        """
      Then the result should be:
        """
        stringValue= /^stringValue.*/
        """

