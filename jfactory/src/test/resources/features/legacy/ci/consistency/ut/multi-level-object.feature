Feature: consistency in multi-level-object

  Background:
    Given declaration jFactory =
    """
    new JFactory();
    """

  Rule: sub object consistency define in one bean

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

    Scenario: merge consistency in parent and sub bean list

      Given the following bean class:
        """
        public class Sub {
          public String value;
          public List<Bean> beans = new ArrayList<>();
        }
        """
      Given the following bean class:
        """
        public class Parent {
          public String value;
          public Sub sub = new Sub();
        }
        """
      And operate:
        """
        jFactory.factory(Sub.class).spec(spec -> {
            spec.consistent(String.class)
                    .direct("value")
                    .list("beans").consistent(beans -> beans
                      .direct("str1"));

        });

        jFactory.factory(Parent.class).spec(spec -> {
            spec.link("value", "sub.value");
        });
        """
      When build:
        """
        jFactory.clear().type(Parent.class)
          .property("value", "hello")
          .property("sub.beans[0]!", null)
          .create();
        """
      Then the result should:
        """
        : {
          value: hello
          sub: {
            value: hello
            beans: [{
                str1: hello
            }]
          }
        }
        """

#  Rule: resolution order
#
#    Background:
#      Given the following bean class:
#        """
#        public class SubBean {
#          public String str;
#          public SubBean() {}
#          public SubBean(String s) {str=s;}
#        }
#        """
#      Given the following bean class:
#        """
#        public class Bean {
#          public String str;
#          public SubBean subBean1 = new SubBean();
#          public SubBean subBean2 = new SubBean();
#        }
#        """
#
#    Scenario: should resolve consistency with parent property before with sub property
#      And the following spec class:
#        """
#        public class ABean extends Spec<Bean> {
#          public void main() {
#              property("subBean1").byFactory();
#              property("subBean2").byFactory();
#
#              consistent(String.class)
#                .direct("str")
#                .direct("subBean1.str");
#
#              consistent(SubBean.class)
#                .direct("subBean1")
#                .direct("subBean2");
#          }
#        }
#        """
#      When build:
#        """
#        jFactory.clear().spec(ABean.class).property("subBean2", new SubBean("hello")).create();
#        """
#      Then the result should:
#        """
#        : {
#          str: hello
#
#          <<subBean1, subBean2>>.str: hello
#
#          subBean1= .subBean2
#        }
#        """
