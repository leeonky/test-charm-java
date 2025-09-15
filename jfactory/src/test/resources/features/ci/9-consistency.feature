Feature: consistency

  Background:
    Given declaration jFactory =
    """
    new JFactory();
    """

  Rule: one property

    Scenario: link property in the same bean directly
      Given the following bean class:
        """
        public class Bean {
          public String str1, str2;
        }
        """
      And the following spec class:
        """
        public class ABean extends Spec<Bean> {
          public void main() {
            linkNew("str1", "str2");
          }
        }
        """
      When build:
        """
        jFactory.spec(ABean.class).property("str1", "hello").create();
        """
      Then the result should:
        """
        <<str1,str2>>= hello
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).property("str2", "hello").create();
        """
      Then the result should:
        """
        <<str1,str2>>= hello
        """

    Scenario: consistency in different type
      Given the following bean class:
        """
        public class Bean {
          public String str;
          public int i;
        }
        """
      And the following spec class:
        """
        public class ABean extends Spec<Bean> {
          public void main() {
            this.<String>consistent()
              .direct("str")
              .property("i").compose(Object::toString).decompose(Integer::parseInt);
          }
        }
        """
      When build:
        """
        jFactory.spec(ABean.class).property("str", "100").create();
        """
      Then the result should:
        """
        : {
          str: '100'
          i: 100
        }
        """
      And operate:
        """
        jFactory.getDataRepository().clear();
        """
      When build:
        """
        jFactory.spec(ABean.class).property("i", 50).create();
        """
      Then the result should:
        """
        : {
          str: '50'
          i: 50
        }
        """

#    Scenario: merge with the same property and composer / decomposer
#      Given the following bean class:
#        """
#        public class Bean {
#          public String str1, str2, str3;
#        }
#        """
#      And the following spec class:
#        """
#        public class ABean extends Spec<Bean> {
#          public void main() {
#            linkNew("str1", "str2");
#            linkNew("str2", "str3");
#          }
#        }
#        """
#      When build:
#        """
#        jFactory.spec(ABean.class).property("str1", "hello").create();
#        """
#      Then the result should:
#        """
#        <<str1,str2,str3>>= hello
#        """
#      When build:
#        """
#        jFactory.clear().spec(ABean.class).property("str2", "hello").create();
#        """
#      Then the result should:
#        """
#        <<str1,str2,str3>>= hello
#        """
#      When build:
#        """
#        jFactory.clear().spec(ABean.class).property("str3", "hello").create();
#        """
#      Then the result should:
#        """
#        <<str1,str2,str3>>= hello
#        """

  Rule: multiple properties

    Scenario: multi properties link
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
            this.<String>consistent()
              .direct("fullName")
              .properties("firstName", "lastName")
                .compose((first,last) -> first+" "+last).decompose(s -> s.split(" "))
              .properties("familyName", "givenName")
                .compose(names -> names[0]+" "+names[1]).decompose(s->s.split(" ")[0], s->s.split(" ")[1]);
          }
        }
        """
      When build:
        """
        jFactory.spec(APerson.class).property("fullName", "James Anderson").create();
        """
      Then the result should:
        """
        : {
          fullName: 'James Anderson'
          firstName: James
          lastName: Anderson
          familyName: James
          givenName: Anderson
        }
        """

#    Scenario: merge consistency
#      Given the following bean class:
#        """
#        public class Bean {
#          public String str1, str2, str3;
#        }
#        """
#      And the following spec class:
#        """
#        public class ABean extends Spec<Bean> {
#          public void main() {
#            linkNew("str1", "str2");
#            linkNew("str2", "str3");
#          }
#        }
#        """
#      When build:
#        """
#        jFactory.spec(ABean.class).property("str1", "hello").create();
#        """
#      Then the result should:
#        """
#        <<str1,str2,sr3>>= hello
#        """
#      And operate:
#        """
#        jFactory.getDataRepository().clear();
#        """
#      When build:
#        """
#        jFactory.spec(ABean.class).property("str2", "hello").create();
#        """
#      Then the result should:
#        """
#        <<str1,str2,str3>>= hello
#        """
#      And operate:
#        """
#        jFactory.getDataRepository().clear();
#        """
#      When build:
#        """
#        jFactory.spec(ABean.class).property("str3", "hello").create();
#        """
#      Then the result should:
#        """
#        <<str1,str2,str3>>= hello
#        """
