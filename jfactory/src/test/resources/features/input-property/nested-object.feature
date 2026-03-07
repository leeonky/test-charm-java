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

    Scenario: Input Empty Map will Create Object with all Properties by Default For Top List
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

    Scenario: Input Empty Map will Query any Exist Sub Objects First For Top List
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

  Rule: Without Only Force Creation

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

    Scenario: Specify ! to Force Create Object for Top List
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

    Scenario: Will Not Query when Specify ! for Top List
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

  Rule: Default Creation With Only Spec

    Background:
      Given the following class definition:
        """
        public class Bean {
          public Object sub;
        }
        """
      Given the following class definition:
        """
        public class Sub {
          public String value1, value2;
        }
        """
      Given the following spec definition:
        """
        public class SubSpec extends Spec<Sub> {
          @Trait
          public void v1() {
            property("value1").value("v1");
          }
        }
        """
      And register as follows:
        """
        jFactory.register(SubSpec.class);
        """

    Scenario: Input Empty Map will Create Object with all Properties by Default
      When evaluating the following code:
        """
        jFactory.type(Bean.class)
          .property("sub(SubSpec)", new HashMap())
          .create();
        """
      Then the result should be:
        """
        sub.class.simpleName= Sub
        """

    Scenario: With Trait Input Empty Map will Create Object with all Properties by Default
      When evaluating the following code:
        """
        jFactory.type(Bean.class)
          .property("sub(v1 SubSpec)", new HashMap())
          .create();
        """
      Then the result should be:
        """
        sub: {
          value1= v1
          class.simpleName= Sub
        }
        """

    Scenario: Input Empty Map will Query any Exist Sub Objects First
      Given register as follows:
        """
        jFactory.create(Sub.class);
        jFactory.register(SubSpec.class);
        """
      When evaluating the following code:
        """
        jFactory.type(Bean.class)
          .property("sub(SubSpec)", new HashMap())
          .create();
        """
      Then the result should be:
        """
        sub.class.simpleName: Sub
        """
      And the field "jFactory" should be:
        """
        SubSpec: [{class.simpleName: Sub}]
        """

    Scenario: Input Empty Map will Create Object with all Properties by Default For Top List
      When evaluating the following code:
        """
        jFactory.type(Object[].class)
          .property("[0](SubSpec)", new HashMap())
          .create();
        """
      Then the result should be:
        """
        : [{ class.simpleName= Sub }]
        """

    Scenario: With Trait Input Empty Map will Create Object with all Properties by Default For Top List
      When evaluating the following code:
        """
        jFactory.type(Object[].class)
          .property("[0](v1 SubSpec)", new HashMap())
          .create();
        """
      Then the result should be:
        """
        : [{
          value1= v1
          class.simpleName= Sub
        }]
        """

    Scenario: Input Empty Map will Query any Exist Sub Objects First for Top List
      Given register as follows:
        """
        jFactory.create(Sub.class);
        """
      When evaluating the following code:
        """
        jFactory.type(Object[].class)
          .property("[0](SubSpec)", new HashMap())
          .create();
        """
      Then the result should be:
        """
        : [{class.simpleName: Sub}]
        """
      And the field "jFactory" should be:
        """
        SubSpec: [{class.simpleName: Sub}]
        """

  Rule: Default Creation With Spec and Force

    Background:
      Given the following class definition:
        """
        public class Bean {
          public Object sub;
        }
        """
      Given the following class definition:
        """
        public class Sub {
          public String value1, value2;
        }
        """
      Given the following spec definition:
        """
        public class SubSpec extends Spec<Sub> {
          @Trait
          public void v1() {
            property("value1").value("v1");
          }
        }
        """
      And register as follows:
        """
        jFactory.register(SubSpec.class);
        """

    Scenario: Force Create Object by Spec and Force
      When evaluating the following code:
        """
        jFactory.type(Bean.class)
          .property("sub(SubSpec)!", null)
          .create();
        """
      Then the result should be:
        """
        sub= {
          value1= /^value1.*/
          value2= /^value2.*/
          class.simpleName= Sub
        }
        """

    Scenario: Will Not Query when Specify !
      Given register as follows:
        """
        jFactory.create(Sub.class);
        """
      When evaluating the following code:
        """
        jFactory.type(Bean.class)
          .property("sub(SubSpec)!", null)
          .create();
        """
      Then the result should be:
        """
        sub= {
          value1= /^value1.*/
          value2= /^value2.*/
          class.simpleName= Sub
        }
        """
      And the field "jFactory" should be:
        """
        SubSpec::size= 2
        """

    Scenario: Specify ! to Force Create Object for Top List
      When evaluating the following code:
        """
        jFactory.type(Object[].class)
          .property("[0](SubSpec)!", null)
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

    Scenario: Will Not Query when Specify ! for Top List
      Given register as follows:
        """
        jFactory.create(Sub.class);
        """
      When evaluating the following code:
        """
        jFactory.type(Object[].class)
          .property("[0](SubSpec)!", null)
          .create();
        """
      Then the result should be:
        """
        : [{
          value1= /^value1.*/
          value2= /^value2.*/
          class.simpleName= Sub
        }]
        """
      And the field "jFactory" should be:
        """
        SubSpec::size= 2
        """

  Rule: With Sub Properties

    Background:
      Given the following class definition:
        """
        public class Bean {
          public Sub sub;
          public String value1, value2;
        }
        """
      Given the following class definition:
        """
        public class Sub {
          public String subValue1, subValue2;
        }
        """

    Scenario: Create with Single Sub Property
      When evaluating the following code:
        """
        jFactory.type(Bean.class)
          .property("sub.subValue1", "v1")
          .create();
        """
      Then the result should be:
        """
        sub: {
          subValue1= v1
        }
        """

    Scenario: Create with Multiple Sub Properties
      When evaluating the following code:
        """
        jFactory.type(Bean.class)
          .property("sub.subValue1", "v1")
          .property("sub.subValue2", "v2")
          .create();
        """
      Then the result should be:
        """
        sub: {
          subValue1= v1
          subValue2= v2
        }
        """

    Scenario: Query with Single Sub Property
      Given register as follows:
        """
        jFactory.type(Bean.class)
          .property("value1", "bean1")
          .property("sub", jFactory.type(Sub.class).property("subValue1", "v1").create())
          .create();
        """
      When evaluating the following code:
        """
        jFactory.type(Bean.class).property("sub.subValue1", "v1").queryAll()
        """
      Then the result should be:
        """
        : [{
          value1= bean1
          sub.subValue1= v1
        }]
        """

    Scenario: Query with Single Sub Property
      Given register as follows:
        """
        jFactory.type(Bean.class)
          .property("value1", "bean1")
          .property("sub", jFactory.type(Sub.class)
            .property("subValue1", "v1")
            .property("subValue2", "v2")
            .create())
          .create();
        """
      When evaluating the following code:
        """
        jFactory.type(Bean.class)
          .property("sub.subValue1", "v1")
          .property("sub.subValue2", "v2")
          .queryAll()
        """
      Then the result should be:
        """
        : [{
          value1= bean1
          sub.subValue1= v1
          sub.subValue2= v2
        }]
        """
