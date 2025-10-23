Feature: choose consistency root source provider

  Background:
    Given declaration jFactory =
    """
    new JFactory();
    """

  Rule: basic order
  order: fixed > readonly > unfixed > object producer > default
  ignore placeholder
  primary.sub(DefaultValueFactoryProducer) is readonly
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
            link("str1", "str2");
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

    Scenario: object producer > default value (consider empty producer child as default value producer)
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
          public SubBean subBean1, subBean2;
        }
        """
      And the following spec class:
        """
        public class ABean extends Spec<Bean> {
          public void main() {
              property("subBean2").byFactory();
              link("subBean1", "subBean2");
          }
        }
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).create();
        """
      Then the result should:
        """
        : {
          subBean1= .subBean2
          subBean1: {...}
        }
        """

    Scenario: prefer item only has reader first - default value
      And the following spec class:
        """
        public class ABean extends Spec<Bean> {
          public void main() {
            consistent(Object.class)
              .property("str1")
                .write(s->s)
                .read(s->s)
              .property("str2")
                .read(s->s);
          }
        }
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).create();
        """
      Then the result should:
        """
        <<str1,str2>>= /^str2.*/
        """

    Scenario: prefer item only has reader first - given value
      And the following spec class:
        """
        public class ABean extends Spec<Bean> {
          public void main() {
            consistent(Object.class)
              .property("str1")
                .write(s->s)
                .read(s->s)
              .property("str2")
                .write(s->s)
                .read(s->s)
              .property("str3")
                .read(s->s);
          }
        }
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).property("str2", "hello").property("str3", "world").create();
        """
      Then the result should:
        """
        <<str1,str3>>= world, str2= hello
        """

    Scenario: one of properties has higher priority then consistency item has higher priority
      Given the following bean class:
        """
        public class Person {
          public String fullName, firstName, lastName, familyName, givenName;
        }
        """
      And the following spec class:
        """
        public class APerson extends Spec<Person> {
          public void main() {
            consistent(String.class)
              .direct("fullName")
              .properties("firstName", "lastName")
                .read((first,last) -> first+" "+last).write(s -> s.split(" "))
              .properties("familyName", "givenName")
                .read(names -> names[0]+" "+names[1]).write(s->s.split(" ")[0], s->s.split(" ")[1]);
          }
        }
        """
      When build:
        """
        jFactory.clear().spec(APerson.class).property("givenName", "Tom").create();
        """
      Then the result should:
        """
        : {
          fullName: /^familyName.* Tom/
          firstName: /^familyName.*/
          lastName: Tom
          familyName: /^familyName.*/
          givenName: Tom
        }
        """

    Scenario: consider sub property of default type value producer is a place holder producer
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
          public SubBean subBean1, subBean2;
        }
        """
      And the following spec class:
        """
        public class ABean extends Spec<Bean> {
          public void main() {
              property("subBean2").byFactory();
              link("subBean1.str", "subBean2.str");
          }
        }
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).create();
        """
      Then the result should:
        """
        : {
          subBean1: null
          subBean2.str: /^str.*/
        }
        """

    Scenario: property of default value(not DefaultValueFactoryProducer) producer is placeholder;
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
          public SubBean subBean1, subBean2;
        }
        """
      And the following spec class:
        """
        public class ABean extends Spec<Bean> {
          public void main() {
              link("subBean1.str", "subBean2.str");
          }
        }
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).create();
        """
      Then the result should:
        """
        : {
          subBean1: null
          subBean2: null
        }
        """

    Scenario: depends on primitive default value sub property
      And the following spec class:
        """
        public class ABean extends Spec<Bean> {
          public void main() {
            property("str1").dependsOn("str2.empty", s -> String.valueOf(s));
          }
        }
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).create();
        """
      Then the result should:
        """
        : {
          str1: 'false'
          str2: /^str2.*/
        }
        """

    Scenario: depends on list of ObjectProducer means list spec/value not list original value
      Given the following bean class:
        """
        public class Bean {
          public List<Bean> beans=new ArrayList<>();
          public String str;
        }
        """
      And the following spec class:
        """
        public class ABean extends Spec<Bean> {
          public void main() {
            property("str").dependsOn("beans.empty", s -> String.valueOf(s));
          }
        }
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).property("beans[0]", null).create();
        """
      Then the result should:
        """
        : {
          str: 'false'
          beans: [null]
        }
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).create();
        """
      Then the result should:
        """
        : {
          str: /^str.*/
          beans: []
        }
        """

    Scenario: should consider optional spec default value producer sub property as PlaceHolderProducer
      Given the following bean class:
        """
        public class SubBean {
          public String str;
        }
        """
      Given the following bean class:
        """
        public class Bean {
          public SubBean subBean;
          public Bean bean;
        }
        """
      And the following spec class:
        """
        public class SubBeanSpec extends Spec<SubBean> {}
        """
      And the following spec class:
        """
        public class ABean extends Spec<Bean> {
          public void main() {
              property("subBean").byFactory();
              property("bean").optional("SubBeanSpec");
              link("subBean", "bean.subBean");
          }
        }
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).create();
        """
      Then the result should:
        """
        : {
          subBean: {...}
          bean: null
        }
        """
