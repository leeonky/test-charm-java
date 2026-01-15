Feature: Define Spec through Lambda for Data Type

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
