Feature: Summary

  Background:
    And the following declarations:
      """
      JFactory jFactory = new JFactory();
      """

  Rule: Simple Creation

    Background:
      Given the following bean definition:
        """
        public class Bean {
          public String stringValue;
          public int intValue;
        }
        """

    Scenario: Object Creation - Create an Object with All Default Values
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
    Scenario: Specify Property Value - Create an Object with One or More Specified Property Values
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

    Scenario: Auto Type Conversion
      When evaluating the following code:
        """
        new JFactory().type(Bean.class)
          .property("stringValue", 100)
          .property("intValue", "200")
          .create();
        """
      Then the result should be:
        """
        : {
          stringValue= '100'
          intValue= 200
        }
        """

  Rule: Use Spec

    Background:
      Given the following bean definition:
        """
        public class Bean {
          public String stringValue;
          public int intValue;
        }
        """

    Scenario: Spec Class - Define a Spec as a Class and Create an Object by Spec
      Given the following spec definition:
        """
        public class ABean extends Spec<Bean> {}
        """
      When evaluating the following code:
        """
        jFactory.spec(ABean.class).property("stringValue", "hello").create();
        """
      Then the result should be:
        """
        stringValue= hello
        """

    Scenario: Spec Name - Use a Spec by Name
      Given the following spec definition:
        """
        public class ABean extends Spec<Bean> {}
        """
      And register as follows:
        """
        jFactory.register(ABean.class);
        """
      When evaluating the following code:
        """
        jFactory.spec("ABean").property("stringValue", "hello").create();
        """
      Then the result should be:
        """
        stringValue= hello
        """

    Scenario: Custom Spec Name - Define a New Spec Name in the Spec Class
      Given the following spec definition:
        """
        public class ABean extends Spec<Bean> {
          protected String getName() { return "OneBean"; }
        }
        """
      And register as follows:
        """
        jFactory.register(ABean.class);
        """
      When evaluating the following code:
        """
        jFactory.spec("OneBean").property("stringValue", "hello").create();
        """
      Then the result should be:
        """
        stringValue= hello
        """
