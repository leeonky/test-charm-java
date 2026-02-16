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

    Scenario Outline: Create Empty List
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

    Scenario Outline: Create Default without Input Properties
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
        | type | specType | actualListType |
#        | List | List<Sub> | ArrayList      |
#        | List<?>     | List<Sub> | ArrayList      |
#        | List<Super> | List<Sub> | ArrayList      |
#        | Object[]    | Sub[]     | Sub[]          |
#        | Super[]     | Sub[]     | Sub[]          |
