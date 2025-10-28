Feature: Simple Bean Creation

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

  @import(java.util.*)
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
