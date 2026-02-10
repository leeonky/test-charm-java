Feature: Collector
  Rule: Nested Object([n].b) by Default Type

    Background:
      Given the following declarations:
        """
        JFactory jFactory = new JFactory();
        """
      Given the following bean definition:
        """
        public class Product {
          public String name;
        }
        """

  Rule: Relation of Sub Element for Default Type ([0].a)

    Background:
      Given the following declarations:
        """
        JFactory jFactory = new JFactory();
        """
      Given the following bean definition:
        """
        public class Bean {
          public String value;
        }
        """

#    @import(java.util.*)
#    Scenario: Collect and Build With Element property
#      Given the following declarations:
#        """
#        Collector collector = jFactory.collector(List.class);
#        """
#      When "collector" collect and build with the following properties:
#        """
#        [0].value= hello
#        """
#      Then the result should be:
#        """
#        = [{value= hello}]
#        """

#TODO support {...}
#TODO default Type
#TODO input spec > default type
#TODO default Spec
#TODO input spec > default spec

#TODO default type Object.class, dal object => map
#TODO dal list > all => list
#TODO intently creation

#TODO error: mixed list and map
#TODO List: whole list, part of list(has negative index)
