Feature: sub object consistency

  Background:
    Given declaration jFactory =
    """
    new JFactory();
    """

  Rule: define in one bean

    Background:
      Given the following bean class:
        """
        public class SubBean {
          public String subStr1, subStr2;
        }
        """
      Given the following bean class:
        """
        public class Bean {
          public String str1;
          public SubBean subBean = new SubBean();
        }
        """

    Scenario: define consistency in parent spec
      And the following spec class:
        """
        public class ABean extends Spec<Bean> {
          public void main() {
              property("subBean").byFactory();
              consistent(String.class)
                .direct("str1")
                .direct("subBean.subStr2")
                .direct("subBean.subStr1");
          }
        }
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).property("str1", "hello").create();
        """
      Then the result should:
        """
        : {
          str1: hello
          subBean: {
            subStr1: hello
            subStr2: hello
          }
        }
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).property("subBean.subStr1", "hello").create();
        """
      Then the result should:
        """
        : {
          str1: hello
          subBean: {
            subStr1: hello
            subStr2: hello
          }
        }
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).property("subBean.subStr2", "hello").create();
        """
      Then the result should:
        """
        : {
          str1: hello
          subBean: {
            subStr1: hello
            subStr2: hello
          }
        }
        """

    Scenario: define consistency in sub spec
      And the following spec class:
        """
        public class SubBeanSpec extends Spec<SubBean> {
          public void main() {
              consistent(String.class)
                .direct("subStr2")
                .direct("subStr1");
          }
        }
        """
      And the following spec class:
        """
        public class ABean extends Spec<Bean> {
          public void main() {
              property("subBean").is("SubBeanSpec");
          }
        }
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).property("subBean.subStr1", "hello").create();
        """
      Then the result should:
        """
        : {
          str1: /^str1.*/
          subBean: {
            subStr1: hello
            subStr2: hello
          }
        }
        """

    Scenario: merge consistency in parent and sub bean
      And the following spec class:
        """
        public class SubBeanSpec extends Spec<SubBean> {
          public void main() {
              consistent(String.class)
                .direct("subStr2")
                .direct("subStr1");
          }
        }
        """
      And the following spec class:
        """
        public class ABean extends Spec<Bean> {
          public void main() {
              property("subBean").is("SubBeanSpec");
              consistent(String.class)
                .direct("str1")
                .direct("subBean.subStr1");
          }
        }
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).property("str1", "hello").create();
        """
      Then the result should:
        """
        : {
          str1: hello
          subBean: {
            subStr1: hello
            subStr2: hello
          }
        }
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).property("subBean.subStr1", "hello").create();
        """
      Then the result should:
        """
        : {
          str1: hello
          subBean: {
            subStr1: hello
            subStr2: hello
          }
        }
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).property("subBean.subStr2", "hello").create();
        """
      Then the result should:
        """
        : {
          str1: hello
          subBean: {
            subStr1: hello
            subStr2: hello
          }
        }
        """
