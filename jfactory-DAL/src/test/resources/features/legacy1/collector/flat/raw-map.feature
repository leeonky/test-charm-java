Feature: Flat Raw Map
  Create Map by Collector

  Rule: Default Collector

    Background:
      Given the following declarations:
        """
        JFactory jFactory = new JFactory();
        """
      Given the following declarations:
        """
        Collector collector = jFactory.collector();
        """

    Scenario: create a Map
      When "collector" collect and build with the following properties:
        """
        : {key= value}
        """
      Then the result should be:
        """
        = {
          key= value
          ::object.class.simpleName= LinkedHashMap
        }
        """

    Scenario: use = to create Map
      When "collector" collect and build with the following properties:
        """
        = {key= value}
        """
      Then the result should be:
        """
        = {
          key= value
          ::object.class.simpleName= LinkedHashMap
        }
        """

    Scenario: use : {...} to create Empty Map
      When "collector" collect and build with the following properties:
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
      When "collector" collect and build with the following properties:
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

  Rule: use = to Force Create List from Collect by Type

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

    Scenario: Map
      When "collector" collect and build with the following properties:
        """
        = {hello: world}
        """
      Then the result should be:
        """
        : {
          hello= world
          ::object.class.simpleName= LinkedHashMap
        }
        """

    Scenario: Empty Map
      When "collector" collect and build with the following properties:
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

  Rule: use = to Force Create List from Collector by Spec

    Background:
      Given the following declarations:
        """
        JFactory jFactory = new JFactory();
        """
      Given the following bean definition:
        """
        public class Bean {}
        """
      Given the following spec definition:
        """
        public class BeanSpec extends Spec<Bean> {}
        """
      Given the following declarations:
        """
        Collector collector = jFactory.collector(BeanSpec.class);
        """

    Scenario: Map
      When "collector" collect and build with the following properties:
        """
        = {hello: world}
        """
      Then the result should be:
        """
        : {
          hello= world
          ::object.class.simpleName= LinkedHashMap
        }
        """

    Scenario: Empty Map
      When "collector" collect and build with the following properties:
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

  Rule: Raise Error When Given Spec and Force to Map

    Background:
      Given the following declarations:
        """
        JFactory jFactory = new JFactory();
        """
      Given the following bean definition:
        """
        public class Bean {}
        """
      Given the following spec definition:
        """
        public class BeanSpec extends Spec<Bean> {}
        """
      Given the following declarations:
        """
        Collector collector = jFactory.collector();
        """

    Scenario: Map
      When "collector" collect and build with the following properties:
        """
        ::this(BeanSpec)= {hello: world}
        """
      Then the result should be:
        """
        ::throw.message::should.contains : ```
                                          ::this(BeanSpec)= {hello: world}
                                                            ^

                                          java.lang.IllegalStateException: Cannot create raw Map/List when traits were specified
                                          ```
        """

    Scenario: Empty Map
      When "collector" collect and build with the following properties:
        """
        ::this(BeanSpec)= {}
        """
      Then the result should be:
        """
        ::throw.message::should.contains : ```
                                          ::this(BeanSpec)= {}
                                                            ^

                                          java.lang.IllegalStateException: Cannot create raw Map/List when traits were specified
                                          ```
        """
