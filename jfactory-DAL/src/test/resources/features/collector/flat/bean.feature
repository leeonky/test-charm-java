Feature: Flat Object
  Use `: {}` to Create Object by JFactory

  Rule: By Default Type

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
      When "collector" collect with the following properties:
        """
        value= hello
        """
      Then the result should be:
        """
        : {
          ::properties= {
            value= hello
          }
          ::build= {
            value= hello
          }
        }
        """

    Scenario: Multiple Properties - Collect and Build Flat Object by Default Type and Multiple Properties
      Given the following bean definition:
        """
        public class Bean {
          public String value1, value2;
        }
        """
      When "collector" collect with the following properties:
        """
        : {
          value1= hello
          value2= world
        }
        """
      Then the result should be:
        """
        : {
          ::properties= {
            value1= hello
            value2= world
          }
          ::build= {
            value1= hello
            value2= world
          }
        }
        """

    Scenario: Matching and Equal Opt has no difference for Property Value Assignment
      Given the following bean definition:
        """
        public class Bean {
          public int value1, value2;
        }
        """
      When "collector" collect with the following properties:
        """
        : {
          value1= 100
          value2: 200
        }
        """
      Then the result should be:
        """
        : {
          ::properties= {
            value1= 100
            value2= 200
          }
          ::build= {
            value1= 100
            value2= 200
          }
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
      When "collector" collect with the following properties:
        """
        ::this(BeanSpec).value2= 200
        """
      Then the result should be:
        """
        : {
          ::properties= {
            'value2'= 200
          }
          ::build= {
            value1= 100
            value2= 200
          }
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
      When "collector" collect with the following properties:
        """
        ::this(trait1 trait2 BeanSpec).value4= from-input
        """
      Then the result should be:
        """
        : {
          ::properties= {
            value4= from-input
          }
          ::build= {
            value1= from-main
            value2= from-trait1
            value3= from-trait2
            value4= from-input
          }
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
      When "collector" collect with the following properties:
        """
        ::this(trait1, trait2,BeanSpec).value4= from-input
        """
      Then the result should be:
        """
        : {
          ::properties= {
            value4= from-input
          }
          ::build= {
            value1= from-main
            value2= from-trait1
            value3= from-trait2
            value4= from-input
          }
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
      When "collector" collect with the following properties:
        """
        ::this(AnotherBeanSpec).value2= 200
        """
      Then the result should be:
        """
        : {
          ::properties= {
            value2= 200
          }
          ::build= {
            value1= 100
            value2= 200
            class.simpleName= AnotherBean
          }
        }
        """

    Scenario: Support use : {...} to create Default Object
      Given the following bean definition:
        """
        public class Bean {
          public String value;
        }
        """
      When "collector" collect with the following properties:
        """
        : {...}
        """
      Then the result should be:
        """
        : {
          ::properties= {}
          ::build: {
            value= /^value.*/
            class.simpleName= Bean
          }
        }
        """

  Rule: By Default Spec

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
      When "collector" collect with the following properties:
        """
        value2= world
        """
      Then the result should be:
        """
        : {
          ::properties= {
            value2= world
          }
          ::build= {
            value1= hello
            value2= world
          }
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
      When "collector" collect with the following properties:
        """
        value3= from-input
        """
      Then the result should be:
        """
        : {
          ::properties= {
            value3= from-input
          }
          ::build= {
            value1= from-main
            value2= from-trait
            value3= from-input
          }
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
      When "collector" collect with the following properties:
        """
        ::this(AnotherBeanSpec).value2= 200
        """
      Then the result should be:
        """
        : {
          ::properties= {
            value2= 200
          }
          ::build= {
            value1= 100
            value2= 200
            class.simpleName= AnotherBean
          }
        }
        """

    Scenario: Support use : {...} to create Default Object with Default Spec
      Given the following bean definition:
        """
        public class Bean {
          public String value;
        }
        """
      Given the following spec definition:
        """
        public class BeanSpec extends Spec<Bean> {
          public void main() {
            property("value").value("hello");
          }
        }
        """
      And the following declarations:
        """
        Collector collector = new JFactory() {{
          register(BeanSpec.class);
        }}.collector("BeanSpec");
        """
      When "collector" collect with the following properties:
        """
        : {...}
        """
      Then the result should be:
        """
        : {
          ::properties= {}
          ::build= {
            value= hello
            class.simpleName= Bean
          }
        }
        """

  Rule: By Default Collector(Type Object) and Given Spec

    Background:
      Given the following declarations:
        """
        JFactory jFactory = new JFactory();
        """
      Given the following bean definition:
        """
        public class Bean {
          public String value1, value2, value3;
        }
        """
      Given the following declarations:
        """
        Collector collector = jFactory.collector();
        """
      Given the following spec definition:
        """
        public class BeanSpec extends Spec<Bean> {
          @Trait
          public void trait() {
            property("value2").value("from-trait");
          }
        }
        """
      And register as follows:
        """
        jFactory.register(BeanSpec.class);
        """

    Scenario: Single Property - Collect and Build Flat Object by Default Type
      When "collector" collect with the following properties:
        """
        ::this(BeanSpec): {
          value1= hello
        }
        """
      Then the result should be:
        """
        : {
          ::properties= {
            value1= hello
          }
          ::build: {
            value1= hello
            class.simpleName= Bean
          }
        }
        """

    Scenario: Specify Traits and Spec
      When "collector" collect with the following properties:
        """
        ::this(trait BeanSpec): {
          value1= hello
        }
        """
      Then the result should be:
        """
        : {
          ::properties= {
            value1= hello
          }
          ::build: {
            value1= hello
            value2= from-trait
            class.simpleName= Bean
          }
        }
        """

    Scenario: Support use : {...} to create Default Object with Default Spec
      When "collector" collect with the following properties:
        """
        ::this(trait BeanSpec): {...}
        """
      Then the result should be:
        """
        : {
          ::properties= {}
          ::build: {
            value1= /^value1.*/
            value2= from-trait
            class.simpleName= Bean
          }
        }
        """
