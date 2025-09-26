Feature: choose consistency input item

  Background:
    Given declaration jFactory =
    """
    new JFactory();
    """

  Rule: basic order

    Background:
      Given the following bean class:
        """
        public class Bean {
          public String str1, str2, str3, str4;
        }
        """

    Scenario: unfixed value > default value
      And the following spec class:
        """
        public class ABean extends Spec<Bean> {
          public void main() {
            property("str2").value("foo");
            linkNew("str1", "str2");
          }
        }
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).create();
        """
      Then the result should:
        """
        <<str1,str2>>= foo
        """

    Scenario: readonly > unfixed value
      Given the following bean class:
        """
        public class SubBean {
          public String str;
          public SubBean() {}
          public SubBean(String s) {str=s;}
        }
        """
      Given the following bean class:
        """
        public class Bean {
          public String str;
          public SubBean subBean = new SubBean();
        }
        """
      And the following spec class:
        """
        public class ABean extends Spec<Bean> {
          public void main() {
              property("subBean").byFactory();
              property("str").value("any");

              consistent(String.class)
                .direct("str")
                .direct("subBean.str");
          }
        }
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).property("subBean", new SubBean("hello")).create();
        """
      Then the result should:
        """
        <<str, subBean.str>>= hello
        """

    Scenario: fixed value > readonly
      Given the following bean class:
        """
        public class SubBean {
          public String str;
          public SubBean() {}
          public SubBean(String s) {str=s;}
        }
        """
      Given the following bean class:
        """
        public class Bean {
          public String str;
          public SubBean subBean = new SubBean();
        }
        """
      And the following spec class:
        """
        public class ABean extends Spec<Bean> {
          public void main() {
              property("subBean").byFactory();
              property("str").value("any");

              consistent(String.class)
                .direct("str")
                .direct("subBean.str");
          }
        }
        """
      When build:
        """
        jFactory.clear().spec(ABean.class)
          .property("str", "fixed")
          .property("subBean", new SubBean("hello"))
          .create();
        """
      Then the result should:
        """
        str= fixed, subBean.str= hello
        """
