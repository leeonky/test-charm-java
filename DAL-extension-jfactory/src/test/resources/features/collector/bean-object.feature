Feature: Nested Object

  Background:
    Given the following declarations:
      """
      JFactory jFactory = new JFactory();
      """
    Given the following bean definition:
      """
      public class Bean {
        public Object sub;
        public String name;
      }
      """
    Given the following declarations:
      """
      Collector collector = jFactory.collector(Bean.class);
      """

  Rule: As a Map

    Scenario: Specify Parent Property Only
      When "collector" collect and build with the following properties:
        """
        name= TestBean
        """
      Then the result should be:
        """
        = {
          sub= null
          name= TestBean
        }
        """

    Scenario: Specify Parent Default Only
      When "collector" collect and build with the following properties:
        """
        : {...}
        """
      Then the result should be:
        """
        = {
          sub= null
          name= /^name.*/
        }
        """

    Scenario: Specify Parent and Child Property
      When "collector" collect and build with the following properties:
        """
        : {
          name= TestBean
          sub= {
            key= value
          }
        }
        """
      Then the result should be:
        """
        = {
          sub= {
            key= value
          }
          name= TestBean
        }
        """

    Scenario: Specify Child Properties
      When "collector" collect and build with the following properties:
        """
        sub= {
          key= value
          number= 123
        }
        """
      Then the result should be:
        """
        = {
          sub= {
            key= value
            number= 123
          }
          name= /^name.*/
        }
        """

    Scenario: Specify Child All Default
      When "collector" collect and build with the following properties:
        """
        sub: {...}
        """
      Then the result should be:
        """
        = {
          sub= {}
          name= /^name.*/
        }
        """

    Scenario: Specify Child All Default = {}
      When "collector" collect and build with the following properties:
        """
        sub= {}
        """
      Then the result should be:
        """
        = {
          sub= {}
          name= /^name.*/
        }
        """

    Scenario: Specify Child With Intently Creation(do nothing, no error) and Properties
      When "collector" collect with the following properties:
        """
        : {
          sub! = {
            key= value
            number= 123
          }
          name= hello
        }
        """
      Then the result should be:
        """
        : {
          ::properties= {
            name= hello
            sub= {
              key= value
              number= 123
            }
          }
          ::build: {
            name= hello
            sub= {
              key= value
              number= 123
            }
          }
        }
        """

    Scenario: Specify Child With Intently Creation(do nothing, no error) and Default
      When "collector" collect with the following properties:
        """
        : {
          sub! = {...}
          name= hello
        }
        """
      Then the result should be:
        """
        : {
          ::properties= {
              name= hello
              sub= {}
          }
          ::build= {
              name= hello
              sub= {}
          }
        }
        """

  Rule: Use Spec

    Background:
      Given the following bean definition:
        """
        public class Sub {
          public String value1, value2;
        }
        """
      And the following spec definition:
        """
        public class SubSpec extends Spec<Sub> {
          public void main() {
            property("value1").value("sub1");
          }
        }
        """
      And register as follows:
        """
        jFactory.register(SubSpec.class);
        """

    Scenario: Specify Child With Spec and Properties
      When "collector" collect and build with the following properties:
        """
        sub(SubSpec): {
          value2= sub2
        }
        """
      Then the result should be:
        """
        = {
          sub= {
            value1= sub1
            value2= sub2
          }
          name= /^name.*/
        }
        """

    Scenario: Specify Child with Spec and Default
      When "collector" collect and build with the following properties:
        """
        sub(SubSpec): {...}
        """
      Then the result should be:
        """
        = {
          sub= {
            value1= sub1
            value2= /^value2.*/
          }
          name= /^name.*/
        }
        """

    Scenario: Specify Child With Spec Intently Creation and Properties
      When "collector" collect with the following properties:
        """
        : {
          sub(SubSpec)!: {
              value2= sub2
          }
          name= hello
        }
        """
      Then the result should be:
        """
        : {
          ::properties= {
            name= hello
            'sub(SubSpec)!.value2'= sub2
          }
          ::build= {
            name= hello
            sub= {
              value1= sub1
              value2= sub2
            }
          }
        }
        """

    Scenario: Specify Child With Spec Intently Creation and Default
      When "collector" collect with the following properties:
        """
        : {
          sub(SubSpec)!: {...}
          name= hello
        }
        """
      Then the result should be:
        """
        : {
          ::properties= {
            name= hello
            'sub(SubSpec)!'= {}
          }
        ::build= {
          name= hello
          sub= {
            value1= sub1
            value2= /^value2.*/
            }
          }
        }
        """
