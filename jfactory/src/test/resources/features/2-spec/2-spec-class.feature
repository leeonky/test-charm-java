Feature: Spec Class - Define Type Rules in a Separate Spec Class

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
      public class BeanSpec extends Spec<Bean> {
        public void main() {
          property("stringValue").value("hello");
        }
      }
      """
    When evaluating the following code:
      """
      jFactory.spec(BeanSpec.class).create();
      """
    Then the result should be:
      """
      stringValue= hello
      """

  Scenario: Spec Name - Use a Spec by Name
    Given the following spec definition:
      """
      public class BeanSpec extends Spec<Bean> {
        public void main() {
          property("stringValue").value("hello");
        }
      }
      """
    And register as follows:
      """
      jFactory.register(BeanSpec.class);
      """
    When evaluating the following code:
      """
      jFactory.spec("BeanSpec").create();
      """
    Then the result should be:
      """
      stringValue= hello
      """

  Scenario: Custom Spec Name - Define a New Spec Name in the Spec Class
    Given the following spec definition:
      """
      public class BeanSpec extends Spec<Bean> {
        public void main() {
          property("stringValue").value("hello");
        }
        protected String getName() { return "OneBean"; }
      }
      """
    And register as follows:
      """
      jFactory.register(BeanSpec.class);
      """
    When evaluating the following code:
      """
      jFactory.spec("OneBean").create();
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
      public class BaseSpec<T> extends Spec<T> {}
      """
    And the following spec definition:
      """
      public class BeanSpec extends BaseSpec<Bean> {}
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

  Rule: Trait in Spec Class

    Scenario: Trait in Spec Class - Define Trait in a Spec Class and Create an Object by Trait
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

    Scenario: Regex Trait in Spec Class - Match the Trait Name and Auto-Convert Captured Groups to Trait Method Parameter Types
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

    Scenario: Trait Argument Mismatch - Raise Error when Captured Groups Size is Different from Trait Method Parameter Size
      Given the following spec definition:
        """
        public class BeanSpec extends Spec<Bean> {
          @Trait("value_(.*)_(.*)")
          public void stringTrait(String s) {
          }
        }
        """
      When evaluating the following code:
        """
        jFactory.spec(BeanSpec.class).traits("value_a1_a2").create();
        """
      Then the result should be:
        """
        ::throw.message= "Trait `value_(.*)_(.*)` argument count mismatch: captured 2 groups but method expects 1"
        """

  Rule: Composition

    Background:
      Given the following bean definition:
        """
        public class Bean {
          public String value1, value2, value3, value4, value5, value6;
        }
        """

    Scenario: Merge Specs from Spec Class, Type Factory with Traits
      And the following spec definition:
        """
        public class BeanSpec extends Spec<Bean> {
          public void main() {
              property("value2").value("spec-class");
          }

          @Trait
          public void specClassTrait() {
            property("value4").value("spec-class-trait");
          }
        }
        """
      And register as follows:
        """
        jFactory.factory(Bean.class).spec(spec -> spec
          .property("value1").value("type-factory"));
        jFactory.factory(Bean.class).spec("typeFactoryTrait", spec -> spec
          .property("value5").value("type-factory-trait"));
        jFactory.specFactory(BeanSpec.class).spec(spec -> spec
          .property("value3").value("spec-factory"));
        jFactory.specFactory(BeanSpec.class).spec("specFactoryTrait", spec -> spec
          .property("value6").value("spec-factory-trait"));
        """
      When evaluating the following code:
        """
        jFactory.spec(BeanSpec.class).traits("typeFactoryTrait", "specClassTrait", "specFactoryTrait").create();
        """
      Then the result should be:
        """
        : {
          value1= type-factory
          value2= spec-class
          value3= spec-factory
          value4= spec-class-trait
          value5= type-factory-trait
          value6= spec-factory-trait
        }
        """

    Scenario: Ineffective Spec - Spec Defined in Non-Matching Spec Class Have No Effect in Spec Creation
      Given the following spec definition:
        """
        public class BeanSpec extends Spec<Bean> {}
        """
      And the following spec definition:
        """
        public class NonMatchingBeanSpec extends Spec<Bean> {
          public void main() {
              property("value1").value("any-value");
          }
        }
        """
      And register as follows:
        """
        jFactory.specFactory(NonMatchingBeanSpec.class).spec(spec -> spec
          .property("value2").value("any-value"));
        """
      When evaluating the following code:
        """
        jFactory.spec(BeanSpec.class).create();
        """
      Then the result should be:
        """
        : {
          value1= /value1.*/
          value2= /value2.*/
        }
        """

    Scenario: Ineffective Trait - Trait Defined in Non-Matching Spec Class Have No Effect in Spec Creation
      Given the following spec definition:
        """
        public class BeanSpec extends Spec<Bean> {}
        """
      And the following spec definition:
        """
        public class NonMatchingBeanSpec extends Spec<Bean> {
          @Trait
          public void trait1() {
            property("value1").value("any-value");
          }
        }
        """
      And register as follows:
        """
        jFactory.specFactory(NonMatchingBeanSpec.class).spec("trait2", spec -> spec.
          property("value1").value("any-value"));
        """
      When evaluating the following code:
        """
        jFactory.spec(BeanSpec.class).traits("trait1").create();
        """
      Then the result should be:
        """
        ::throw.message= "Trait `trait1` not exist"
        """
      When evaluating the following code:
        """
        jFactory.spec(BeanSpec.class).traits("trait2").create();
        """
      Then the result should be:
        """
        ::throw.message= "Trait `trait2` not exist"
        """

    Scenario: Specify and Replace From Input - Specify a New Spec for a Sub Object
      Given the following bean definition:
        """
        public class Container {
          public Bean bean;
        }
        """
      And the following spec definition:
        """
        public class BeanSpec extends Spec<Bean> {
          public void main() {
              property("value1").value("spec-class");
          }
        }
        """
      And the following spec definition:
        """
        public class NewBeanSpec extends Spec<Bean> {
          public void main() {
              property("value2").value("input-property");
          }
        }
        """
      And the following spec definition:
        """
        public class ContainerSpec extends Spec<Container> {
          public void main() {
              property("bean").is(BeanSpec.class);
          }
        }
        """
      And register as follows:
        """
        jFactory.register(NewBeanSpec.class);
        """
      When evaluating the following code:
        """
        jFactory.spec(ContainerSpec.class).property("bean(NewBeanSpec)", new HashMap()).create();
        """
      Then the result should be:
        """
        : {
          bean: {
            value1= /^value1.*/
            value2= input-property
          }
        }
        """

  Rule: Property Spec Precedence

    Background:
      Given the following bean definition:
        """
        public class Bean {
          public String value;
        }
        """

    Scenario: Spec-Class > Type-Factory - Specs in Spec Class Takes Precedence over Type Factor
      Given the following spec definition:
        """
        public class BeanSpec extends Spec<Bean> {
          public void main() {
              property("value").value("spec-class");
          }
        }
        """
      And register as follows:
        """
        jFactory.factory(Bean.class).spec(spec -> spec
          .property("value").value("type-factory"));
        """
      When evaluating the following code:
        """
        jFactory.spec(BeanSpec.class).create();
        """
      Then the result should be:
        """
        value= spec-class
        """

    Scenario: Spec Factory > Spec Class - Specs in Spec Factory Takes Precedence over Spec Class
      Given the following spec definition:
        """
        public class BeanSpec extends Spec<Bean> {
          public void main() {
            property("value").value("spec-class");
          }
        }
        """
      And register as follows:
        """
        jFactory.specFactory(BeanSpec.class).spec(spec -> spec
          .property("value").value("spec-factory"));
        """
      When evaluating the following code:
        """
        jFactory.createAs(BeanSpec.class);
        """
      Then the result should be:
        """
        value= spec-factory
        """

    Scenario: Type-Factory Trait > Spec-Factory Spec - Lowest Priority Trait Takes Precedence over Highest Priority Spec
      Given the following spec definition:
        """
        public class BeanSpec extends Spec<Bean> {}
        """
      And register as follows:
        """
        jFactory.factory(Bean.class).spec("trait", spec -> spec
          .property("value").value("type-factory-trait"));
        jFactory.specFactory(BeanSpec.class).spec(spec -> spec
          .property("value").value("spec-factory"));
        """
      When evaluating the following code:
        """
        jFactory.spec(BeanSpec.class).traits("trait").create();
        """
      Then the result should be:
        """
        value= type-factory-trait
        """

    Scenario: Spec-Class Trait > Spec-Factory Spec - Middle Priority Trait Takes Precedence over Highest Priority Spec
      Given the following spec definition:
        """
        public class BeanSpec extends Spec<Bean> {
          @Trait
          public void trait() {
            property("value").value("spec-class-trait");
          }
        }
        """
      And register as follows:
        """
        jFactory.specFactory(BeanSpec.class).spec(spec -> spec
          .property("value").value("spec-factory"));
        """
      When evaluating the following code:
        """
        jFactory.spec(BeanSpec.class).traits("trait").create();
        """
      Then the result should be:
        """
        value= spec-class-trait
        """

    Scenario: Spec-Factory Trait > Spec-Factory Spec - Highest Priority Trait Takes Precedence over Highest Priority Spec
      Given the following spec definition:
        """
        public class BeanSpec extends Spec<Bean> {}
        """
      And register as follows:
        """
        jFactory.specFactory(BeanSpec.class).spec(spec -> spec
          .property("value").value("spec-factory"));
        jFactory.specFactory(BeanSpec.class).spec("trait", spec -> spec
          .property("value").value("spec-factory-trait"));
        """
      When evaluating the following code:
        """
        jFactory.spec(BeanSpec.class).traits("trait").create();
        """
      Then the result should be:
        """
        value= spec-factory-trait
        """

  Rule: Trait Precedence

    Background:
      Given the following bean definition:
        """
        public class Bean {
          public String value;
        }
        """

    Scenario: Spec-Class > Type-Factory - The Same-Named Trait in Spec Class Takes Precedence over Type Factory
      Given the following spec definition:
        """
        public class BeanSpec extends Spec<Bean> {
          @Trait
          public void trait() {
              property("value").value("spec-class");
          }
        }
        """
      And register as follows:
        """
        jFactory.factory(Bean.class).spec("trait", spec -> spec
          .property("value").value("type-factory"));
        """
      When evaluating the following code:
        """
        jFactory.spec(BeanSpec.class).traits("trait").create();
        """
      Then the result should be:
        """
        value= spec-class
        """

    Scenario: Spec-Factory > Spec-Class - The Same-Named Trait in Spec Factory Takes Precedence over Spec Class
      Given the following spec definition:
        """
        public class BeanSpec extends Spec<Bean> {
          @Trait
          public void trait() {
            property("value").value("spec-class");
          }
        }
        """
      And register as follows:
        """
        jFactory.specFactory(BeanSpec.class).spec("trait", spec -> spec
          .property("value").value("spec-factory"));
        """
      When evaluating the following code:
        """
        jFactory.spec(BeanSpec.class).traits("trait").create();
        """
      Then the result should be:
        """
        value= spec-factory
        """

    Scenario: Whole-Trait Replacement - The Same-Named Trait Resolution Replaces the Whole Trait Definition, Rather than Merging by Property
      Given the following bean definition:
        """
        public class Bean {
          public String value1, value2;
        }
        """
      Given the following spec definition:
        """
        @Global
        public class BeanSpec extends Spec<Bean> {
          @Trait
          public void trait() {
              property("value1").value("trait");
          }
        }
        """
      And register as follows:
        """
        jFactory.factory(Bean.class).spec("trait", spec -> spec
          .property("value1").value("type-factory-1")
          .property("value2").value("type-factory-2"));
        jFactory.register(BeanSpec.class);
        """
      When evaluating the following code:
        """
        jFactory.type(Bean.class).traits("trait").create();
        """
      Then the result should be:
        """
        : {
          value1= trait
          value2= /^value2.*/
        }
        """

    Scenario: Specify and Replace From Input - Specify a New Spec for a Sub Object
      Given the following bean definition:
        """
        public class Container {
          public Bean bean;
        }
        """
      And the following spec definition:
        """
        public class BeanSpec extends Spec<Bean> {

          @Trait
          public void v1() {
            property("value").value("spec-class");
          }
        }
        """
      And the following spec definition:
        """
        public class NewBeanSpec extends Spec<Bean> {

          @Trait
          public void v1() {
            property("value").value("input-property");
          }
        }
        """
      And the following spec definition:
        """
        public class ContainerSpec extends Spec<Container> {
          public void main() {
              property("bean").is(BeanSpec.class);
          }
        }
        """
      And register as follows:
        """
        jFactory.register(NewBeanSpec.class);
        """
      When evaluating the following code:
        """
        jFactory.spec(ContainerSpec.class).property("bean(v1 NewBeanSpec)", new HashMap()).create();
        """
      Then the result should be:
        """
        bean.value= input-property
        """
