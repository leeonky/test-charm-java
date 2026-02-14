Feature: List Bean([0].b)

  Background:
    Given the following declarations:
      """
      JFactory jFactory = new JFactory();
      """
#    Given the following bean definition:
#      """
#      public class Bean {
#        public String value1, value2;
#      }
#      """
#    Given the following spec definition:
#      """
#      public class BeanSpec extends Spec<Bean> {
#        public void main() {
#          property("value1").value("v1");
#        }
#      }
#      """
#    And register as follows:
#      """
#      jFactory.register(BeanSpec.class);
#      """
    Given the following declarations:
      """
      Collector collector = jFactory.collector(java.util.LinkedList.class);
      """

  Rule: As a Map

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
            ::object.class.name= java.util.LinkedHashMap
          }]

          class.name= java.util.LinkedList
        }
        """

    Scenario: Specify Child All Default
      When "collector" collect and build with the following properties:
        """
        : [= {...}]
        """
      Then the result should be:
        """
        : {
          ::this= [{
            ::object.class.name= java.util.HashMap
          }]

          class.name= java.util.LinkedList
        }
        """
