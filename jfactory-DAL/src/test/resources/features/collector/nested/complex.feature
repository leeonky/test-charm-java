Feature: More complex object

  Background:
    Given the following declarations:
      """
      JFactory jFactory = new JFactory();
      """
    Given the following class definition:
      """
      public class Order {
        public String orderNo;
        public Customer customer;
        public java.util.List<OrderLine> orderLines;
        public Object tags;
      }
      """
    And the following class definition:
      """
      public class OrderLine {
        public int quantity;
        public Product product;
      }
      """
    And the following class definition:
      """
      public class Product {
        public String name;
        public Category category;
      }
      """
    And the following class definition:
      """
      public class Customer {
        public String name;
        public Address address;
      }
      """
    And the following class definition:
      """
      public class Address {
        public String city;
        public String street;
      }
      """
    And the following class definition:
      """
      public class Category {
        public String name;
        public java.util.List<String> tags;
      }
      """
    And the following class definition:
      """
      public class Tag {
        public String name;
        public Object value;
      }
      """
    And the following spec definition:
      """
      public class TagSpec extends Spec<Tag> {}
      """
    And the following spec definition:
      """
      public class OrderSpec extends Spec<Order> {}
      """
    And register as follows:
      """
      jFactory.register(TagSpec.class);
      jFactory.register(OrderSpec.class);
      """

  Scenario: : Top is Object
    Given the following declarations:
      """
      Collector collector = jFactory.collector(Order.class);
      """
    When "collector" collect and build with the following properties:
      """
      : {
        orderNo: s01
        customer: {
          name: Tom
          address: {
            city: New-York
            street: 5th-Avenue
          }
        }

        orderLines: [{
          quantity: 2
          product: {
            name: iPhone
            category: {
              name: Mobile
              tags: [electronics, gadget]
            }
          }
        }{
          quantity: 1
          product: {
            name: MacBook
            category: {
              name: Computer
              tags: [electronics, gadget]
            }
          }
        }]

        tags= [
          new
          hot
          (TagSpec): {
            name: boolean
            value: true
          }
          (TagSpec): {
            name: map
            value= {
              hello:  world
            }
          }
          (TagSpec): {
            name: nested-map
            value= {
              subTag(TagSpec): {
                name: sub
                value: 123
              }
            }
          }
        ]
      }
      """
    Then the result should be:
      """
      = {
        class.simpleName= Order
        orderNo= s01
        customer= {
          class.simpleName= Customer
          name= Tom
          address= {
            class.simpleName= Address
            city= New-York
            street= 5th-Avenue
          }
        }

        orderLines= [{
          class.simpleName= OrderLine
          quantity= 2
          product= {
            class.simpleName= Product
            name= iPhone
            category= {
              class.simpleName= Category
              name= Mobile
              tags= [electronics, gadget]
              tags.class.simpleName= ArrayList
            }
          }
        }{
          class.simpleName= OrderLine
          quantity= 1
          product= {
            class.simpleName= Product
            name= MacBook
            category= {
              class.simpleName= Category
              name= Computer
              tags= [electronics, gadget]
              tags.class.simpleName= ArrayList
            }
          }
        }]
        tags= [
          new
          hot
          {
            class.simpleName= Tag
            name= boolean
            value= true
          }{
            class.simpleName= Tag
            name= map
            value= {
              ::object.class.simpleName= LinkedHashMap
              hello= world
            }
          }{
            class.simpleName= Tag
            name= nested-map
            value= {
              ::object.class.simpleName= LinkedHashMap
              subTag= {
                class.simpleName= Tag
                name= sub
                value= 123
              }
            }
          }
        ]
      }
      """

  Scenario: Top is List
    Given the following declarations:
      """
      Collector collector = jFactory.collector(java.util.LinkedList.class);
      """
    When "collector" collect and build with the following properties:
      """
      ::this(OrderSpec[]): [{
        orderNo: s01
        customer: {
          name: Tom
          address: {
            city: New-York
            street: 5th-Avenue
          }
        }
        orderLines: [{
          quantity: 2
          product: {
            name: iPhone
            category: {
              name: Mobile
              tags: [electronics, gadget]
            }
          }
        }]
        tags= [
          new
          (TagSpec): {
            name: boolean
            value: true
          }
          (TagSpec): {
            name: nested-map
            value= {
              subTag(TagSpec): {
                name: sub
                value: 123
              }
            }
          }
        ]
      }{
        orderNo: s02
        customer: {
          name: Jerry
          address: {
            city: Los-Angeles
            street: Hollywood-Blvd
          }
        }
        orderLines: [{
          quantity: 3
          product: {
            name: MacBook
            category: {
              name: Computer
              tags: [electronics, laptop]
            }
          }
        }]
        tags= [
          hot
          (TagSpec): {
            name: map
            value= {
              hello: world
            }
          }
        ]
      }]
      """
    Then the result should be:
      """
      : {
        ::this= [{
          class.simpleName= Order
          orderNo= s01
          customer= {
            class.simpleName= Customer
            name= Tom
            address= {
              class.simpleName= Address
              city= New-York
              street= 5th-Avenue
            }
          }
          orderLines= [{
            class.simpleName= OrderLine
            quantity= 2
            product= {
              class.simpleName= Product
              name= iPhone
              category= {
                class.simpleName= Category
                name= Mobile
                tags= [electronics, gadget]
                tags.class.simpleName= ArrayList
              }
            }
          }]
          tags= [
            new
            {
              class.simpleName= Tag
              name= boolean
              value= true
            }{
              class.simpleName= Tag
              name= nested-map
              value= {
                ::object.class.simpleName= LinkedHashMap
                subTag= {
                  class.simpleName= Tag
                  name= sub
                  value= 123
                }
              }
            }
          ]
        }{
          class.simpleName= Order
          orderNo= s02
          customer= {
            class.simpleName= Customer
            name= Jerry
            address= {
              class.simpleName= Address
              city= Los-Angeles
              street= Hollywood-Blvd
            }
          }
          orderLines= [{
            class.simpleName= OrderLine
            quantity= 3
            product= {
              class.simpleName= Product
              name= MacBook
              category= {
                class.simpleName= Category
                name= Computer
                tags= [electronics, laptop]
                tags.class.simpleName= ArrayList
              }
            }
          }]
          tags= [
            hot
            {
              class.simpleName= Tag
              name= map
              value= {
                ::object.class.simpleName= LinkedHashMap
                hello= world
              }
            }
          ]
        }]
        ::object.class.simpleName= ArrayList
      }
      """

  Scenario: Top is Raw List
    Given the following declarations:
      """
      Collector collector = jFactory.collector();
      """
    When "collector" collect and build with the following properties:
      """
      = [
        (OrderSpec): {
          orderNo= s01
          customer: {
            name= Tom
            address: {
              city= New-York
              street= 5th-Avenue
            }
          }
          orderLines: [{
            quantity= 2
            product: {
              name= iPhone
              category: {
                name: Mobile
                tags: [electronics, gadget]
              }
            }
          }]
          tags= [new hot]
        }
        (OrderSpec): {
          orderNo= s02
          customer: {
            name= Jerry
            address: {
              city= Los-Angeles
              street= Hollywood-Blvd
            }
          }
          orderLines: [{
            quantity= 1
            product: {
              name= MacBook
              category: {
                name: Computer
                tags: [electronics, laptop]
              }
            }
          }]
          tags= [sale]
        }
      ]
      """
    Then the result should be:
      """
      : {
        ::this= [{
          ::object.class.simpleName= Order
          orderNo= s01
          customer= {
            ::object.class.simpleName= Customer
            name= Tom
            address= {
              ::object.class.simpleName= Address
              city= New-York
              street= 5th-Avenue
            }
          }
          orderLines= [{
            ::object.class.simpleName= OrderLine
            quantity= 2
            product= {
              ::object.class.simpleName= Product
              name= iPhone
              category= {
                ::object.class.simpleName= Category
                name= Mobile
                tags= [electronics, gadget]
              }
            }
          }]
          tags= [new hot]
        }{
          ::object.class.simpleName= Order
          orderNo= s02
          customer= {
            ::object.class.simpleName= Customer
            name= Jerry
            address= {
              ::object.class.simpleName= Address
              city= Los-Angeles
              street= Hollywood-Blvd
            }
          }
          orderLines= [{
            ::object.class.simpleName= OrderLine
            quantity= 1
            product= {
              ::object.class.simpleName= Product
              name= MacBook
              category= {
                ::object.class.simpleName= Category
                name: Computer
                tags= [electronics, laptop]
              }
            }
          }]
          tags= [sale]
        }]
        class.simpleName= ArrayList
      }
      """

  Scenario: Top is Raw Map
    Given the following declarations:
      """
      Collector collector = jFactory.collector();
      """
    When "collector" collect and build with the following properties:
      """
      = {
        order(OrderSpec): {
          orderNo: s01
          customer: {
            name: Tom
            address: {
              city: New-York
              street: 5th-Avenue
            }
          }
          orderLines: [{
            quantity: 2
            product: {
              name: iPhone
              category: {
                name: Mobile
                tags: [electronics, gadget]
              }
            }
          }{
            quantity: 1
            product: {
              name: MacBook
              category: {
                name: Computer
                tags: [electronics, laptop]
              }
            }
          }]
          tags= [new]
        }
        metadata: {
          createdBy: system
          tags: [
            new
            hot
          ]
          extra(TagSpec): {
            name: promo
            value: true
          }
        }
      }
      """
    Then the result should be:
      """
      : {
        ::object.class.simpleName= LinkedHashMap
        order= {
          ::object.class.simpleName= Order
          orderNo= s01
          customer= {
            ::object.class.simpleName= Customer
            name= Tom
            address= {
              ::object.class.simpleName= Address
              city= New-York
              street= 5th-Avenue
            }
          }
          orderLines= [{
            ::object.class.simpleName= OrderLine
            quantity= 2
            product= {
              ::object.class.simpleName= Product
              name= iPhone
              category= {
                ::object.class.simpleName= Category
                name= Mobile
                tags= [electronics, gadget]
              }
            }
          }{
            ::object.class.simpleName= OrderLine
            quantity= 1
            product= {
              ::object.class.simpleName= Product
              name= MacBook
              category= {
                ::object.class.simpleName= Category
                name= Computer
                tags= [electronics, laptop]
              }
            }
          }]
          tags= [new]
        }
        metadata= {
          ::object.class.simpleName= LinkedHashMap
          createdBy= system
          tags= [new hot]
          extra= {
            class.simpleName= Tag
            name= promo
            value= true
          }
        }
      }
      """
