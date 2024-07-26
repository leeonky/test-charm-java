Feature: default sub object spec

  Scenario: not use default sub object spec when object has a global spec class
    Given the following bean class:
    """
      public class Order {
        public Product product;
      }
    """
    And the following bean class:
    """
      public class Product {
        public String name;
        public int price;
      }
    """
    And the following spec class:
    """
      public class OneOrder extends Spec<Order> {
        public void main() {
          property("product").is(OneProduct.class);
        }
      }
    """
    And the following spec class:
    """
      public class OneProduct extends Spec<Product> {
        public void main() {
          property("name").value("PC");
        }
      }
    """
    And the following spec class:
    """
      @Global
      public class GlobalProduct extends Spec<Product> {
      }
    """
    When build:
    """
      jFactory.spec(OneOrder.class).property("product.price", 100).create();
    """
    Then the result should:
    """
      product: {
        name= PC
        price= 100
      }
    """
