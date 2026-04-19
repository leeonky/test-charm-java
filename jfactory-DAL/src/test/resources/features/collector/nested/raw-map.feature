Feature: Nested Raw Map
  Create Map by Collector

  Background:
    Given the following declarations:
      """
      JFactory jFactory = new JFactory();
      """
    Given the following declarations:
      """
      Collector collector = jFactory.collector();
      """

  Rule: Value is a Bean

    Background:
      Given the following bean definition:
        """
        public class Category {
          public int order;
        }
        """
      Given the following spec definition:
        """
        public class CategorySpec extends Spec<Category> {}
        """
      And register as follows:
        """
        jFactory.register(CategorySpec.class);
        """

    Scenario: Specify Child With Spec and Properties
      When "collector" collect and build with the following properties:
        """
        = {
          category(CategorySpec): {
            order= 1
          }
        }
        """
      Then the result should be:
        """
        : {
          category= {
            order= 1,
            ::object.class.name= Category
          }
          ::object.class.name= java.util.LinkedHashMap
        }
        """

    Scenario: Specify Child With Spec and Default
      When "collector" collect and build with the following properties:
        """
        = {
          category(CategorySpec): {...}
        }
        """
      Then the result should be:
        """
        : {
          category= {
            order= 1,
            ::object.class.name= Category
          }
          ::object.class.name= java.util.LinkedHashMap
        }
        """

    Scenario: Specify Child With Spec Intently Creation and Properties
      When "collector" collect and build with the following properties:
        """
        = {
          category(CategorySpec)!: {
            order= 42
          }
        }
        """
      Then the result should be:
        """
        : {
          category= {
            order= 42,
            ::object.class.name= Category
          }
          ::object.class.name= java.util.LinkedHashMap
        }
        """

    Scenario: Specify Child With Spec Intently Creation and Default
      When "collector" collect and build with the following properties:
        """
        = {
          category(CategorySpec)!: {...}
        }
        """
      Then the result should be:
        """
        : {
          category= {
            order= 1,
            ::object.class.name= Category
          }
          ::object.class.name= java.util.LinkedHashMap
        }
        """

  Rule: Value is a List

    Background:
      Given the following spec definition:
        """
        public class StringList extends Spec<String[]> { }
        """
      And register as follows:
        """
        jFactory.register(StringList.class);
        """

    Scenario: Specify Child With Spec and Properties
      When "collector" collect and build with the following properties:
        """
        = {
          items(StringList): [hello world]
        }
        """
      Then the result should be:
        """
        : {
          items= [hello world]
          items.class.simpleName= 'String[]',
          ::object.class.name= java.util.LinkedHashMap
        }
        """

    Scenario: Specify Child With Spec and Default
      When "collector" collect and build with the following properties:
        """
        = {
          items(StringList): {...}
        }
        """
      Then the result should be:
        """
        : {
          items= []
          items.class.simpleName= 'String[]',
          ::object.class.name= java.util.LinkedHashMap
        }
        """

  Rule: Value is a Raw List

    Scenario: List
      When "collector" collect and build with the following properties:
        """
        = {
          items= [hello world]
        }
        """
      Then the result should be:
        """
        : {
          items= [hello world]
          items.class.simpleName= ArrayList,
          ::object.class.simpleName= LinkedHashMap
        }
        """

    Scenario: Empty List
      When "collector" collect and build with the following properties:
        """
        = {
          items= []
        }
        """
      Then the result should be:
        """
        : {
          items= []
          items.class.simpleName= ArrayList,
          ::object.class.simpleName= LinkedHashMap
        }
        """

  Rule: Value is a Raw Map

    Scenario: Specify Child Properties
      When "collector" collect and build with the following properties:
        """
        = {
          sub= {
            key= value
          }
        }
        """
      Then the result should be:
        """
        : {
          sub= {
            key= value
            ::object.class.name= java.util.LinkedHashMap
          }
          ::object.class.name= java.util.LinkedHashMap
        }
        """

    Scenario: Specify Child All Default
      When "collector" collect and build with the following properties:
        """
        = {
          sub= {}
        }
        """
      Then the result should be:
        """
        : {
          sub= {
            ::object.class.simpleName= LinkedHashMap
          }
          ::object.class.simpleName= LinkedHashMap
        }
        """

    Scenario: Specify Child With Intently Creation(do nothing, no error) and Properties
      When "collector" collect and build with the following properties:
        """
        = {
          sub! = {
            key= value
            number= 123
          }
        }
        """
      Then the result should be:
        """
        : {
          sub= {
            key= value
            number= 123,
            ::object.class.name= java.util.LinkedHashMap
          }
          ::object.class.name= java.util.LinkedHashMap
        }
        """

    Scenario: Specify Child With Intently Creation(do nothing, no error) and Default
      When "collector" collect and build with the following properties:
        """
        = {
          sub! = {}
        }
        """
      Then the result should be:
        """
        : {
          sub= {
            ::object.class.simpleName= LinkedHashMap
          }
          ::object.class.simpleName= LinkedHashMap
        }
        """

