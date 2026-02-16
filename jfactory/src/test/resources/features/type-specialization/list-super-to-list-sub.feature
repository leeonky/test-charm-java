Feature: List Super => List Sub
  Specify element property to Sub Type

  Background:
    Given the following declarations:
      """
      JFactory jFactory = new JFactory();
      """
    Given the following bean definition:
      """
      public class Super {}
      """
    And the following bean definition:
      """
      public class Sub extends Super{
        public String value1, value2;
      }
      """

  Rule: In Parent Spec by is

    Background:
      Given the following spec definition:
        """
        public class BeanSpec extends Spec<Bean> {
          public void main() {
            property("list").is(ListSubSpec.class);
          }
        }
        """

    Scenario Outline: Create Default Empty List without Input Properties
      Given the following spec definition:
        """
        public class ListSubSpec extends Spec<<specType>> {}
        """
      And the following bean definition:
        """
        public class Bean {
          public <type> list;
        }
        """
      When evaluating the following code:
        """
        jFactory.spec(BeanSpec.class).create();
        """
      Then the result should be:
        """
        : {
          list= []
          list.class.simpleName= '<actualListType>'
        }
        """
      Examples:
        | type        | specType  | actualListType |
        | List        | List<Sub> | ArrayList      |
        | List<?>     | List<Sub> | ArrayList      |
        | List<Super> | List<Sub> | ArrayList      |
        | Object[]    | Sub[]     | Sub[]          |
        | Super[]     | Sub[]     | Sub[]          |

    Scenario Outline: Create Default with Specified Default Sub
      Given the following spec definition:
        """
        public class ListSubSpec extends Spec<<specType>> {}
        """
      And the following bean definition:
        """
        public class Bean {
          public <type> list;
        }
        """
      When evaluating the following code:
        """
        jFactory.spec(BeanSpec.class).property("list[0]", new HashMap<>()).create();
        """
      Then the result should be:
        """
        : {
          list= [{
            value1= /^value1.*/
            value2= /^value2.*/
            class.simpleName= Sub
          }]
          list.class.simpleName= '<actualListType>'
        }
        """
      Examples:
        | type        | specType  | actualListType |
        | List        | List<Sub> | ArrayList      |
        | List<?>     | List<Sub> | ArrayList      |
        | List<Super> | List<Sub> | ArrayList      |
        | Object[]    | Sub[]     | Sub[]          |
        | Super[]     | Sub[]     | Sub[]          |

    Scenario Outline: Create with Sub Property
      Given the following spec definition:
        """
        public class ListSubSpec extends Spec<<specType>> {}
        """
      And the following bean definition:
        """
        public class Bean {
          public <type> list;
        }
        """
      When evaluating the following code:
        """
        jFactory.spec(BeanSpec.class).property("list[0].value1", "v1").create();
        """
      Then the result should be:
        """
        : {
          list= [{
            value1= v1
            value2= /^value2.*/
            class.simpleName= Sub
          }]
          list.class.simpleName= '<actualListType>'
        }
        """
      Examples:
        | type        | specType  | actualListType |
        | List        | List<Sub> | ArrayList      |
        | List<?>     | List<Sub> | ArrayList      |
        | List<Super> | List<Sub> | ArrayList      |
        | Object[]    | Sub[]     | Sub[]          |
        | Super[]     | Sub[]     | Sub[]          |

    Scenario Outline: Create with Sub Property Query
      Given the following spec definition:
        """
        public class ListSubSpec extends Spec<<specType>> {}
        """
      And the following bean definition:
        """
        public class Bean {
          public <type> list;
        }
        """
      Given register as follows:
        """
        jFactory.type(Sub.class).property("value1", "v1").property("value2", "v2").create();
        """
      When evaluating the following code:
        """
        jFactory.spec(BeanSpec.class).property("list[0].value1", "v1").create();
        """
      Then the result should be:
        """
        : {
          list= [{
            value1= v1
            value2= v2
          }]
          list.class.simpleName= '<actualListType>'
        }
        """
      Examples:
        | type        | specType  | actualListType |
        | List        | List<Sub> | ArrayList      |
        | List<?>     | List<Sub> | ArrayList      |
        | List<Super> | List<Sub> | ArrayList      |
        | Object[]    | Sub[]     | Sub[]          |
        | Super[]     | Sub[]     | Sub[]          |

    Scenario Outline: Query with Sub Property
      Given the following spec definition:
        """
        public class ListSubSpec extends Spec<<specType>> {}
        """
      And the following bean definition:
        """
        public class Bean {
          public <type> list;
        }
        """
      Given register as follows:
        """
        Sub sub = jFactory.type(Sub.class).property("value1", "v1").property("value2", "v2").create();
        jFactory.type(Bean.class).property("list[0]", sub).create();
        """
      When evaluating the following code:
        """
        jFactory.spec(BeanSpec.class).property("list[0].value1", "v1").query();
        """
      Then the result should be:
        """
        : {
          list= [{
            value1= v1
            value2= v2
          }]
          list.class.simpleName= '<actualListType>'
        }
        """
      Examples:
        | type        | specType  | actualListType |
        | List        | List<Sub> | ArrayList      |
        | List<?>     | List<Sub> | ArrayList      |
        | List<Super> | List<Sub> | ArrayList      |
        | Object[]    | Sub[]     | Object[]       |
        | Super[]     | Sub[]     | Super[]        |

  Rule: In Parent Spec by is []

    Background:
      Given the following spec definition:
        """
        public class SubSpec extends Spec<Sub> {}
        """
      And register as follows:
        """
        jFactory.register(SubSpec.class);
        """
      And the following spec definition:
        """
        public class BeanSpec extends Spec<Bean> {
          public void main() {
            property("list[]").is("SubSpec");
          }
        }
        """

    Scenario Outline: Create Default Empty List without Input Properties
      Given the following bean definition:
        """
        public class Bean {
          public <type> list;
        }
        """
      When evaluating the following code:
        """
        jFactory.spec(BeanSpec.class).create();
        """
      Then the result should be:
        """
        : {
          list= []
          list.class.simpleName= '<actualListType>'
        }
        """
      Examples:
        | type        | actualListType |
        | List        | ArrayList      |
        | List<?>     | ArrayList      |
        | List<Super> | ArrayList      |
        | Object[]    | Object[]       |
        | Super[]     | Super[]        |

    Scenario Outline: Create Default with Specified Default Sub
      Given the following bean definition:
        """
        public class Bean {
          public <type> list;
        }
        """
      When evaluating the following code:
        """
        jFactory.spec(BeanSpec.class).property("list[0]", new HashMap<>()).create();
        """
      Then the result should be:
        """
        : {
          list= [{
            value1= /^value1.*/
            value2= /^value2.*/
            class.simpleName= Sub
          }]
          list.class.simpleName= '<actualListType>'
        }
        """
      Examples:
        | type        | actualListType |
        | List        | ArrayList      |
        | List<?>     | ArrayList      |
        | List<Super> | ArrayList      |
        | Object[]    | Object[]       |
        | Super[]     | Super[]        |

    Scenario Outline: Create with Sub Property
      Given the following bean definition:
        """
        public class Bean {
          public <type> list;
        }
        """
      When evaluating the following code:
        """
        jFactory.spec(BeanSpec.class).property("list[0].value1", "v1").create();
        """
      Then the result should be:
        """
        : {
          list: [{
            value1= v1
            class.simpleName= Sub
          }]
          list.class.simpleName= '<actualListType>'
        }
        """
      Examples:
        | type        | actualListType |
        | List        | ArrayList      |
        | List<?>     | ArrayList      |
        | List<Super> | ArrayList      |
        | Object[]    | Object[]       |
        | Super[]     | Super[]        |

    Scenario Outline: Create with Sub Property Query
      Given register as follows:
        """
        jFactory.type(Sub.class).property("value1", "v1").property("value2", "v2").create();
        """
      Given the following bean definition:
        """
        public class Bean {
          public <type> list;
        }
        """
      When evaluating the following code:
        """
        jFactory.spec(BeanSpec.class).property("list[0].value1", "v1").create();
        """
      Then the result should be:
        """
        : {
          list= [{
            value1= v1
            value2= v2
          }]
          list.class.simpleName= '<actualListType>'
        }
        """
      Examples:
        | type        | actualListType |
        | List        | ArrayList      |
        | List<?>     | ArrayList      |
        | List<Super> | ArrayList      |
        | Object[]    | Object[]       |
        | Super[]     | Super[]        |

    Scenario Outline: Query with Sub Property
      Given the following bean definition:
        """
        public class Bean {
          public <type> list;
        }
        """
      Given register as follows:
        """
        Sub sub = jFactory.type(Sub.class).property("value1", "v1").property("value2", "v2").create();
        jFactory.type(Bean.class).property("list[0]", sub).create();
        """
      When evaluating the following code:
        """
        jFactory.spec(BeanSpec.class).property("list[0].value1", "v1").query();
        """
      Then the result should be:
        """
        : {
          list= [{
            value1= v1
            value2= v2
          }]
          list.class.simpleName= '<actualListType>'
        }
        """
      Examples:
        | type        | actualListType |
        | List        | ArrayList      |
        | List<?>     | ArrayList      |
        | List<Super> | ArrayList      |
        | Object[]    | Object[]       |
        | Super[]     | Super[]        |

  Rule: Input Property Spec

    Background:
      Given the following spec definition:
        """
        public class SubSpec extends Spec<Sub> {}
        """
      And register as follows:
        """
        jFactory.register(SubSpec.class);
        """

    Scenario Outline: Create Default with Specified Default Sub
      Given the following bean definition:
        """
        public class Bean {
          public <type> list;
        }
        """
      When evaluating the following code:
        """
        jFactory.type(Bean.class).property("list[0](SubSpec)", new HashMap()).create();
        """
      Then the result should be:
        """
        : {
          list= [{
            value1= /^value1.*/
            value2= /^value2.*/
            class.simpleName= Sub
          }]
          list.class.simpleName= '<actualListType>'
        }
        """
      Examples:
        | type        | actualListType |
        | List        | ArrayList      |
        | List<?>     | ArrayList      |
        | List<Super> | ArrayList      |
        | Object[]    | Object[]       |
        | Super[]     | Super[]        |

    Scenario Outline: Create Default with Sub Properties
      Given the following bean definition:
        """
        public class Bean {
          public <type> list;
        }
        """
      When evaluating the following code:
        """
        jFactory.type(Bean.class).property("list[0](SubSpec).value1", "v1").create();
        """
      Then the result should be:
        """
        : {
          list: [{
            value1= v1
            class.simpleName= Sub
          }]
          list.class.simpleName= '<actualListType>'
        }
        """
      Examples:
        | type        | actualListType |
        | List        | ArrayList      |
        | List<?>     | ArrayList      |
        | List<Super> | ArrayList      |
        | Object[]    | Object[]       |
        | Super[]     | Super[]        |

    Scenario Outline: Create with Sub Property
      Given the following bean definition:
        """
        public class Bean {
          public <type> list;
        }
        """
      When evaluating the following code:
        """
        jFactory.type(Bean.class).property("list[0](SubSpec).value1", "v1").create();
        """
      Then the result should be:
        """
        : {
          list: [{
            value1= v1
            class.simpleName= Sub
          }]
          list.class.simpleName= '<actualListType>'
        }
        """
      Examples:
        | type        | actualListType |
        | List        | ArrayList      |
        | List<?>     | ArrayList      |
        | List<Super> | ArrayList      |
        | Object[]    | Object[]       |
        | Super[]     | Super[]        |

    Scenario Outline: Create with Sub Property Query
      Given the following bean definition:
        """
        public class Bean {
          public <type> list;
        }
        """
      Given register as follows:
        """
        jFactory.type(Sub.class).property("value1", "v1").property("value2", "v2").create();
        """
      When evaluating the following code:
        """
        jFactory.type(Bean.class).property("list[0](SubSpec).value1", "v1").create();
        """
      Then the result should be:
        """
        : {
          list: [{
            value1= v1
            value2= v2
            class.simpleName= Sub
          }]
          list.class.simpleName= '<actualListType>'
        }
        """
      Examples:
        | type        | actualListType |
        | List        | ArrayList      |
        | List<?>     | ArrayList      |
        | List<Super> | ArrayList      |
        | Object[]    | Object[]       |
        | Super[]     | Super[]        |

    Scenario Outline: Query with Sub Property
      Given the following bean definition:
        """
        public class Bean {
          public <type> list;
        }
        """
      Given register as follows:
        """
        Sub sub = jFactory.type(Sub.class).property("value1", "v1").property("value2", "v2").create();
        jFactory.type(Bean.class).property("list[0]", sub).create();
        """
      When evaluating the following code:
        """
        jFactory.type(Bean.class).property("list[0](SubSpec).value1", "v1").query();
        """
      Then the result should be:
        """
        : {
          list: [{
            value1= v1
            value2= v2
            class.simpleName= Sub
          }]
          list.class.simpleName= '<actualListType>'
        }
        """
      Examples:
        | type        | actualListType |
        | List        | ArrayList      |
        | List<?>     | ArrayList      |
        | List<Super> | ArrayList      |
        | Object[]    | Object[]       |
        | Super[]     | Super[]        |
