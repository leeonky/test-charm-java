Feature: bugs

  Scenario: global is a spec class that different with given spec, missing Spec runtime context when create new spec
    Given the following bean class:
      """
      public class OrderLine {
        public String value;
        public Order order;
      }
      """
    Given the following bean class:
      """
      public class Order {
        public OrderLine line;
        public String value;
      }
      """
    And the following spec class:
      """
      @Global
      public class OrderLineSpec extends Spec<OrderLine> {
        public void main() {
          property("order").is("OrderSpec");
        }
      }
      """
    And the following spec class:
      """
      public class OrderSpec extends Spec<Order> {
        public void main() {
          property("line").reverseAssociation("order");
          property("line").is("OrderLineSpec");
        }
      }
      """
    And the following spec class:
      """
      public class AnotherOrderLineSpec extends OrderLineSpec {
      }
      """
    When build:
      """
      jFactory.spec(AnotherOrderLineSpec.class).create();
      """
#  TODO Association
#    Then the result should:
#      """
#      order: {
#        line= ::root
#        value= hello
#      }
#      """
