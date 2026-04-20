Feature: Nested Raw List
  Create List by Collector

  Background:
    Given the following declarations:
      """
      JFactory jFactory = new JFactory();
      """
    Given the following declarations:
      """
      Collector collector = jFactory.collector();
      """

  Rule: Element is a Bean

    Background:
      Given the following bean definition:
        """
        public class Bean {
          public String value1, value2;
        }
        """
      Given the following spec definition:
        """
        public class BeanSpec extends Spec<Bean> {}
        """
      And register as follows:
        """
        jFactory.register(BeanSpec.class);
        """

    Scenario: Specify Child With Spec and Properties
      When "collector" collect and build with the following properties:
        """
        = [(BeanSpec): {
          value2= v2
        }]
        """
      Then the result should be:
        """
        : {
          ::this= [{
            value1= /^value1.*/
            value2= v2
            ::object.class.simpleName= Bean
          }]

          class.simpleName= ArrayList
        }
        """

    Scenario: Specify Child with Spec and Default
      When "collector" collect and build with the following properties:
        """
        = [(BeanSpec): {...}]
        """
      Then the result should be:
        """
        : {
          ::this= [{
            value1= /^value1.*/
            value2= /^value2.*/
            class.simpleName= Bean
          }]
          class.simpleName= ArrayList
        }
        """

    Scenario: Specify Child With Spec Intently Creation and Properties
      When "collector" collect and build with the following properties:
        """
        = [(BeanSpec)!: {
          value2= v2
        }]
        """
      Then the result should be:
        """
        : {
          ::this= [{
            value1= /^value1.*/
            value2= v2
            class.simpleName= Bean
          }]

          class.simpleName= ArrayList
        }
        """

    Scenario: Specify Child With Spec Intently Creation and Default
      When "collector" collect and build with the following properties:
        """
        = [(BeanSpec)!: {...}]
        """
      Then the result should be:
        """
        : {
          ::this= [{
            value1= /^value1.*/
            value2= /^value2.*/
            class.simpleName= Bean
          }]

          class.simpleName= ArrayList
        }
        """

  Rule: Element is a List

    Background:
      Given the following bean definition:
        """
        public class Bean {}
        """
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
        = [(StringList): [hello world]]
        """
      Then the result should be:
        """
        : {
          ::this: {
            ::this= [[hello world]]
            ::this[0].class.simpleName= 'String[]'
            class.simpleName= ArrayList
          }
        }
        """

    Scenario: Specify Child with Spec and Default
      When "collector" collect and build with the following properties:
        """
        = [(StringList): {...}]
        """
      Then the result should be:
        """
        : {
          ::this: {
            ::this= [[]]
            ::this[0].class.simpleName= 'String[]'
            class.simpleName= ArrayList
          }
        }
        """

  Rule: Element is a Raw List

    Scenario: List
      When "collector" collect and build with the following properties:
        """
        : [
          =[hello world]
        ]
        """
      Then the result should be:
        """
        : {
          ::this= [[hello world]]
          class.simpleName= ArrayList
        }
        """

    Scenario: Empty List
      When "collector" collect and build with the following properties:
        """
        : [
          = []
        ]
        """
      Then the result should be:
        """
        : {
          ::this= [[]]
          class.simpleName= ArrayList
        }
        """

  Rule: Element is a Raw Map

    Scenario: Specify Child Properties
      When "collector" collect and build with the following properties:
        """
        : [= {
          value1= v1
          value2= v2
        }]
        """
      Then the result should be:
        """
        : {
          ::this= [{
            value1= v1
            value2= v2
            ::object.class.simpleName= LinkedHashMap
          }]

          class.simpleName= ArrayList
        }
        """

    Scenario: Specify Child All Default
      When "collector" collect and build with the following properties:
        """
        : [= {}]
        """
      Then the result should be:
        """
        : {
          ::this= [{
            ::object.class.simpleName= LinkedHashMap
          }]

          class.simpleName= ArrayList
        }
        """
