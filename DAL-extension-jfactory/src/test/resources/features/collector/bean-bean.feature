Feature: Nested Bean(a.b)

  Background:
    Given the following declarations:
      """
      JFactory jFactory = new JFactory();
      """
    Given the following bean definition:
      """
      public class Product {
        public Category category;
        public String name;
      }
      """
    Given the following bean definition:
      """
      public class Category {
        public String name;
        public int order;
      }
      """
    Given the following declarations:
      """
      Collector collector = jFactory.collector(Product.class);
      """

  Scenario: Specify Parent Property Only
    When "collector" collect and build with the following properties:
      """
      name= Smartphone
      """
    Then the result should be:
      """
      = {
        category= null
        name= Smartphone
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
        category= null
        name= /^name.*/
      }
      """

  Scenario: Specify Parent and Child Property
    When "collector" collect and build with the following properties:
      """
      : {
        name= Smartphone
        category.name= Electronics
      }
      """
    Then the result should be:
      """
      = {
        category.name= Electronics
        name= Smartphone
      }
      """

  Scenario: Specify Child Properties
    When "collector" collect and build with the following properties:
      """
      category: {
        name= Electronics
        order= 1
      }
      """
    Then the result should be:
      """
      = {
        category= {
          name= Electronics
          order= 1
        }
        name= /^name.*/
      }
      """

  Scenario: Specify Child All Default
    When "collector" collect and build with the following properties:
      """
      category: {...}
      """
    Then the result should be:
      """
      = {
        category= {
          name= /^name.*/
          order= 1
        }
        name= /^name.*/
      }
      """

  Scenario: Specify Child All Default = {}
    When "collector" collect and build with the following properties:
      """
      category= {}
      """
    Then the result should be:
      """
      = {
        category= {
          name= /^name.*/
          order= 1
        }
        name= /^name.*/
      }
      """

  Scenario: Specify Child With Spec and Properties
    Given the following spec definition:
      """
      public class CategorySpec extends Spec<Category> {
        public void main() {
            property("name").value("Electronics");
        }
      }
      """
    And register as follows:
      """
      jFactory.register(CategorySpec.class);
      """
    When "collector" collect and build with the following properties:
      """
      category(CategorySpec): {
        order= 2
      }
      """
    Then the result should be:
      """
      = {
        category: {
          name= Electronics
          order= 2
        }
        name= /^name.*/
      }
      """

  Scenario: Specify Child with Spec and Default
    Given the following spec definition:
      """
      public class CategorySpec extends Spec<Category> {
        public void main() {
            property("name").value("Electronics");
            property("order").value(42);
        }
      }
      """
    And register as follows:
      """
      jFactory.register(CategorySpec.class);
      """
    When "collector" collect and build with the following properties:
      """
      category(CategorySpec): {...}
      """
    Then the result should be:
      """
      = {
        category: {
          name= Electronics
          order= 42
        }
        name= /^name.*/
      }
      """

  Scenario: Specify Child With Intently Creation and Properties
    When "collector" collect with the following properties:
      """
      : {
        name= Smartphone
        category!: {
          name= Electronics
          order= 42
        }
      }
      """
    Then the result should be:
      """
      : {
        ::properties= {
          name= Smartphone
          'category!.name'= Electronics
          'category!.order'= 42
        }
        ::build: {
          name= Smartphone
          category= {
            name= Electronics
            order= 42
          }
        }
      }
      """

  Scenario: Specify Child With Intently Creation and Default
    When "collector" collect with the following properties:
      """
      : {
        name= Smartphone
        category!: {...}
      }
      """
    Then the result should be:
      """
      : {
        ::properties= {
          name= Smartphone
          'category!'= {}
        }
        ::build= {
          name= Smartphone
          category= {
            name= /^name.*/
            order= 1
          }
        }
      }
      """

  Scenario: Specify Child With Spec Intently Creation and Properties
    Given the following spec definition:
      """
      public class CategorySpec extends Spec<Category> {
        public void main() {
            property("name").value("Electronics");
        }
      }
      """
    And register as follows:
      """
      jFactory.register(CategorySpec.class);
      """
    When "collector" collect with the following properties:
      """
      : {
        name= Smartphone
        category(CategorySpec)!: {
          order= 2
        }
      }
      """
    Then the result should be:
      """
      : {
        ::properties= {
          name= Smartphone
          'category(CategorySpec)!.order'= 2
        }
        ::build= {
          name= Smartphone
          category= {
            name= Electronics
            order= 2
          }
        }
      }
      """

  Scenario: Specify Child With Spec Intently Creation and Default
    Given the following spec definition:
      """
      public class CategorySpec extends Spec<Category> {
        public void main() {
            property("name").value("Electronics");
            property("order").value(42);
        }
      }
      """
    And register as follows:
      """
      jFactory.register(CategorySpec.class);
      """
    When "collector" collect with the following properties:
      """
      : {
        name= Smartphone
        category(CategorySpec)!: {...}
      }
      """
    Then the result should be:
      """
      : {
        ::properties= {
          name= Smartphone
          'category(CategorySpec)!'= {}
        }
        ::build= {
          name= Smartphone
          category= {
            name= Electronics
            order= 42
          }
        }
      }
      """

  Scenario: Invalid = {key: value} in Child Creation
    When "collector" collect and build with the following properties:
      """
      : {
        name= Smartphone
        category= {
          name= Electronics
        }
      }
      """
    Then the result should be:
      """
      ::throw.message= ```
                       Cannot convert from java.util.LinkedHashMap to class Category
                       ```
      """

#  Parent Only
#  Parent Default

#  Properties
#  Default

#  Spec Properties
#  Spec Default

#  Intently Properties
#  Intently Default

#  Spec Intently Properties
#  Spec Intently Default
