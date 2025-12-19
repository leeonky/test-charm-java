Feature: build value producer

  Rule: should query first

    Background:
      Given the following bean definition:
        """
        public class Bean {
          public String str;
        }
        """
      And the following bean definition:
        """
        public class BeanContainer {
          public Bean bean;
        }
        """
      And the following declarations:
        """
        JFactory jFactory = new JFactory();
        """

    Scenario: should query first in byFactory with property
      Given the following spec definition:
        """
        public class BeanContainerSpec extends Spec<BeanContainer> {
          public void main() {
            property("bean").byFactory(builder -> builder.property("str", "hello"));
          }
        }
        """
      When register as follows:
        """
        jFactory.type(Bean.class).property("str", "hello").create();
        jFactory.spec(BeanContainerSpec.class).property("bean.str", "hello").create();
        """
      And evaluating the following code:
        """
        jFactory.type(Bean.class).queryAll();
        """
      Then the result should be:
        """
        ::size= 1
        """

    Scenario: should not query first when merge with not query first and not query first
      Given the following spec definition:
        """
        public class BeanContainerSpec extends Spec<BeanContainer> {
          public void main() {
            property("bean").byFactory();
          }
        }
        """
      When register as follows:
        """
        jFactory.type(Bean.class).create();
        jFactory.spec(BeanContainerSpec.class).property("bean!", "any").create();
        """
      And evaluating the following code:
        """
        jFactory.type(Bean.class).queryAll();
        """
      Then the result should be:
        """
        ::size= 2
        """

    Scenario: should query first when merge with query first and not query first
      Given the following spec definition:
        """
        public class BeanContainerSpec extends Spec<BeanContainer> {
          public void main() {
            property("bean").byFactory();
          }
        }
        """
      When register as follows:
        """
        jFactory.type(Bean.class).property("str", "hello").create();
        jFactory.spec(BeanContainerSpec.class).property("bean.str", "hello").create();
        """
      And evaluating the following code:
        """
        jFactory.type(Bean.class).queryAll();
        """
      Then the result should be:
        """
        ::size= 1
        """

#TODO missing test of all query first scenarios
#TODO spec query, property intently create?