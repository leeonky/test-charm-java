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
        new JFactory().create(Bean.class)
        """
      Then the result should be:
        """
        : {
          stringValue= stringValue#1
          intValue= 1
        }
        """

    Scenario: Creating with specified property/properties value(s)
      Given the following bean definition:
        """
        public class Bean {
          public String stringValue;
          public int intValue;
        }
        """
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
          .create()
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
        }}).create()
        """
      Then the result should be:
        """
        : {
          stringValue= world
          intValue= 250
        }
        """
