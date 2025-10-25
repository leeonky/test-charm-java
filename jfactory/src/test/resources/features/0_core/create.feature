Feature: Create test objects using JFactory

  Rule: Creating a simple bean

    Scenario: Creating with all default values
      Given the following bean definition:
      """
      public class Bean {
        public String stringValue;
        public int intValue;
      }
      """
      When evaluating the following code:
      """
      new JFactory().type(Bean.class).property("stringValue", "input-value").create()
      """
      Then the result should be:
      """
      stringValue= input-value
      """
