Feature: consistency producer replacement

  Background:
    Given declaration jFactory =
    """
    new JFactory();
    """

    Given the following bean class:
      """
      public class Bean {
        public String str1, str2, str3, str4;
      }
      """

  Scenario: should keep original consistency producer
    And the following spec class:
      """
      public class ABean extends Spec<Bean> {
        public void main() {
          consistent(String.class)
            .direct("str1")
            .direct("str2");

          consistent(String.class)
            .direct("str3")
            .property("str2")
              .write(s->s);
        }
      }
      """
    When build:
      """
      jFactory.clear().spec(ABean.class).create();
      """
    Then the result should:
      """
      <<str1,str2>>= /^str1.*/, str3= /^str3.*/
      """

  Scenario: should keep original fixed value producer
    And the following spec class:
      """
      public class ABean extends Spec<Bean> {
        public void main() {
          consistent(String.class)
            .direct("str1")
            .direct("str2");

          consistent(String.class)
            .direct("str3")
            .property("str1")
              .write(s->s);
        }
      }
      """
    When build:
      """
      jFactory.clear().spec(ABean.class).property("str1", "hello").create();
      """
    Then the result should:
      """
      <<str1,str2>>= hello, str3= /^str3.*/
      """

  Scenario: should keep original parent fixed value producer
    Given the following bean class:
    """
      public class Bean {
        public String str;
      }
    """
    And the following bean class:
    """
      public class Beans {
        public Bean bean1, bean2;
      }
    """
    And register:
    """
      jFactory.factory(Beans.class).spec(spec-> {
        spec.link("bean1.str", "bean2.str");
      });
    """
    When build:
      """
      jFactory.type(Beans.class).property("bean1", new Bean()).property("bean2.str", "world").create();
      """
    Then the result should:
      """
      bean1.str: null, bean2.str: world
      """

  Scenario: should keep fixed object producer
    Given the following bean class:
    """
      public class Bean {
        public String str;
      }
    """
    And the following bean class:
    """
      public class Beans {
        public Bean bean1, bean2;
      }
    """
    And register:
    """
      jFactory.factory(Beans.class).spec(spec-> {
        spec.consistent(Object.class)
          .property("bean1")
            .write(o->o)
          .property("bean2")
            .read(o->o);
      });
    """
    When build:
      """
      jFactory.type(Beans.class).property("bean1.str", "hello").property("bean2.str", "world").create();
      """
    Then the result should:
      """
      bean1.str: hello, bean2.str: world
      """
