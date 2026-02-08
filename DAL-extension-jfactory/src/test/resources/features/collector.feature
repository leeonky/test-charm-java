Feature: Collector

  Rule: Flat Object by Default Type

    Background:
      Given the following declarations:
        """
        JFactory jFactory = new JFactory();
        """
      Given the following bean definition:
        """
        public class Bean {}
        """
      Given the following declarations:
        """
        Collector collector = jFactory.collector(Bean.class);
        """

    Scenario: Single Property - Collect and Build Flat Object by Default Type
      Given the following bean definition:
        """
        public class Bean {
          public String value;
        }
        """
      When "collector" collect and build with the following properties:
        """
        value= hello
        """
      Then the result should be:
        """
        value= hello
        """

    Scenario: Multiple Properties - Collect and Build Flat Object by Default Type and Multiple Properties
      Given the following bean definition:
        """
        public class Bean {
          public String value1, value2;
        }
        """
      When "collector" collect and build with the following properties:
        """
        : {
          value1= hello
          value2= world
        }
        """
      Then the result should be:
        """
        = {
          value1= hello
          value2= world
        }
        """

    Scenario: Matching and Equal Opt has no difference for Property Value Assignment
      Given the following bean definition:
        """
        public class Bean {
          public int value1, value2;
        }
        """
      When "collector" collect and build with the following properties:
        """
        : {
          value1= 100
          value2: 200
        }
        """
      Then the result should be:
        """
        = {
          value1= 100
          value2= 200
        }
        """

    Scenario: Specify Spec
      Given the following bean definition:
        """
        public class Bean {
          public int value1, value2;
        }
        """
      Given the following spec definition:
        """
        public class BeanSpec extends Spec<Bean> {
          public void main() {
            property("value1").value(100);
          }
        }
        """
      And register as follows:
        """
        jFactory.register(BeanSpec.class);
        """
      When "collector" collect and build with the following properties:
        """
        ::this(BeanSpec).value2= 200
        """
      Then the result should be:
        """
        = {
          value1= 100
          value2= 200
        }
        """

    Scenario: Specify Traits and Spec
      Given the following bean definition:
        """
        public class Bean {
          public String value1, value2, value3, value4;
        }
        """
      Given the following spec definition:
        """
        public class BeanSpec extends Spec<Bean> {
          public void main() {
            property("value1").value("from-main");
          }

          @Trait
          public void trait1() {
            property("value2").value("from-trait1");
          }

          @Trait
          public void trait2() {
            property("value3").value("from-trait2");
          }
        }
        """
      And register as follows:
        """
        jFactory.register(BeanSpec.class);
        """
      When "collector" collect and build with the following properties:
        """
        ::this(trait1 trait2 BeanSpec).value4= from-input
        """
      Then the result should be:
        """
        = {
          value1= from-main
          value2= from-trait1
          value3= from-trait2
          value4= from-input
        }
        """

    Scenario: Supported delimiter of Traits and Spec
      Given the following bean definition:
        """
        public class Bean {
          public String value1, value2, value3, value4;
        }
        """
      Given the following spec definition:
        """
        public class BeanSpec extends Spec<Bean> {
          public void main() {
            property("value1").value("from-main");
          }

          @Trait
          public void trait1() {
            property("value2").value("from-trait1");
          }

          @Trait
          public void trait2() {
            property("value3").value("from-trait2");
          }
        }
        """
      And register as follows:
        """
        jFactory.register(BeanSpec.class);
        """
      When "collector" collect and build with the following properties:
        """
        ::this(trait1, trait2,BeanSpec).value4= from-input
        """
      Then the result should be:
        """
        = {
          value1= from-main
          value2= from-trait1
          value3= from-trait2
          value4= from-input
        }
        """

    Scenario: Specify Spec of a different Type with Default Type
      Given the following bean definition:
        """
        public class Bean {}
        """
      Given the following bean definition:
        """
        public class AnotherBean {
          public int value1, value2;
        }
        """
      Given the following spec definition:
        """
        public class AnotherBeanSpec extends Spec<AnotherBean> {
          public void main() {
            property("value1").value(100);
          }
        }
        """
      And register as follows:
        """
        jFactory.register(AnotherBeanSpec.class);
        """
      When "collector" collect and build with the following properties:
        """
        ::this(AnotherBeanSpec).value2= 200
        """
      Then the result should be:
        """
        = {
          value1= 100
          value2= 200
          class.simpleName= AnotherBean
        }
        """

    Scenario: Support use : {...} to create Default Object
      Given the following bean definition:
        """
        public class Bean {
          public String value;
        }
        """
      When "collector" collect and build with the following properties:
        """
        : {...}
        """
      Then the result should be:
        """
        : {
          value= /^value.*/
          class.simpleName= Bean
        }
        """

    Scenario: Support use = {} to create Default Object
      Given the following bean definition:
        """
        public class Bean {
          public String value;
        }
        """
      When "collector" collect and build with the following properties:
        """
        = {}
        """
      Then the result should be:
        """
        : {
          value= /^value.*/
          class.simpleName= Bean
        }
        """

  Rule: Flat Object for Default Spec

    Scenario: Simple Object - Collect and Build Object with Default Spec of Simple Object
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
            property("value1").value("hello");
          }
        }
        """
      And the following declarations:
        """
        Collector collector = new JFactory() {{
          register(BeanSpec.class);
        }}.collector("BeanSpec");
        """
      When "collector" collect and build with the following properties:
        """
        value2= world
        """
      Then the result should be:
        """
        = {
          value1= hello
          value2= world
        }
        """

    Scenario: Specify Traits and Spec
      Given the following bean definition:
        """
        public class Bean {
          public String value1, value2, value3;
        }
        """
      Given the following spec definition:
        """
        public class BeanSpec extends Spec<Bean> {
          public void main() {
            property("value1").value("from-main");
          }

          @Trait
          public void trait() {
            property("value2").value("from-trait");
          }
        }
        """
      And the following declarations:
        """
        Collector collector = new JFactory() {{
          register(BeanSpec.class);
        }}.collector("trait", "BeanSpec");
        """
      When "collector" collect and build with the following properties:
        """
        value3= from-input
        """
      Then the result should be:
        """
        = {
          value1= from-main
          value2= from-trait
          value3= from-input
        }
        """

    Scenario: Specify Another Spec
      Given the following bean definition:
        """
        public class Bean {}
        """
      Given the following spec definition:
        """
        public class BeanSpec extends Spec<Bean> {}
        """
      Given the following bean definition:
        """
        public class AnotherBean {
          public int value1, value2;
        }
        """
      Given the following spec definition:
        """
        public class AnotherBeanSpec extends Spec<AnotherBean> {
          public void main() {
            property("value1").value(100);
          }
        }
        """
      And the following declarations:
        """
        Collector collector = new JFactory() {{
          register(BeanSpec.class);
          register(AnotherBeanSpec.class);
        }}.collector("BeanSpec");
        """
      When "collector" collect and build with the following properties:
        """
        ::this(AnotherBeanSpec).value2= 200
        """
      Then the result should be:
        """
        = {
          value1= 100
          value2= 200
          class.simpleName= AnotherBean
        }
        """

  Rule: Nested Object(a.b) by Default Type

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
        }
        """
      Given the following declarations:
        """
        Collector collector = jFactory.collector(Product.class);
        """

    Scenario: Collect and Build With Parent Property
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

    Scenario: Collect and Build With Parent and Child Property
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

    Scenario: Collect and Build With all Default Properties
      When "collector" collect and build with the following properties:
        """
        = {...}
        """
      Then the result should be:
        """
        = {
          category= null
          name= /^name.*/
        }
        """

    Scenario: Specify Spec
      Given the following spec definition:
        """
        public class ProductSpec extends Spec<Product> {
          public void main() {
              property("name").value("Laptop");
          }
        }
        """
      And register as follows:
        """
        jFactory.register(ProductSpec.class);
        """
      When "collector" collect and build with the following properties:
        """
        ::this(ProductSpec).category= {
          name= Electronics
        }
        """
      Then the result should be:
        """
        = {
          category= {
              name= Electronics
          }
          name= Laptop
        }
        """

  Rule: Nested Object([n].b) by Default Type

    Background:
      Given the following declarations:
        """
        JFactory jFactory = new JFactory();
        """
      Given the following bean definition:
        """
        public class Product {
          public String name;
        }
        """

  Rule: Flat Map / List

    Background:
      Given the following declarations:
        """
        JFactory jFactory = new JFactory();
        """
      Given the following declarations:
        """
        Collector collector = jFactory.collector();
        """

    Scenario: create a LinkedHashMap for Type Object
      Given the following declarations:
        """
        Collector mapCollector = jFactory.collector();
        """
      When "mapCollector" collect and build with the following properties:
        """
        key= value
        """
      Then the result should be:
        """
        = {
          key= value
          ::object.class.simpleName= LinkedHashMap
        }
        """

    Scenario: use : {...} to create Empty Map
      Given the following declarations:
        """
        Collector mapCollector = jFactory.collector();
        """
      When "mapCollector" collect and build with the following properties:
        """
        : {...}
        """
      Then the result should be:
        """
        : {
          ::this= {}
          ::object.class.simpleName= LinkedHashMap
        }
        """

    Scenario: use = {} to create Empty Map
      Given the following declarations:
        """
        Collector mapCollector = jFactory.collector();
        """
      When "mapCollector" collect and build with the following properties:
        """
        = {}
        """
      Then the result should be:
        """
        : {
          ::this= {}
          ::object.class.simpleName= LinkedHashMap
        }
        """

    Scenario: create a Collection for Type Object
      Given the following declarations:
        """
        Collector listCollector = jFactory.collector();
        """
      When "listCollector" collect and build with the following properties:
        """
        = [hello world]
        """
      Then the result should be:
        """
        : {
          ::this= [hello world]
          ::object.class.simpleName= Object[]
        }
        """

    Scenario: Support use : [] to create Default Collection
      Given the following declarations:
        """
        Collector listCollector = jFactory.collector();
        """
      When "listCollector" collect and build with the following properties:
        """
        = []
        """
      Then the result should be:
        """
        : {
          ::this= []
          ::object.class.simpleName= Object[]
        }
        """


  Rule: Relation of Sub Element for Default Type ([0].a)

    Background:
      Given the following declarations:
        """
        JFactory jFactory = new JFactory();
        """
      Given the following bean definition:
        """
        public class Bean {
          public String value;
        }
        """

#    @import(java.util.*)
#    Scenario: Collect and Build With Element property
#      Given the following declarations:
#        """
#        Collector collector = jFactory.collector(List.class);
#        """
#      When "collector" collect and build with the following properties:
#        """
#        [0].value= hello
#        """
#      Then the result should be:
#        """
#        = [{value= hello}]
#        """

#TODO support {...}
#TODO default Type
#TODO input spec > default type
#TODO default Spec
#TODO input spec > default spec

#TODO default type Object.class, dal object => map
#TODO dal list > all => list
#TODO intently creation

#TODO error: mixed list and map
#TODO List: whole list, part of list(has negative index)
