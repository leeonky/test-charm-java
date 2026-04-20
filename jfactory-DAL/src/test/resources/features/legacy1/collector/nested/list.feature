Feature: List
  Use `: []` to Create List by JFactory

  Background:
    Given the following declarations:
      """
      JFactory jFactory = new JFactory();
      """
    Given the following declarations:
      """
      Collector collector = jFactory.collector(java.util.LinkedList.class);
      """

  Rule: Element is a Raw Map

    Scenario: Specify Child Properties
      When "collector" collect with the following properties:
        """
        : [= {
          value1= v1
          value2= v2
        }]
        """
      Then the result should be:
        """
        : {
          ::properties= {
            '[0]'= {
              value1= v1
              value2= v2
            }
          }
          ::build: {
            ::this= [{
              value1= v1
              value2= v2
              ::object.class.name= java.util.LinkedHashMap
            }]

            class.name= java.util.LinkedList
          }
        }
        """

    Scenario: Specify Child All Default
      When "collector" collect with the following properties:
        """
        : [= {}]
        """
      Then the result should be:
        """
        : {
          ::properties= {
            '[0](EMPTY_MAP)'= {}
          }
          ::build: {
            ::this= [{
              ::object.class.name= java.util.HashMap
            }]

            class.name= java.util.LinkedList
          }
        }
        """

    Scenario: Specify Child With Intently Creation(do nothing, no error) and Properties
      When "collector" collect with the following properties:
        """
        [0]! = {
          value1= v1
          value2= v2
        }
        """
      Then the result should be:
        """
        : {
          ::properties= {
            '[0]'= {
              value1= v1
              value2= v2
            }
          }

          ::build= [{
            value1= v1
            value2= v2
          }]

          class.name= java.util.LinkedList
        }
        """

    Scenario: Specify Child With Intently Creation(do nothing, no error) and Default
      When "collector" collect with the following properties:
        """
        [0]! = {}
        """
      Then the result should be:
        """
        : {
          ::properties= {
              '[0](EMPTY_MAP)'= {}
          }

          ::build= [{}]

          class.name= java.util.LinkedList
        }
        """

  Rule: Element is a Raw List

    Scenario: List
      When "collector" collect with the following properties:
        """
        : [
          =[hello world]
        ]
        """
      Then the result should be:
        """
        : {
          ::properties: {
            '[0]'= [hello world]
          }
          ::build: {
            ::this= [[hello world]]
            ::object.class.simpleName= LinkedList
          }
        }
        """

    Scenario: Empty List
      When "collector" collect with the following properties:
        """
        : [
          = []
        ]
        """
      Then the result should be:
        """
        : {
          ::properties= {
            '[0]'= []
          }
          ::build: {
            ::this= [[]]
            ::object.class.simpleName= LinkedList
          }
        }
        """

  Rule: Element is Bean by Spec

    Background:
      Given the following bean definition:
        """
        public class Bean {
          public String value1, value2;
        }
        """
      Given the following spec definition:
        """
        public class BeanSpec extends Spec<Bean> {
          public void main() {
            property("value1").value("v1");
          }
        }
        """
      And register as follows:
        """
        jFactory.register(BeanSpec.class);
        """

    Scenario: Specify Child With Spec and Properties
      When "collector" collect with the following properties:
        """
        [0](BeanSpec): {
          value2= v2
        }
        """
      Then the result should be:
        """
        : {
          ::properties= {
            '[0](BeanSpec).value2'= v2
          }
          ::build: {
            ::this= [{
                value1= v1
                value2= v2
                ::object.class.name= Bean
            }]

            class.name= java.util.LinkedList
          }
        }
        """

    Scenario: Specify Child with Spec and Default
      When "collector" collect with the following properties:
        """
        [0](BeanSpec): {...}
        """
      Then the result should be:
        """
        : {
          ::properties= {
            '[0](BeanSpec)'= {}
          }

          ::build= [{
            value1= v1
            value2= /^value2.*/
          }]

          class.name= java.util.LinkedList
        }
        """

    Scenario: Specify Child With Spec Intently Creation and Properties
      When "collector" collect with the following properties:
        """
        [0](BeanSpec)!: {
          value2= v2
        }
        """
      Then the result should be:
        """
        : {
          ::properties= {
            '[0](BeanSpec)!.value2'= v2
          }

          ::build= [{
            value1= v1
            value2= v2
          }]

          class.name= java.util.LinkedList
        }
        """

    Scenario: Specify Child With Spec Intently Creation and Default
      When "collector" collect with the following properties:
        """
        [0](BeanSpec)!: {...}
        """
      Then the result should be:
        """
        : {
          ::properties= {
            '[0](BeanSpec)!'= {}
          }

          ::build= [{
            value1= v1
            value2= /^value2.*/
          }]

          class.name= java.util.LinkedList
        }
        """

    Scenario: Specify Collection Spec and Element Properties
      Given the following spec definition:
        """
        public class ListBeanSpec extends Spec<java.util.List<Bean>> { }
        """
      And register as follows:
        """
        jFactory.register(ListBeanSpec.class);
        """
      When "collector" collect with the following properties:
        """
        ::this(ListBeanSpec)[0]: {
          value2= v2
        }
        """
      Then the result should be:
        """
        : {
          ::properties= {
            '[0].value2'= v2
          }
          ::build: {
            ::this: [{
                value2= v2
                ::object.class.name= Bean
            }]

            class.simpleName= ArrayList
          }
        }
        """

  Rule: Element is List by Spec

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
      When "collector" collect with the following properties:
        """
        [0](StringList): [hello world]
        """
      Then the result should be:
        """
        : {
          ::properties= {
            '[0](StringList)[0]'= hello
            '[0](StringList)[1]'= world
          }
          ::build: {
            ::this= [[hello world]]
            ::this[0].class.simpleName= 'String[]'
            class.simpleName= LinkedList
          }
        }
        """

    Scenario: Specify Child with Spec and Default
      When "collector" collect with the following properties:
        """
        [0](StringList): {...}
        """
      Then the result should be:
        """
        : {
          ::properties= {
            '[0](StringList)'= {}
          }

          ::build: {
            ::this= [[]]
            ::this[0].class.simpleName= 'String[]'
            class.simpleName= LinkedList
          }
        }
        """

  Rule: Element is List by Element Spec[]

    Background:
      Given the following bean definition:
      """
      public class Product {
        public String name, color;
        public int price;
      }
      """
      Given the following spec definition:
        """
        public class ProductSpec extends Spec<Product> {
          @Trait
          public void red() {
            property("color").value("red");
          }
        }
        """
      And register as follows:
        """
        jFactory.register(ProductSpec.class);
        """

    Scenario: Specify Element Spec
      Given the following declarations:
        """
        Collector elementSpecCollector = jFactory.collector("red", "ProductSpec[]");
        """
      When "elementSpecCollector" collect with the following properties:
        """
        : [{
          name: p1
        }{
          name: p2
        }]
        """
      Then the result should be:
        """
        : {
          ::properties= {
            '[0].name'= p1
            '[1].name'= p2
          }
          ::build: {
            ::this= | name | color | price | class.simpleName |
                    | p1   | red   | 1     | Product          |
                    | p2   | red   | 2     | Product          |
          }
        }
        """

    Scenario: bug: list mapping
      Given the following declarations:
        """
        Collector elementSpecCollector = jFactory.collector("red", "ProductSpec[]");
        """
      When "elementSpecCollector" collect with the following properties:
        """
        name[]: [p1 p2]
        """
      Then the result should be:
        """
        : {
          ::properties= {
            '[0].name'= p1
            '[1].name'= p2
          }
          ::build: {
            ::this= | name | color | price | class.simpleName |
                    | p1   | red   | 1     | Product          |
                    | p2   | red   | 2     | Product          |
          }
        }
        """

  Rule: Element is a Raw List Map

    Scenario: Specify Grand Child Properties
      When "collector" collect with the following properties:
        """
        [0]= [{
          value1= v1
          value2= v2
        }]
        """
      Then the result should be:
        """
        : {
          ::properties: {
            '[0]'= [{
              value1= v1
              value2= v2
            }]
          }

          ::build: {
            ::this= [[{
              value1= v1
              value2= v2
              ::object.class.name= java.util.LinkedHashMap
            }]]

            [0]::object.class.name= java.util.ArrayList
            class.name= java.util.LinkedList
          }
        }
        """

    Scenario: Specify Grand Child All Default
      When "collector" collect with the following properties:
        """
        [0]= [{}]
        """
      Then the result should be:
        """
        : {
          ::properties: {
            '[0]'= [{}]
          }

          ::build: {
            ::this= [[{
              ::object.class.name= java.util.LinkedHashMap
            }]]

            [0]::object.class.name= java.util.ArrayList
            class.name= java.util.LinkedList
          }
        }
        """

