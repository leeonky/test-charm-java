Feature: Nested list element specialization via Spec
  Create a concrete subtype for a nested list element

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

  Rule: By is(...) with collection spec in the parent spec

    Background:
      Given the following spec definition:
        """
        public class BeanSpec extends Spec<Bean> {
          public void main() {
            property("list").is(ListSubSpec.class);
          }
        }
        """

    Scenario Outline: Create Root Object Without Specifying any List Element Properties
      Given the following spec definition:
        """
        public class ListSubSpec extends Spec<<specType>> {}
        """
      And the following bean definition:
        """
        public class Bean<generic> {
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
        | type        | specType  | actualListType | generic |
        | Object      | List<Sub> | ArrayList      |         |
        | Object      | Super[]   | Super[]        |         |
        | Object      | Sub[]     | Sub[]          |         |
        | T           | List<Sub> | ArrayList      | <T>     |
        | List<T>     | List<Sub> | ArrayList      | <T>     |
        | List        | List<Sub> | ArrayList      |         |
        | List<?>     | List<Sub> | ArrayList      |         |
        | List<Super> | List<Sub> | ArrayList      |         |
        | Object[]    | Sub[]     | Sub[]          |         |
        | Super[]     | Sub[]     | Sub[]          |         |

    Scenario Outline: Create Default List Element Without Specifying its Properties
      Given the following spec definition:
        """
        public class ListSubSpec extends Spec<<specType>> {}
        """
      And the following bean definition:
        """
        public class Bean<generic> {
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
        | type        | specType  | actualListType | generic |
        | Object      | List<Sub> | ArrayList      |         |
        | Object      | Sub[]     | Sub[]          |         |
        | List        | List<Sub> | ArrayList      |         |
        | List<?>     | List<Sub> | ArrayList      |         |
        | List<Super> | List<Sub> | ArrayList      |         |
        | Object[]    | Sub[]     | Sub[]          |         |
        | Super[]     | Sub[]     | Sub[]          |         |

    Scenario Outline: Intently Create Default List Element Without Specifying its Properties
      Given the following spec definition:
        """
        public class ListSubSpec extends Spec<<specType>> {}
        """
      And the following bean definition:
        """
        public class Bean<generic> {
          public <type> list;
        }
        """
      When evaluating the following code:
        """
        jFactory.spec(BeanSpec.class).property("list[0]!", null).create();
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
        | type        | specType  | actualListType | generic |
        | Object      | List<Sub> | ArrayList      |         |
        | Object      | Sub[]     | Sub[]          |         |
        | List        | List<Sub> | ArrayList      |         |
        | List<?>     | List<Sub> | ArrayList      |         |
        | List<Super> | List<Sub> | ArrayList      |         |
        | Object[]    | Sub[]     | Sub[]          |         |
        | Super[]     | Sub[]     | Sub[]          |         |

    Scenario Outline: Create List Element With Given Properties
      Given the following spec definition:
        """
        public class ListSubSpec extends Spec<<specType>> {}
        """
      And the following bean definition:
        """
        public class Bean<generic> {
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
        | type        | specType  | actualListType | generic |
        | Object      | List<Sub> | ArrayList      |         |
        | Object      | Sub[]     | Sub[]          |         |
        | T           | List<Sub> | ArrayList      | <T>     |
        | List<T>     | List<Sub> | ArrayList      | <T>     |
        | List        | List<Sub> | ArrayList      |         |
        | List<?>     | List<Sub> | ArrayList      |         |
        | List<Super> | List<Sub> | ArrayList      |         |
        | Object[]    | Sub[]     | Sub[]          |         |
        | Super[]     | Sub[]     | Sub[]          |         |

    Scenario Outline: IntentlyCreate List Element With Given Properties
      Given the following spec definition:
        """
        public class ListSubSpec extends Spec<<specType>> {}
        """
      And the following bean definition:
        """
        public class Bean<generic> {
          public <type> list;
        }
        """
      When evaluating the following code:
        """
        jFactory.spec(BeanSpec.class).property("list[0]!.value1", "v1").create();
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
        | type        | specType  | actualListType | generic |
        | Object      | List<Sub> | ArrayList      |         |
        | Object      | Sub[]     | Sub[]          |         |
        | T           | List<Sub> | ArrayList      | <T>     |
        | List<T>     | List<Sub> | ArrayList      | <T>     |
        | List        | List<Sub> | ArrayList      |         |
        | List<?>     | List<Sub> | ArrayList      |         |
        | List<Super> | List<Sub> | ArrayList      |         |
        | Object[]    | Sub[]     | Sub[]          |         |
        | Super[]     | Sub[]     | Sub[]          |         |

    Scenario Outline: Reuse Previously Created Object by Matching its Properties
      Given the following spec definition:
        """
        public class ListSubSpec extends Spec<<specType>> {}
        """
      And the following bean definition:
        """
        public class Bean<generic> {
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
        | type        | specType  | actualListType | generic |
        | Object      | List<Sub> | ArrayList      |         |
        | Object      | Sub[]     | Sub[]          |         |
        | T           | List<Sub> | ArrayList      | <T>     |
        | List<T>     | List<Sub> | ArrayList      | <T>     |
        | List        | List<Sub> | ArrayList      |         |
        | List<?>     | List<Sub> | ArrayList      |         |
        | List<Super> | List<Sub> | ArrayList      |         |
        | Object[]    | Sub[]     | Sub[]          |         |
        | Super[]     | Sub[]     | Sub[]          |         |

    Scenario Outline: Query Root Object by List Element Properties
      Given the following spec definition:
        """
        public class ListSubSpec extends Spec<<specType>> {} // Spec type not work in query
        """
      And the following bean definition:
        """
        public class Bean<generic> {
          public <type> list;
        }
        """
      Given register as follows:
        """
        Sub sub = jFactory.type(Sub.class).property("value1", "v1").property("value2", "v2").create();
        jFactory.type(Bean.class).property("list", new Object[]{sub}).create();
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
        | type        | specType  | actualListType | generic |
        | Object      | List<Sub> | Object[]       |         |
        | Object      | Sub[]     | Object[]       |         |
        | T           | List<Sub> | Object[]       | <T>     |
        | List<T>     | List<Sub> | ArrayList      | <T>     |
        | List        | List<Sub> | ArrayList      |         |
        | List<?>     | List<Sub> | ArrayList      |         |
        | List<Super> | List<Sub> | ArrayList      |         |
        | Object[]    | Sub[]     | Object[]       |         |
        | Super[]     | Sub[]     | Super[]        |         |

  Rule: By is(...) with collection element spec in the parent spec

    Background:
      Given the following spec definition:
        """
        public class SubSpec extends Spec<Sub> {}
        """
      And the following spec definition:
        """
        public class BeanSpec extends Spec<Bean> {
          public void main() {
            property("list[]").is(SubSpec.class);
          }
        }
        """

    Scenario Outline: Create Root Object Without Specifying any List Element Properties
      Given the following bean definition:
        """
        public class Bean<generic> {
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
        | type        | actualListType | generic |
        | Object      | ArrayList      |         |
        | T           | ArrayList      | <T>     |
        | List<T>     | ArrayList      | <T>     |
        | List        | ArrayList      |         |
        | List<?>     | ArrayList      |         |
        | List<Super> | ArrayList      |         |
        | Object[]    | Object[]       |         |
        | Super[]     | Super[]        |         |

    Scenario Outline: Create Default List Element Without Specifying its Properties
      Given the following bean definition:
        """
        public class Bean<generic> {
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
        | type        | actualListType | generic |
        | Object      | ArrayList      |         |
        | T           | ArrayList      | <T>     |
        | List<T>     | ArrayList      | <T>     |
        | List        | ArrayList      |         |
        | List<?>     | ArrayList      |         |
        | List<Super> | ArrayList      |         |
        | Object[]    | Object[]       |         |
        | Super[]     | Super[]        |         |

    Scenario Outline: Intently Create Default List Element Without Specifying its Properties
      Given the following bean definition:
        """
        public class Bean<generic> {
          public <type> list;
        }
        """
      When evaluating the following code:
        """
        jFactory.spec(BeanSpec.class).property("list[0]!", null).create();
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
        | type        | actualListType | generic |
        | Object      | ArrayList      |         |
        | T           | ArrayList      | <T>     |
        | List<T>     | ArrayList      | <T>     |
        | List        | ArrayList      |         |
        | List<?>     | ArrayList      |         |
        | List<Super> | ArrayList      |         |
        | Object[]    | Object[]       |         |
        | Super[]     | Super[]        |         |

    Scenario Outline: Create List Element With Given Properties
      Given the following bean definition:
        """
        public class Bean<generic> {
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
        | type        | actualListType | generic |
        | Object      | ArrayList      |         |
        | T           | ArrayList      | <T>     |
        | List<T>     | ArrayList      | <T>     |
        | List        | ArrayList      |         |
        | List<?>     | ArrayList      |         |
        | List<Super> | ArrayList      |         |
        | Object[]    | Object[]       |         |
        | Super[]     | Super[]        |         |

    Scenario Outline: Intently Create List Element With Given Properties
      Given the following bean definition:
        """
        public class Bean<generic> {
          public <type> list;
        }
        """
      When evaluating the following code:
        """
        jFactory.spec(BeanSpec.class).property("list[0]!.value1", "v1").create();
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
        | type        | actualListType | generic |
        | Object      | ArrayList      |         |
        | T           | ArrayList      | <T>     |
        | List<T>     | ArrayList      | <T>     |
        | List        | ArrayList      |         |
        | List<?>     | ArrayList      |         |
        | List<Super> | ArrayList      |         |
        | Object[]    | Object[]       |         |
        | Super[]     | Super[]        |         |

    Scenario Outline: Reuse Previously Created Element by Matching its Properties
      Given register as follows:
        """
        jFactory.type(Sub.class).property("value1", "v1").property("value2", "v2").create();
        """
      Given the following bean definition:
        """
        public class Bean<generic> {
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
        | type        | actualListType | generic |
        | Object      | ArrayList      |         |
        | T           | ArrayList      | <T>     |
        | List<T>     | ArrayList      | <T>     |
        | List        | ArrayList      |         |
        | List<?>     | ArrayList      |         |
        | List<Super> | ArrayList      |         |
        | Object[]    | Object[]       |         |
        | Super[]     | Super[]        |         |

    Scenario Outline: Query Root Object by List Element Properties
      Given the following bean definition:
        """
        public class Bean<generic> {
          public <type> list;
        }
        """
      Given register as follows:
        """
        Sub sub = jFactory.type(Sub.class).property("value1", "v1").property("value2", "v2").create();
        jFactory.type(Bean.class).property("list", new Object[]{sub}).create();
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
        | type        | actualListType | generic |
        | Object      | Object[]       |         |
        | T           | Object[]       | <T>     |
        | List<T>     | ArrayList      | <T>     |
        | List        | ArrayList      |         |
        | List<?>     | ArrayList      |         |
        | List<Super> | ArrayList      |         |
        | Object[]    | Object[]       |         |
        | Super[]     | Super[]        |         |

  Rule: By input child collection Spec

    Background:
      Given the following spec definition:
        """
        public class ListSubSpec extends Spec<<specType>> {}
        """
      And register as follows:
        """
        jFactory.register(ListSubSpec.class);
        """

    Scenario Outline: Create Default List Element Without Specifying its Properties
      Given the following spec definition:
        """
        public class ListSubSpec extends Spec<<specType>> {}
        """
      And the following bean definition:
        """
        public class Bean<generic> {
          public <type> list;
        }
        """
      When evaluating the following code:
        """
        jFactory.type(Bean.class).property("list(ListSubSpec)[0]", new HashMap<>()).create();
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
        | type        | specType  | actualListType | generic |
        | Object      | List<Sub> | ArrayList      |         |
        | Object      | Sub[]     | Sub[]          |         |
        | T           | List<Sub> | ArrayList      | <T>     |
        | List<T>     | List<Sub> | ArrayList      | <T>     |
        | List        | List<Sub> | ArrayList      |         |
        | List<?>     | List<Sub> | ArrayList      |         |
        | List<Super> | List<Sub> | ArrayList      |         |
        | Object[]    | Sub[]     | Sub[]          |         |
        | Super[]     | Sub[]     | Sub[]          |         |

    Scenario Outline: Intently Create Default List Element Without Specifying its Properties
      Given the following spec definition:
        """
        public class ListSubSpec extends Spec<<specType>> {}
        """
      And the following bean definition:
        """
        public class Bean<generic> {
          public <type> list;
        }
        """
      When evaluating the following code:
        """
        jFactory.type(Bean.class).property("list(ListSubSpec)[0]!", null).create();
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
        | type        | specType  | actualListType | generic |
        | Object      | List<Sub> | ArrayList      |         |
        | Object      | Sub[]     | Sub[]          |         |
        | T           | List<Sub> | ArrayList      | <T>     |
        | List<T>     | List<Sub> | ArrayList      | <T>     |
        | List        | List<Sub> | ArrayList      |         |
        | List<?>     | List<Sub> | ArrayList      |         |
        | List<Super> | List<Sub> | ArrayList      |         |
        | Object[]    | Sub[]     | Sub[]          |         |
        | Super[]     | Sub[]     | Sub[]          |         |

    Scenario Outline: Create List Element With Given Properties
      Given the following spec definition:
        """
        public class ListSubSpec extends Spec<<specType>> {}
        """
      And the following bean definition:
        """
        public class Bean<generic> {
          public <type> list;
        }
        """
      When evaluating the following code:
        """
        jFactory.type(Bean.class).property("list(ListSubSpec)[0].value1", "v1").create();
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
        | type        | specType  | actualListType | generic |
        | Object      | List<Sub> | ArrayList      |         |
        | Object      | Sub[]     | Sub[]          |         |
        | T           | List<Sub> | ArrayList      | <T>     |
        | List<T>     | List<Sub> | ArrayList      | <T>     |
        | List        | List<Sub> | ArrayList      |         |
        | List<?>     | List<Sub> | ArrayList      |         |
        | List<Super> | List<Sub> | ArrayList      |         |
        | Object[]    | Sub[]     | Sub[]          |         |
        | Super[]     | Sub[]     | Sub[]          |         |

    Scenario Outline: IntentlyCreate List Element With Given Properties
      Given the following spec definition:
        """
        public class ListSubSpec extends Spec<<specType>> {}
        """
      And the following bean definition:
        """
        public class Bean<generic> {
          public <type> list;
        }
        """
      When evaluating the following code:
        """
        jFactory.type(Bean.class).property("list(ListSubSpec)[0]!.value1", "v1").create();
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
        | type        | specType  | actualListType | generic |
        | Object      | List<Sub> | ArrayList      |         |
        | Object      | Sub[]     | Sub[]          |         |
        | T           | List<Sub> | ArrayList      | <T>     |
        | List<T>     | List<Sub> | ArrayList      | <T>     |
        | List        | List<Sub> | ArrayList      |         |
        | List<?>     | List<Sub> | ArrayList      |         |
        | List<Super> | List<Sub> | ArrayList      |         |
        | Object[]    | Sub[]     | Sub[]          |         |
        | Super[]     | Sub[]     | Sub[]          |         |

    Scenario Outline: Reuse Previously Created Object by Matching its Properties
      Given the following spec definition:
        """
        public class ListSubSpec extends Spec<<specType>> {}
        """
      And the following bean definition:
        """
        public class Bean<generic> {
          public <type> list;
        }
        """
      Given register as follows:
        """
        jFactory.type(Sub.class).property("value1", "v1").property("value2", "v2").create();
        """
      When evaluating the following code:
        """
        jFactory.type(Bean.class).property("list(ListSubSpec)[0].value1", "v1").create();
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
        | type        | specType  | actualListType | generic |
        | Object      | List<Sub> | ArrayList      |         |
        | Object      | Sub[]     | Sub[]          |         |
        | T           | List<Sub> | ArrayList      | <T>     |
        | List<T>     | List<Sub> | ArrayList      | <T>     |
        | List        | List<Sub> | ArrayList      |         |
        | List<?>     | List<Sub> | ArrayList      |         |
        | List<Super> | List<Sub> | ArrayList      |         |
        | Object[]    | Sub[]     | Sub[]          |         |
        | Super[]     | Sub[]     | Sub[]          |         |

    Scenario Outline: Query Root Object by List Element Properties
      Given the following spec definition:
        """
        public class ListSubSpec extends Spec<<specType>> {}
        """
      And the following bean definition:
        """
        public class Bean<generic> {
          public <type> list;
        }
        """
      Given register as follows:
        """
        Sub sub = jFactory.type(Sub.class).property("value1", "v1").property("value2", "v2").create();
        jFactory.type(Bean.class).property("list", new Object[]{sub}).create();
        """
      When evaluating the following code:
        """
        jFactory.type(Bean.class).property("list(ListSubSpec)[0].value1", "v1").query();
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
        | type        | specType  | actualListType | generic |
        | Object      | List<Sub> | Object[]       |         |
        | Object      | Sub[]     | Object[]       |         |
        | T           | List<Sub> | Object[]       | <T>     |
        | List<T>     | List<Sub> | ArrayList      | <T>     |
        | List        | List<Sub> | ArrayList      |         |
        | List<?>     | List<Sub> | ArrayList      |         |
        | List<Super> | List<Sub> | ArrayList      |         |
        | Object[]    | Sub[]     | Object[]       |         |
        | Super[]     | Sub[]     | Super[]        |         |

  Rule: By input child collection element spec

    Background:
      Given the following spec definition:
        """
        public class SubSpec extends Spec<Sub> {
          @Trait
          public void v2() {
            property("value2").value("v2");
          }
        }
        """
      And register as follows:
        """
        jFactory.register(SubSpec.class);
        """

    Scenario Outline: Create Default List Element Without Specifying its Properties
      And the following bean definition:
        """
        public class Bean<generic> {
          public <type> list;
        }
        """
      When evaluating the following code:
        """
        jFactory.type(Bean.class).property("list(SubSpec[])[0]", new HashMap<>()).create();
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
        | type        | actualListType | generic |
        | Object      | ArrayList      |         |
        | T           | ArrayList      | <T>     |
        | List<T>     | ArrayList      | <T>     |
        | List        | ArrayList      |         |
        | List<?>     | ArrayList      |         |
        | List<Super> | ArrayList      |         |
        | Object[]    | Sub[]          |         |
        | Super[]     | Sub[]          |         |

    Scenario Outline: Intently Create Default List Element Without Specifying its Properties
      And the following bean definition:
        """
        public class Bean<generic> {
          public <type> list;
        }
        """
      When evaluating the following code:
        """
        jFactory.type(Bean.class).property("list(SubSpec[])[0]!", null).create();
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
        | type        | actualListType | generic |
        | Object      | ArrayList      |         |
        | T           | ArrayList      | <T>     |
        | List<T>     | ArrayList      | <T>     |
        | List        | ArrayList      |         |
        | List<?>     | ArrayList      |         |
        | List<Super> | ArrayList      |         |
        | Object[]    | Sub[]          |         |
        | Super[]     | Sub[]          |         |

    Scenario Outline: Create List Element With Given Properties
      And the following bean definition:
        """
        public class Bean<generic> {
          public <type> list;
        }
        """
      When evaluating the following code:
        """
        jFactory.type(Bean.class).property("list(SubSpec[])[0].value1", "v1").create();
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
        | type        | actualListType | generic |
        | Object      | ArrayList      |         |
        | T           | ArrayList      | <T>     |
        | List<T>     | ArrayList      | <T>     |
        | List        | ArrayList      |         |
        | List<?>     | ArrayList      |         |
        | List<Super> | ArrayList      |         |
        | Object[]    | Sub[]          |         |
        | Super[]     | Sub[]          |         |

    Scenario Outline: Intently Create List Element With Given Properties
      And the following bean definition:
        """
        public class Bean<generic> {
          public <type> list;
        }
        """
      When evaluating the following code:
        """
        jFactory.type(Bean.class).property("list(SubSpec[])[0]!.value1", "v1").create();
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
        | type        | actualListType | generic |
        | Object      | ArrayList      |         |
        | T           | ArrayList      | <T>     |
        | List<T>     | ArrayList      | <T>     |
        | List        | ArrayList      |         |
        | List<?>     | ArrayList      |         |
        | List<Super> | ArrayList      |         |
        | Object[]    | Sub[]          |         |
        | Super[]     | Sub[]          |         |

    Scenario Outline: Reuse Previously Created List Element by Matching its Properties
      And the following bean definition:
        """
        public class Bean<generic> {
          public <type> list;
        }
        """
      Given register as follows:
        """
        jFactory.type(Sub.class).property("value1", "v1").property("value2", "v2").create();
        """
      When evaluating the following code:
        """
        jFactory.type(Bean.class).property("list(SubSpec[])[0].value1", "v1").create();
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
        | type        | actualListType | generic |
        | Object      | ArrayList      |         |
        | T           | ArrayList      | <T>     |
        | List<T>     | ArrayList      | <T>     |
        | List        | ArrayList      |         |
        | List<?>     | ArrayList      |         |
        | List<Super> | ArrayList      |         |
        | Object[]    | Sub[]          |         |
        | Super[]     | Sub[]          |         |

    Scenario Outline: Query Root Object by List Element Properties
      And the following bean definition:
        """
        public class Bean<generic> {
          public <type> list;
        }
        """
      Given register as follows:
        """
        Sub sub = jFactory.type(Sub.class).property("value1", "v1").property("value2", "v2").create();
        jFactory.type(Bean.class).property("list", new Object[]{sub}).create();
        """
      When evaluating the following code:
        """
        jFactory.type(Bean.class).property("list(SubSpec[])[0].value1", "v1").query();
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
        | type        | actualListType | generic |
        | Object      | Object[]       |         |
        | T           | Object[]       | <T>     |
        | List<T>     | ArrayList      | <T>     |
        | List        | ArrayList      |         |
        | List<?>     | ArrayList      |         |
        | List<Super> | ArrayList      |         |
        | Object[]    | Object[]       |         |
        | Super[]     | Super[]        |         |

  Rule: By input child element Spec

    Background:
      Given the following spec definition:
        """
        public class SubSpec extends Spec<Sub> {
          @Trait
          public void v2() {
            property("value2").value("v2");
          }
        }
        """
      And register as follows:
        """
        jFactory.register(SubSpec.class);
        """

    Scenario Outline: Create Default List Element Without Specifying its Properties
      Given the following bean definition:
        """
        public class Bean<generic> {
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
        | type        | actualListType | generic |
        | List<T>     | ArrayList      | <T>     |
        | List        | ArrayList      |         |
        | List<?>     | ArrayList      |         |
        | List<Super> | ArrayList      |         |
        | Object[]    | Object[]       |         |
        | Super[]     | Super[]        |         |

    Scenario Outline: Intently Create Default List Element Without Specifying its Properties
      Given the following bean definition:
        """
        public class Bean<generic> {
          public <type> list;
        }
        """
      When evaluating the following code:
        """
        jFactory.type(Bean.class).property("list[0](SubSpec)!", null).create();
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
        | type        | actualListType | generic |
        | List<T>     | ArrayList      | <T>     |
        | List        | ArrayList      |         |
        | List<?>     | ArrayList      |         |
        | List<Super> | ArrayList      |         |
        | Object[]    | Object[]       |         |
        | Super[]     | Super[]        |         |

    Scenario Outline: Create List Element With Given Properties
      Given the following bean definition:
        """
        public class Bean<generic> {
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
        | type        | actualListType | generic |
        | List<T>     | ArrayList      | <T>     |
        | List        | ArrayList      |         |
        | List<?>     | ArrayList      |         |
        | List<Super> | ArrayList      |         |
        | Object[]    | Object[]       |         |
        | Super[]     | Super[]        |         |

    Scenario Outline: Intently Create List Element With Given Properties
      Given the following bean definition:
        """
        public class Bean<generic> {
          public <type> list;
        }
        """
      When evaluating the following code:
        """
        jFactory.type(Bean.class).property("list[0](SubSpec)!.value1", "v1").create();
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
        | type        | actualListType | generic |
        | List<T>     | ArrayList      | <T>     |
        | List        | ArrayList      |         |
        | List<?>     | ArrayList      |         |
        | List<Super> | ArrayList      |         |
        | Object[]    | Object[]       |         |
        | Super[]     | Super[]        |         |

    Scenario Outline: Reuse Previously Created List Element by Matching its Properties
      Given the following bean definition:
        """
        public class Bean<generic> {
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
        | type        | actualListType | generic |
        | List<T>     | ArrayList      | <T>     |
        | List        | ArrayList      |         |
        | List<?>     | ArrayList      |         |
        | List<Super> | ArrayList      |         |
        | Object[]    | Object[]       |         |
        | Super[]     | Super[]        |         |

    Scenario Outline: Query Root Object by List Element Properties
      Given the following bean definition:
        """
        public class Bean<generic> {
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
        | type        | actualListType | generic |
        | List<T>     | ArrayList      | <T>     |
        | List        | ArrayList      |         |
        | List<?>     | ArrayList      |         |
        | List<Super> | ArrayList      |         |
        | Object[]    | Object[]       |         |
        | Super[]     | Super[]        |         |

    Scenario Outline: Use Trait in Element Property Spec
      Given the following bean definition:
        """
        public class Bean<generic> {
          public <type> list;
        }
        """
      When evaluating the following code:
        """
        jFactory.type(Bean.class).property("list[0](v2 SubSpec).value1", "v1").create();
        """
      Then the result should be:
        """
        list: [{
          value1= v1
          value2= v2
          class.simpleName= Sub
        }]
        """
      Examples:
        | type        |
        | List        |
        | List<?>     |
        | List<Super> |
        | Object[]    |
        | Super[]     |

    Scenario Outline: Create with Sub Properties (Merge Spec)
      Given the following bean definition:
        """
        public class Bean<generic> {
          public <type> list;
        }
        """
      When evaluating the following code:
        """
        jFactory.type(Bean.class).property("list[0](SubSpec).value1", "v1").property("list[0].value2", "v2").create();
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
        | type        | actualListType | generic |
        | List<T>     | ArrayList      | <T>     |
        | List        | ArrayList      |         |
        | List<?>     | ArrayList      |         |
        | List<Super> | ArrayList      |         |
        | Object[]    | Object[]       |         |
        | Super[]     | Super[]        |         |

  Rule: By input child element Spec for Object Type Collection

    Background:
      Given the following bean definition:
        """
        public class Bean<generic> {
          public Object list;
        }
        """
      Given the following spec definition:
        """
        public class SubSpec extends Spec<Sub> {
          @Trait
          public void v2() {
            property("value2").value("v2");
          }
        }
        """
      And register as follows:
        """
        jFactory.register(SubSpec.class);
        jFactory.register(ListSubSpec.class);
        """

    Scenario Outline: Create Default List Element Without Specifying its Properties
      Given the following spec definition:
        """
        public class ListSubSpec extends Spec<<specType>> {}
        """
      When evaluating the following code:
        """
        jFactory.type(Bean.class).property("list(ListSubSpec)[0](SubSpec)", new HashMap()).create();
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
        | specType    | actualListType |
        | List        | ArrayList      |
        | List<?>     | ArrayList      |
        | List<Super> | ArrayList      |
        | Object[]    | Object[]       |
        | Super[]     | Super[]        |

    Scenario Outline: Intently Create Default List Element Without Specifying its Properties
      Given the following spec definition:
        """
        public class ListSubSpec extends Spec<<specType>> {}
        """
      When evaluating the following code:
        """
        jFactory.type(Bean.class).property("list(ListSubSpec)[0](SubSpec)!", null).create();
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
        | specType    | actualListType |
        | List        | ArrayList      |
        | List<?>     | ArrayList      |
        | List<Super> | ArrayList      |
        | Object[]    | Object[]       |
        | Super[]     | Super[]        |

    Scenario Outline: Create List Element With Given Properties
      Given the following spec definition:
        """
        public class ListSubSpec extends Spec<<specType>> {}
        """
      When evaluating the following code:
        """
        jFactory.type(Bean.class).property("list(ListSubSpec)[0](SubSpec).value1", "v1").create();
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
        | specType    | actualListType |
        | List        | ArrayList      |
        | List<?>     | ArrayList      |
        | List<Super> | ArrayList      |
        | Object[]    | Object[]       |
        | Super[]     | Super[]        |

    Scenario Outline: Intently Create List Element With Given Properties
      Given the following spec definition:
        """
        public class ListSubSpec extends Spec<<specType>> {}
        """
      When evaluating the following code:
        """
        jFactory.type(Bean.class).property("list(ListSubSpec)[0](SubSpec)!.value1", "v1").create();
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
        | specType    | actualListType |
        | List        | ArrayList      |
        | List<?>     | ArrayList      |
        | List<Super> | ArrayList      |
        | Object[]    | Object[]       |
        | Super[]     | Super[]        |

    Scenario Outline: Reuse Previously Created List Element by Matching its Properties
      Given the following spec definition:
        """
        public class ListSubSpec extends Spec<<specType>> {}
        """
      Given register as follows:
        """
        jFactory.type(Sub.class).property("value1", "v1").property("value2", "v2").create();
        """
      When evaluating the following code:
        """
        jFactory.type(Bean.class).property("list(ListSubSpec)[0](SubSpec).value1", "v1").create();
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
        | specType    | actualListType |
        | List        | ArrayList      |
        | List<?>     | ArrayList      |
        | List<Super> | ArrayList      |
        | Object[]    | Object[]       |
        | Super[]     | Super[]        |

    Scenario Outline: Query Root Object by List Element Properties
      Given the following spec definition:
        """
        public class ListSubSpec extends Spec<<specType>> {}
        """
      Given register as follows:
        """
        Sub sub = jFactory.type(Sub.class).property("value1", "v1").property("value2", "v2").create();
        jFactory.type(Bean.class).property("list", new Object[]{sub}).create();
        """
      When evaluating the following code:
        """
        jFactory.type(Bean.class).property("list(ListSubSpec)[0](SubSpec).value1", "v1").query();
        """
      Then the result should be:
        """
        : {
          list: [{
            value1= v1
            value2= v2
            class.simpleName= Sub
          }]
          list.class.simpleName= 'Object[]'
        }
        """
      Examples:
        | specType    |
        | List        |
        | List<?>     |
        | List<Super> |
        | Object[]    |
        | Super[]     |

    Scenario Outline: Use Trait in Element Property Spec
      Given the following spec definition:
        """
        public class ListSubSpec extends Spec<<specType>> {}
        """
      When evaluating the following code:
        """
        jFactory.type(Bean.class).property("list(ListSubSpec)[0](v2 SubSpec).value1", "v1").create();
        """
      Then the result should be:
        """
        list: [{
          value1= v1
          value2= v2
          class.simpleName= Sub
        }]
        """
      Examples:
        | specType    |
        | List        |
        | List<?>     |
        | List<Super> |
        | Object[]    |
        | Super[]     |

    Scenario Outline: Create with Sub Properties (Merge Spec)
      Given the following spec definition:
        """
        public class ListSubSpec extends Spec<<specType>> {}
        """
      When evaluating the following code:
        """
        jFactory.type(Bean.class).property("list(ListSubSpec)[0](SubSpec).value1", "v1").property("list[0].value2", "v2").create();
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
        | specType    | actualListType |
        | List        | ArrayList      |
        | List<?>     | ArrayList      |
        | List<Super> | ArrayList      |
        | Object[]    | Object[]       |
        | Super[]     | Super[]        |

  Rule: By input child collection Spec Override Parent is(...) with Collection Spec

    Background:
      And register as follows:
        """
        jFactory.register(ListSubSpec.class);
        jFactory.factory(Bean.class).spec(spec -> {
          spec.property("list").is(OriginalListSpec.class);
        });
        """

    Scenario Outline: Create Default List Element Without Specifying its Properties
      Given the following spec definition:
        """
        public class OriginalListSpec extends Spec<<originalSpecType>> {}
        """
      Given the following spec definition:
        """
        public class ListSubSpec extends Spec<<specType>> {}
        """
      And the following bean definition:
        """
        public class Bean<generic> {
          public <type> list;
        }
        """
      When evaluating the following code:
        """
        jFactory.type(Bean.class).property("list(ListSubSpec)[0]", new HashMap<>()).create();
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
        | type        | originalSpecType | specType  | actualListType | generic |
        | Object      | List<Super>      | List<Sub> | ArrayList      |         |
        | Object      | Super[]          | Sub[]     | Sub[]          |         |
        | T           | Super[]          | Sub[]     | Sub[]          | <T>     |
        | List<T>     | List<Super>      | List<Sub> | ArrayList      | <T>     |
        | List        | List<Super>      | List<Sub> | ArrayList      |         |
        | List<?>     | List<Super>      | List<Sub> | ArrayList      |         |
        | List<Super> | List<Super>      | List<Sub> | ArrayList      |         |
        | Object[]    | Super[]          | Sub[]     | Sub[]          |         |
        | Super[]     | Super[]          | Sub[]     | Sub[]          |         |

    Scenario Outline: Create List Element With Given Properties
      Given the following spec definition:
        """
        public class OriginalListSpec extends Spec<<originalSpecType>> {}
        """
      Given the following spec definition:
        """
        public class ListSubSpec extends Spec<<specType>> {}
        """
      And the following bean definition:
        """
        public class Bean<generic> {
          public <type> list;
        }
        """
      When evaluating the following code:
        """
        jFactory.type(Bean.class).property("list(ListSubSpec)[0].value1", "v1").create();
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
        | type        | originalSpecType | specType  | actualListType | generic |
        | Object      | List<Super>      | List<Sub> | ArrayList      |         |
        | Object      | Super[]          | Sub[]     | Sub[]          |         |
        | T           | Super[]          | Sub[]     | Sub[]          | <T>     |
        | List<T>     | List<Super>      | List<Sub> | ArrayList      | <T>     |
        | List        | List<Super>      | List<Sub> | ArrayList      |         |
        | List<?>     | List<Super>      | List<Sub> | ArrayList      |         |
        | List<Super> | List<Super>      | List<Sub> | ArrayList      |         |
        | Object[]    | Super[]          | Sub[]     | Sub[]          |         |
        | Super[]     | Super[]          | Sub[]     | Sub[]          |         |

    Scenario Outline: Reuse Previously Created Object by Matching its Properties
      Given the following spec definition:
        """
        public class OriginalListSpec extends Spec<<originalSpecType>> {}
        """
      Given the following spec definition:
        """
        public class ListSubSpec extends Spec<<specType>> {}
        """
      And the following bean definition:
        """
        public class Bean<generic> {
          public <type> list;
        }
        """
      Given register as follows:
        """
        jFactory.type(Sub.class).property("value1", "v1").property("value2", "v2").create();
        """
      When evaluating the following code:
        """
        jFactory.type(Bean.class).property("list(ListSubSpec)[0].value1", "v1").create();
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
        | type        | originalSpecType | specType  | actualListType | generic |
        | Object      | List<Super>      | List<Sub> | ArrayList      |         |
        | Object      | Super[]          | Sub[]     | Sub[]          |         |
        | T           | Super[]          | Sub[]     | Sub[]          | <T>     |
        | List<T>     | List<Super>      | List<Sub> | ArrayList      | <T>     |
        | List        | List<Super>      | List<Sub> | ArrayList      |         |
        | List<?>     | List<Super>      | List<Sub> | ArrayList      |         |
        | List<Super> | List<Super>      | List<Sub> | ArrayList      |         |
        | Object[]    | Super[]          | Sub[]     | Sub[]          |         |
        | Super[]     | Super[]          | Sub[]     | Sub[]          |         |

    Scenario Outline: Query Root Object by List Element Properties
      Given the following spec definition:
        """
        public class OriginalListSpec extends Spec<<originalSpecType>> {}
        """
      Given the following spec definition:
        """
        public class ListSubSpec extends Spec<<specType>> {}
        """
      And the following bean definition:
        """
        public class Bean<generic> {
          public <type> list;
        }
        """
      Given register as follows:
        """
        Sub sub = jFactory.type(Sub.class).property("value1", "v1").property("value2", "v2").create();
        jFactory.type(Bean.class).property("list", new Object[]{sub}).create();
        """
      When evaluating the following code:
        """
        jFactory.type(Bean.class).property("list(ListSubSpec)[0].value1", "v1").query();
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
        | type        | originalSpecType | specType  | actualListType | generic |
        | Object      | List<Super>      | List<Sub> | Object[]       |         |
        | Object      | Super[]          | Sub[]     | Object[]       |         |
        | T           | Super[]          | Sub[]     | Object[]       | <T>     |
        | List<T>     | List<Super>      | List<Sub> | ArrayList      | <T>     |
        | List        | List<Super>      | List<Sub> | ArrayList      |         |
        | List<?>     | List<Super>      | List<Sub> | ArrayList      |         |
        | List<Super> | List<Super>      | List<Sub> | ArrayList      |         |
        | Object[]    | Super[]          | Sub[]     | Object[]       |         |
        | Super[]     | Super[]          | Sub[]     | Super[]        |         |

#  Rule: By input child collection Spec Override Parent is(...) with Element Spec
#
#    Background:
#      Given the following spec definition:
#        """
#        public class OriginalElementSpec extends Spec<Object> {}
#        """
#      And register as follows:
#        """
#        jFactory.register(ListSubSpec.class);
#        jFactory.factory(Bean.class).spec(spec -> {
#          spec.property("list[]").is(OriginalElementSpec.class);
#        });
#        """
#
#    Scenario Outline: Create Default List Element Without Specifying its Properties
#      Given the following spec definition:
#        """
#        public class ListSubSpec extends Spec<<specType>> {}
#        """
#      And the following bean definition:
#        """
#        public class Bean<generic> {
#          public <type> list;
#        }
#        """
#      When evaluating the following code:
#        """
#        jFactory.type(Bean.class).property("list(ListSubSpec)[0]", new HashMap<>()).create();
#        """
#      Then the result should be:
#        """
#        : {
#          list= [{
#            value1= /^value1.*/
#            value2= /^value2.*/
#            class.simpleName= Sub
#          }]
#          list.class.simpleName= '<actualListType>'
#        }
#        """
#      Examples:
#        | type   | originalSpecType | specType  | actualListType |
#        | Object | List<Super>      | List<Sub> | ArrayList      |
#        | Object      | Super[]          | Sub[]     | Sub[]          |
#        | List        | List<Super>      | List<Sub> | ArrayList      |
#        | List<?>     | List<Super>      | List<Sub> | ArrayList      |
#        | List<Super> | List<Super>      | List<Sub> | ArrayList      |
#        | Object[]    | Super[]          | Sub[]     | Sub[]          |
#        | Super[]     | Super[]          | Sub[]     | Sub[]          |
#
#    Scenario Outline: Create List Element With Given Properties
#      Given the following spec definition:
#        """
#        public class ListSubSpec extends Spec<<specType>> {}
#        """
#      And the following bean definition:
#        """
#        public class Bean<generic> {
#          public <type> list;
#        }
#        """
#      When evaluating the following code:
#        """
#        jFactory.type(Bean.class).property("list(ListSubSpec)[0].value1", "v1").create();
#        """
#      Then the result should be:
#        """
#        : {
#          list= [{
#            value1= v1
#            value2= /^value2.*/
#            class.simpleName= Sub
#          }]
#          list.class.simpleName= '<actualListType>'
#        }
#        """
#      Examples:
#        | type        | originalSpecType | specType  | actualListType |
#        | Object      | List<Super>      | List<Sub> | ArrayList      |
#        | Object      | Super[]          | Sub[]     | Sub[]          |
#        | List        | List<Super>      | List<Sub> | ArrayList      |
#        | List<?>     | List<Super>      | List<Sub> | ArrayList      |
#        | List<Super> | List<Super>      | List<Sub> | ArrayList      |
#        | Object[]    | Super[]          | Sub[]     | Sub[]          |
#        | Super[]     | Super[]          | Sub[]     | Sub[]          |
#
#    Scenario Outline: Reuse Previously Created Object by Matching its Properties
#      Given the following spec definition:
#        """
#        public class ListSubSpec extends Spec<<specType>> {}
#        """
#      And the following bean definition:
#        """
#        public class Bean<generic> {
#          public <type> list;
#        }
#        """
#      Given register as follows:
#        """
#        jFactory.type(Sub.class).property("value1", "v1").property("value2", "v2").create();
#        """
#      When evaluating the following code:
#        """
#        jFactory.type(Bean.class).property("list(ListSubSpec)[0].value1", "v1").create();
#        """
#      Then the result should be:
#        """
#        : {
#          list= [{
#            value1= v1
#            value2= v2
#          }]
#          list.class.simpleName= '<actualListType>'
#        }
#        """
#      Examples:
#        | type        | originalSpecType | specType  | actualListType |
#        | Object      | List<Super>      | List<Sub> | ArrayList      |
#        | Object      | Super[]          | Sub[]     | Sub[]          |
#        | List        | List<Super>      | List<Sub> | ArrayList      |
#        | List<?>     | List<Super>      | List<Sub> | ArrayList      |
#        | List<Super> | List<Super>      | List<Sub> | ArrayList      |
#        | Object[]    | Super[]          | Sub[]     | Sub[]          |
#        | Super[]     | Super[]          | Sub[]     | Sub[]          |
#
#    Scenario Outline: Query Root Object by List Element Properties
#      Given the following spec definition:
#        """
#        public class ListSubSpec extends Spec<<specType>> {}
#        """
#      And the following bean definition:
#        """
#        public class Bean<generic> {
#          public <type> list;
#        }
#        """
#      Given register as follows:
#        """
#        Sub sub = jFactory.type(Sub.class).property("value1", "v1").property("value2", "v2").create();
#        jFactory.type(Bean.class).property("list", new Object[]{sub}).create();
#        """
#      When evaluating the following code:
#        """
#        jFactory.type(Bean.class).property("list(ListSubSpec)[0].value1", "v1").query();
#        """
#      Then the result should be:
#        """
#        : {
#          list= [{
#            value1= v1
#            value2= v2
#          }]
#          list.class.simpleName= '<actualListType>'
#        }
#        """
#      Examples:
#        | type        | originalSpecType | specType  | actualListType |
#        | Object      | List<Super>      | List<Sub> | Object[]       |
#        | Object      | Super[]          | Sub[]     | Object[]       |
#        | List        | List<Super>      | List<Sub> | ArrayList      |
#        | List<?>     | List<Super>      | List<Sub> | ArrayList      |
#        | List<Super> | List<Super>      | List<Sub> | ArrayList      |
#        | Object[]    | Super[]          | Sub[]     | Object[]       |
#        | Super[]     | Super[]          | Sub[]     | Super[]        |
#


  Rule: By input child element Spec override Original Spec in Parent

    Background:
      Given the following class definition:
        """
        public class AnotherSub extends Super {}
        """
      Given the following spec definition:
        """
        public class OriginalSupSpec extends Spec<AnotherSub> {}
        """
      And the following spec definition:
        """
        public class SubSpec extends Spec<Sub> {
          public void main() {
            property("value2").value("New");
          }

          @Trait
          public void v2() {
            property("value2").value("v2");
          }
        }
        """
      And register as follows:
         """
         jFactory.register(SubSpec.class);
         """
      And the following spec definition:
         """
         public class BeanSpec extends Spec<Bean> {
           public void main() {
             property("list[]").is(OriginalSupSpec.class);
           }
         }
         """

    Scenario Outline: Create Default List Element Without Specifying its Properties
      Given the following bean definition:
        """
        public class Bean<generic> {
          public <type> list;
        }
        """
      When evaluating the following code:
        """
        jFactory.spec(BeanSpec.class).property("list[0](SubSpec)", new HashMap()).create();
        """
      Then the result should be:
        """
        : {
          list: [{
            value2= New
            class.simpleName= Sub
          }]
          list.class.simpleName= '<actualListType>'
        }
        """
      Examples:
        | type        | actualListType | generic |
#Bug        | Object      | ArrayList      |         |
        | List<T>     | ArrayList      | <T>     |
        | List        | ArrayList      |         |
        | List<?>     | ArrayList      |         |
        | List<Super> | ArrayList      |         |
        | Object[]    | Object[]       |         |
        | Super[]     | Super[]        |         |

    Scenario Outline: Create List Element With Given Properties
      Given the following bean definition:
        """
        public class Bean<generic> {
          public <type> list;
        }
        """
      When evaluating the following code:
        """
        jFactory.spec(BeanSpec.class).property("list[0](SubSpec).value1", "v1").create();
        """
      Then the result should be:
        """
        : {
          list: [{
            value1= v1
            value2= New
            class.simpleName= Sub
          }]
          list.class.simpleName= '<actualListType>'
        }
        """
      Examples:
        | type        | actualListType | generic |
        | List<T>     | ArrayList      | <T>     |
        | List        | ArrayList      |         |
        | List<?>     | ArrayList      |         |
        | List<Super> | ArrayList      |         |
        | Object[]    | Object[]       |         |
        | Super[]     | Super[]        |         |

    Scenario Outline: Reuse Previously Created List Element by Matching its Properties
      Given the following bean definition:
        """
        public class Bean<generic> {
          public <type> list;
        }
        """
      Given register as follows:
        """
        jFactory.type(Sub.class).property("value1", "v1").property("value2", "v2").create();
        """
      When evaluating the following code:
        """
        jFactory.spec(BeanSpec.class).property("list[0](SubSpec).value1", "v1").create();
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
        | type        | actualListType | generic |
        | List<T>     | ArrayList      | <T>     |
        | List        | ArrayList      |         |
        | List<?>     | ArrayList      |         |
        | List<Super> | ArrayList      |         |
        | Object[]    | Object[]       |         |
        | Super[]     | Super[]        |         |

    Scenario Outline: Query Root Object by List Element Properties
      Given the following bean definition:
        """
        public class Bean<generic> {
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
        jFactory.spec(BeanSpec.class).property("list[0](SubSpec).value1", "v1").query();
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
        | type        | actualListType | generic |
        | List<T>     | ArrayList      | <T>     |
        | List        | ArrayList      |         |
        | List<?>     | ArrayList      |         |
        | List<Super> | ArrayList      |         |
        | Object[]    | Object[]       |         |
        | Super[]     | Super[]        |         |

    Scenario Outline: Use Trait in Element Property Spec
      Given the following bean definition:
        """
        public class Bean<generic> {
          public <type> list;
        }
        """
      When evaluating the following code:
        """
        jFactory.spec(BeanSpec.class).property("list[0](v2 SubSpec).value1", "v1").create();
        """
      Then the result should be:
        """
        list: [{
          value1= v1
          value2= v2
          class.simpleName= Sub
        }]
        """
      Examples:
        | type        | actualListType | generic |
        | List<T>     | ArrayList      | <T>     |
        | List        | ArrayList      |         |
        | List<?>     | ArrayList      |         |
        | List<Super> | ArrayList      |         |
        | Object[]    | Object[]       |         |
        | Super[]     | Super[]        |         |

    Scenario Outline: Create with Sub Properties (Merge Spec)
      Given the following bean definition:
        """
        public class Bean<generic> {
          public <type> list;
        }
        """
      When evaluating the following code:
        """
        jFactory.spec(BeanSpec.class).property("list[0](SubSpec).value1", "v1").property("list[0].value2", "v2").create();
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
        | type        | actualListType | generic |
        | List<T>     | ArrayList      | <T>     |
        | List        | ArrayList      |         |
        | List<?>     | ArrayList      |         |
        | List<Super> | ArrayList      |         |
        | Object[]    | Object[]       |         |
        | Super[]     | Super[]        |         |

    Scenario Outline: Create one Keep the others
      Given the following bean definition:
        """
        public class Bean<generic> {
          public <type> list;
        }
        """
      When evaluating the following code:
        """
        jFactory.spec(BeanSpec.class).property("list[0](SubSpec).value1", "v1").property("list[1]", new HashMap()).create();
        """
      Then the result should be:
        """
        : {
          list: [{
            value1= v1
            value2= New
            class.simpleName= Sub
          }{
            class.simpleName= AnotherSub
          }]
          list.class.simpleName= '<actualListType>'
        }
        """
      Examples:
        | type        | actualListType | generic |
        | List<T>     | ArrayList      | <T>     |
        | List        | ArrayList      |         |
        | List<?>     | ArrayList      |         |
        | List<Super> | ArrayList      |         |
        | Object[]    | Object[]       |         |
        | Super[]     | Super[]        |         |

  Rule: By input child element Spec override Original Spec in Parent for type object Collection

    Background:
      Given the following bean definition:
        """
        public class Bean {
          public Object list;
        }
        """
      And the following spec definition:
        """
        public class Element {
          public String value1, value2;
        }
        """
      Given the following spec definition:
        """
        public class ElementSpec extends Spec<Element> {
          @Trait
          public void v2() {
            property("value2").value("v2");
          }
        }
        """
      And register as follows:
        """
        jFactory.register(ElementSpec.class);
        """

    Scenario: Specify Element Spec
      Given the following class definition:
        """
        public class NewElement extends Element {}
        """
      Given the following spec definition:
        """
        public class NewElementSpec extends Spec<NewElement> {
          public void main() {
            property("value2").value("v2");
          }
        }
        """
      And register as follows:
        """
        jFactory.register(NewElementSpec.class);
        """
      When evaluating the following code:
        """
        jFactory.type(Bean.class)
          .property("list(ElementSpec[])[0](NewElementSpec).value1", "v1")
          .property("list(ElementSpec[])[1]", new HashMap())
          .create();
        """
      Then the result should be:
        """
        list: | value1      | value2      | class.simpleName |
              | v1          | v2          | NewElement       |
              | /^value1.*/ | /^value2.*/ | Element          |
        """

    Scenario: Use Trait in Element Property Spec
      Given the following class definition:
        """
        public class NewElement extends Element {}
        """
      Given the following spec definition:
        """
        public class NewElementSpec extends Spec<NewElement> {
          @Trait
          public void v2() {
            property("value2").value("v2");
          }
        }
        """
      And register as follows:
        """
        jFactory.register(NewElementSpec.class);
        """
      When evaluating the following code:
        """
        jFactory.type(Bean.class).property("list(ElementSpec[])[0](v2 NewElementSpec).value1", "v1").create();
        """
      Then the result should be:
        """
        list: [{
          value1= v1
          value2= v2
          class.simpleName= NewElement
        }]
        """


#      Done TODO Missing spec override test input collection override parent collection
#      Bug TODO Missing spec override test input collection override parent element[]

#      TODO Missing spec override test input element[] override parent collection
#      TODO Missing spec override test input element[] override parent element[]

#      TODO Missing spec override test input element spec override parent collection
#      Done TODO Missing spec override test input element spec override parent element[]
#      Bug TODO Missing spec override test input element spec override parent element[] for object type
#      TODO Missing spec override test input element spec override input collection
#      TODO Missing spec override test input element spec override input element[]
