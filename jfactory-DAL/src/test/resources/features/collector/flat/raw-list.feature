Feature: Flat Raw List
  Create List by Collector

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

    Scenario: create a Collection
      When "collector" collect and build with the following properties:
        """
        : [hello world]
        """
      Then the result should be:
        """
        : {
          ::this= [hello world]
          ::object.class.simpleName= ArrayList
        }
        """

    Scenario: use = to create Collection
      When "collector" collect and build with the following properties:
        """
        = [hello world]
        """
      Then the result should be:
        """
        : {
          ::this= [hello world]
          ::object.class.simpleName= ArrayList
        }
        """

    Scenario: use : [] to create Default Collection
      When "collector" collect and build with the following properties:
        """
        : []
        """
      Then the result should be:
        """
        : {
          ::this= []
          ::object.class.simpleName= ArrayList
        }
        """

    Scenario: use = [] to create Default Collection
      When "collector" collect and build with the following properties:
        """
        = []
        """
      Then the result should be:
        """
        : {
          ::this= []
          ::object.class.simpleName= ArrayList
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

    Scenario: List
      When "collector" collect and build with the following properties:
        """
        = [hello world]
        """
      Then the result should be:
        """
        : {
          ::this= [hello world]
          ::object.class.simpleName= ArrayList
        }
        """

    Scenario: Empty List
      When "collector" collect and build with the following properties:
        """
        = []
        """
      Then the result should be:
        """
        : {
          ::this= []
          ::object.class.simpleName= ArrayList
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

    Scenario: List
      When "collector" collect and build with the following properties:
        """
        = [hello world]
        """
      Then the result should be:
        """
        : {
          ::this= [hello world]
          ::object.class.simpleName= ArrayList
        }
        """

    Scenario: Empty List
      When "collector" collect and build with the following properties:
        """
        = []
        """
      Then the result should be:
        """
        : {
          ::this= []
          ::object.class.simpleName= ArrayList
        }
        """

  Rule: Raise Error When Given Spec and Force to List

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

    Scenario: List
      When "collector" collect and build with the following properties:
        """
        ::this(BeanSpec)= [hello world]
        """
      Then the result should be:
        """
        ::throw.message::should.contains : ```
                                          ::this(BeanSpec)= [hello world]
                                                            ^

                                          java.lang.IllegalStateException: Cannot create raw Map/List when traits were specified
                                          ```
        """

    Scenario: Empty List
      When "collector" collect and build with the following properties:
        """
        ::this(BeanSpec)= []
        """
      Then the result should be:
        """
        ::throw.message::should.contains : ```
                                          ::this(BeanSpec)= []
                                                            ^

                                          java.lang.IllegalStateException: Cannot create raw Map/List when traits were specified
                                          ```
        """
