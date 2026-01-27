Feature: Type Default Spec - Define Default Rules for a Type

  Background:
    Given the following declarations:
      """
      JFactory jFactory = new JFactory();
      """
    Given the following bean definition:
      """
      public class Bean {
        public String stringValue;
        public int intValue;
      }
      """

  Scenario: Create Object Using the Type Default Spec
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

  Rule: Trait

    Scenario: Type-Factory Trait - Define Naming Spec as a Trait in Type Factory
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

    Scenario: Missing Trait - Use a Non-Existing Trait and Raise an Error
      When evaluating the following code:
        """
        jFactory.type(Bean.class).traits("not_exist").create();
        """
      Then the result should be:
        """
        ::throw.message= "Trait `not_exist` not exist"
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

  Rule: Composition

    Scenario: Merge Specs with Trait
      Given the following bean definition:
        """
        public class Bean {
          public String value1, value2, value3;
        }
        """
      And register as follows:
        """
        jFactory.factory(Bean.class).spec(spec -> spec
          .property("value1").value("type-factory"));
        jFactory.factory(Bean.class).spec("trait", spec -> spec
          .property("value2").value("trait"));
        """
      When evaluating the following code:
        """
        jFactory.type(Bean.class).traits("trait").create();
        """
      Then the result should be:
        """
        : {
          value1= type-factory
          value2= trait
        }
        """

  Rule: Property-Level Spec Precedence

    Scenario: Trait > Spec - The Same-Named Property Specs in Trait Takes Precedence over Type Spec
      Given the following bean definition:
        """
        public class Bean {
          public String value;
        }
        """
      Given the following spec definition:
        """
        public class BeanSpec extends Spec<Bean> {}
        """
      And register as follows:
        """
        jFactory.factory(Bean.class).spec("trait", spec -> spec
            .property("value").value("trait"));
        jFactory.factory(Bean.class).spec(spec -> spec
            .property("value").value("spec"));
        """
      When evaluating the following code:
        """
        jFactory.spec(BeanSpec.class).traits("trait").create();
        """
      Then the result should be:
        """
        value= trait
        """

    Scenario: Later Trait > Earlier Trait - Traits Listed Later Takes Precedence over Earlier Traits
      Given the following bean definition:
        """
        public class Bean {
          public String value;
        }
        """
      And register as follows:
        """
        jFactory.factory(Bean.class)
          .spec("trait1", spec -> spec.property("value").value("trait1"))
          .spec("trait2", spec -> spec.property("value").value("trait2"));
        """
      When evaluating the following code:
        """
        jFactory.spec(BeanSpec.class).traits("trait1", "trait2").create();
        """
      Then the result should be:
        """
        value= trait2
        """
      When evaluating the following code:
        """
        jFactory.spec(BeanSpec.class).traits("trait2", "trait1").create();
        """
      Then the result should be:
        """
        value= trait1
        """

    Scenario: Replace by Property - When trait and spec both define rules for the same property, only that property’s rules are replaced (other properties remain unchanged)
      Given the following bean definition:
        """
        public class Bean {
          public String value1, value2;
        }
        """
      And register as follows:
        """
        jFactory.factory(Bean.class)
          .spec(spec -> spec
            .property("value1").value("type-factory-1")
            .property("value2").value("type-factory-2"))
          .spec("trait", spec -> spec
            .property("value1").value("type-factory-trait"));
        """
      When evaluating the following code:
        """
        jFactory.type(Bean.class).traits("trait").create();
        """
      Then the result should be:
        """
        : {
          value1= type-factory-trait
          value2= type-factory-2
        }
        """

  Rule: Trait Precedence

    Scenario: Exactly > Regex - An Exact Trait Name Match Takes Precedence Over a Regex Trait
      Given register as follows:
        """
        jFactory.factory(Bean.class)
          .spec("trait", spec -> spec
            .property("stringValue").value("exactly"))
          .spec("trai.*", spec -> spec
            .property("stringValue").value("regex"));
        """
      When evaluating the following code:
        """
        jFactory.type(Bean.class).traits("trait").create();
        """
      Then the result should be:
        """
        stringValue= exactly
        """
