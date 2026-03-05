Feature: Nested

  Background:
    Given the following declarations:
      """
      JFactory jFactory = new JFactory();
      """

  Rule: Without any Postfix

    Background:
      Given the following class definition:
        """
        public class Bean {
          public Sub sub;
        }
        """
      Given the following class definition:
        """
        public class Sub {
          public String value1, value2;
        }
        """

    Scenario: Input Empty Map will Create Object with all Properties by Default
      When evaluating the following code:
        """
        jFactory.type(Bean.class)
          .property("sub", new HashMap())
          .create();
        """
      Then the result should be:
        """
        sub= {
          value1= /^value1.*/
          value2= /^value2.*/
        }
        """

    Scenario: Input Empty Map will Query any Exist Sub Objects First
      Given register as follows:
        """
        jFactory.create(Sub.class);
        jFactory.type(Bean.class)
          .property("sub", new HashMap())
          .create();
        """
      When evaluating the following code:
        """
        jFactory.type(Sub.class).queryAll()
        """
      Then the result should be:
        """
        : [{class.simpleName: Sub}]
        """

    Scenario: Input Empty Map will Create Object with all Properties by Default For List
      When evaluating the following code:
        """
        jFactory.type(Sub[].class)
          .property("[0]", new HashMap())
          .create();
        """
      Then the result should be:
        """
        = [{
          value1= /^value1.*/
          value2= /^value2.*/
          class.simpleName= Sub
        }]
        """

    Scenario: Input Empty Map will Query any Exist Sub Objects First For List
      Given register as follows:
        """
        jFactory.create(Sub.class);
        jFactory.type(Sub[].class)
          .property("[0]", new HashMap())
          .create();
        """
      When evaluating the following code:
        """
        jFactory.type(Sub.class).queryAll()
        """
      Then the result should be:
        """
        : [{class.simpleName: Sub}]
        """

  Rule: Without Force Creation

    Background:
      Given the following class definition:
        """
        public class Bean {
          public Sub sub;
        }
        """
      Given the following class definition:
        """
        public class Sub {
          public String value1, value2;
        }
        """

    Scenario: Specify ! to Force Create Object
      When evaluating the following code:
        """
        jFactory.type(Bean.class)
          .property("sub!", null)
          .create();
        """
      Then the result should be:
        """
        sub= {
          value1= /^value1.*/
          value2= /^value2.*/
        }
        """

    Scenario: Will Not Query when Specify !
      Given register as follows:
        """
        jFactory.create(Sub.class);
        jFactory.type(Bean.class)
          .property("sub!", null)
          .create();
        """
      When evaluating the following code:
        """
        jFactory.type(Sub.class).queryAll()
        """
      Then the result should be:
        """
        ::size= 2
        """

    Scenario: Specify ! to Force Create Object for List
      When evaluating the following code:
        """
        jFactory.type(Sub[].class)
          .property("[0]!", null)
          .create();
        """
      Then the result should be:
        """
        = [{
          value1= /^value1.*/
          value2= /^value2.*/
        }]
        """

    Scenario: Will Not Query when Specify ! for List
      Given register as follows:
        """
        jFactory.create(Sub.class);
        jFactory.type(Sub[].class)
          .property("[0]!", null)
          .create();
        """
      When evaluating the following code:
        """
        jFactory.type(Sub.class).queryAll()
        """
      Then the result should be:
        """
        ::size= 2
        """

