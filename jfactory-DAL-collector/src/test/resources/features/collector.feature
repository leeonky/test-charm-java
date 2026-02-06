Feature: Collector

  Rule: Single Object for Default Type

    Background:
      Given the following declarations:
        """
        JFactory jFactory = new JFactory();
        """
      And the following bean definition:
        """
        public class Bean {
          public String value;
        }
        """
      And the following declarations:
        """
        Collector collector = new Collector(jFactory, Bean.class);
        """

    Scenario: Simple Object - Collect and Build Object with Default Type of Simple Object
      When "collector" collect and build with the following properties:
        """
        value= hello
        """
      Then the result should be:
        """
        value= hello
        """


#TODO default Type
#TODO input spec > type
#TODO default Spec
#TODO input spec > spec
#TODO default object, dal object => map
#TODO dal list > all => list
