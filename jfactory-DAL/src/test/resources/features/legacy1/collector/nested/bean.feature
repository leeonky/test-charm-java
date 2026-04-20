Feature: Nested Object

  Background:
    Given the following declarations:
      """
      JFactory jFactory = new JFactory();
      """

  Rule: Sub is Bean

    Background:
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

    Scenario: Specify Parent and Child Property
      When "collector" collect with the following properties:
        """
        : {
          name= Smartphone
          category.name= Electronics
        }
        """
      Then the result should be:
        """
        : {
          ::properties= {
            name= Smartphone
            'category.name'= Electronics
          }
          ::build= {
            category.name= Electronics
            name= Smartphone
          }
        }
        """

    Scenario: Specify Child Properties
      When "collector" collect with the following properties:
        """
        category: {
          name= Electronics
          order= 1
        }
        """
      Then the result should be:
        """
        : {
          ::properties= {
            'category.name'= Electronics
            'category.order'= 1
          }
          ::build= {
            category= {
              name= Electronics
              order= 1
            }
            name= /^name.*/
          }
        }
        """

    Scenario: Specify Child All Default
      When "collector" collect with the following properties:
        """
        category: {...}
        """
      Then the result should be:
        """
        : {
          ::properties= {
            category= {}
          }
          ::build= {
            category= {
              name= /^name.*/
              order= 1
            }
            name= /^name.*/
          }
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

  Rule: Sub is Bean by Spec

    Background:
      Given the following bean definition:
        """
        public class Product {
          public Object category;
          public String name;
        }
        """
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
      Given the following declarations:
        """
        Collector collector = jFactory.collector(Product.class);
        """
      And register as follows:
        """
        jFactory.register(CategorySpec.class);
        """

    Scenario: Specify Parent and Child Property
      When "collector" collect with the following properties:
        """
        : {
          name= Smartphone
          category(CategorySpec).order= 100
        }
        """
      Then the result should be:
        """
        : {
          ::properties= {
            name= Smartphone
            'category(CategorySpec).order'= 100
          }
          ::build= {
            category.order= 100
            name= Smartphone
          }
        }
        """

    Scenario: Specify Child Properties
      When "collector" collect with the following properties:
        """
        category(CategorySpec): {
          order= 1
        }
        """
      Then the result should be:
        """
        : {
          ::properties= {
            'category(CategorySpec).order'= 1
          }
          ::build= {
            category= {
              order= 1
            }
            name= /^name.*/
          }
        }
        """

    Scenario: Specify Child All Default
      When "collector" collect with the following properties:
        """
        category(CategorySpec): {...}
        """
      Then the result should be:
        """
        : {
          ::properties= {
            'category(CategorySpec)'= {}
          }
          ::build= {
            category= {
              order= 1
              class.simpleName= Category
            }
            name= /^name.*/
          }
        }
        """

    Scenario: Specify Child With Intently Creation and Properties
      When "collector" collect with the following properties:
        """
        : {
          name= Smartphone
          category(CategorySpec)!: {
            order= 42
          }
        }
        """
      Then the result should be:
        """
        : {
          ::properties= {
            name= Smartphone
            'category(CategorySpec)!.order'= 42
          }
          ::build: {
            name= Smartphone
            category= {
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
              order= 1
              class.simpleName= Category
            }
          }
        }
        """

    Scenario: Invalid = {key: value} in Child Creation
      When "collector" collect with the following properties:
        """
        : {
          name= Smartphone
          category(CategorySpec)= {
            order= 100
          }
        }
        """
      Then the result should be:
        """
        ::throw.message::should.contains : ```

                                          : {
                                            name= Smartphone
                                            category(CategorySpec)= {
                                                                    ^
                                              order= 100
                                            }
                                          }

                                          java.lang.IllegalStateException: Cannot create raw Map/List when traits were specified
                                          ```
        """

  Rule: Sub is Raw Map

    Background:
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

    Scenario: Specify Parent Property Only
      When "collector" collect with the following properties:
        """
        name= TestBean
        """
      Then the result should be:
        """
        : {
          ::properties= {
            name= TestBean
          }
          ::build= {
            sub= null
            name= TestBean
          }
        }
        """

    Scenario: Specify Parent Default Only
      When "collector" collect with the following properties:
        """
        : {...}
        """
      Then the result should be:
        """
        : {
          ::properties= {}
          ::build= {
            sub= null
            name= /^name.*/
          }
        }
        """

    Scenario: Specify Parent and Child Property
      When "collector" collect with the following properties:
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
        : {
          ::properties= {
            name= TestBean
            sub= {
              key= value
            }
          }
          ::build= {
            sub= {
              key= value
              ::object.class.name= java.util.LinkedHashMap
            }
            name= TestBean
          }
        }
        """

    Scenario: Specify Child Properties
      When "collector" collect with the following properties:
        """
        sub= {
          key= value
          number= 123
        }
        """
      Then the result should be:
        """
        : {
          ::properties= {
            sub= {
              key= value
              number= 123
            }
          }
          ::build= {
            sub= {
              key= value
              number= 123,
              ::object.class.name= java.util.LinkedHashMap
            }
            name= /^name.*/
          }
        }
        """

    Scenario: Default of Object with : {...} should create an Object
      When "collector" collect with the following properties:
        """
        sub: {...}
        """
      Then the result should be:
        """
        : {
          ::properties= {
            'sub'= {}
          }
          ::build= {
            sub= {
              ::object.class.name= java.lang.Object
            }
            name= /^name.*/
          }
        }
        """

    Scenario: Specify Child an Empty Map by = {}
      When "collector" collect with the following properties:
        """
        sub= {}
        """
      Then the result should be:
        """
        : {
          ::properties= {
            'sub(EMPTY_MAP)'= {}
          }
          ::build= {
            sub= {
              ::object.class.name= java.util.HashMap
            }
            name= /^name.*/
          }
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
              number= 123,
              ::object.class.name= java.util.LinkedHashMap
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
          sub! = {}
          name= hello
        }
        """
      Then the result should be:
        """
        : {
          ::properties= {
              name= hello
              'sub(EMPTY_MAP)'= {}
          }
          ::build= {
              name= hello
              sub= {
                ::object.class.name= java.util.HashMap
              }
          }
        }
        """

  Rule: Sub is Raw List

    Background:
      Given the following bean definition:
        """
        public class Bean {
          public Object sub;
        }
        """
      Given the following declarations:
        """
        Collector collector = jFactory.collector(Bean.class);
        """

    Scenario: List
      When "collector" collect with the following properties:
        """
        sub= [hello world]
        """
      Then the result should be:
        """
        : {
          ::properties: {
            'sub'= [hello world]
          }
          ::build: {
            sub= [hello world]
            sub.class.simpleName= ArrayList
          }
        }
        """

    Scenario: Empty List
      When "collector" collect with the following properties:
        """
        sub= []
        """
      Then the result should be:
        """
        : {
          ::properties= {
            sub= []
          }
          ::build: {
            sub= []
            sub.class.simpleName= ArrayList
          }
        }
        """

  Rule: Sub is List by Spec

    Background:
      Given the following bean definition:
        """
        public class Bean {
          public Object sub;
        }
        """
      Given the following spec definition:
        """
        public class StringList extends Spec<String[]> { }
        """
      And register as follows:
        """
        jFactory.register(StringList.class);
        """
      Given the following declarations:
        """
        Collector collector = jFactory.collector(Bean.class);
        """

    Scenario: Specify Child With Spec and Properties
      When "collector" collect with the following properties:
        """
        sub(StringList): [hello world]
        """
      Then the result should be:
        """
        : {
          ::properties= {
            'sub(StringList)[0]'= hello
            'sub(StringList)[1]'= world
          }
          ::build: {
            ::this.sub= [hello world]
            ::this.sub.class.simpleName= 'String[]'
          }
        }
        """

    Scenario: Specify Child with Spec and Default
      When "collector" collect with the following properties:
        """
        sub(StringList): {...}
        """
      Then the result should be:
        """
        : {
          ::properties= {
            'sub(StringList)'= {}
          }

          ::build: {
            ::this.sub= []
            ::this.sub.class.simpleName= 'String[]'
          }
        }
        """

