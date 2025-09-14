Feature: property link

  Background:
    Given declaration jFactory =
    """
    new JFactory();
    """

  Rule: top level dependency

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
      And operate:
        """
        jFactory.getDataRepository().clear();
        """
      When build:
        """
        jFactory.spec(ABean.class).property("str2", "hello").create();
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

#    Scenario: multi properties link
#      Given the following bean class:
#        """
#        public class Person {
#          public String fullName, firstName, lastName, familyName, givenName;
#        }
#        """
#      And the following spec class:
#        """
#        public class APerson extends Spec<Person> {
#          public void main() {
#             consistent<String>()
#                .link(directly("fullName")),
#                .link(properties("firstName", "lastName").compose(names -> String.join(" ", names)).decompose(s -> s.split(" "))),
#                .link(properties("familyName", "givenName").compose(names -> String.join(" ", names)).decompose(s -> s.split(" "))));
#          }
#        }
#        """
#      When build:
#        """
#        jFactory.spec(APerson.class).property("str2", "hello").create();
#        """
#      Then the result should:
#        """
#        <<str1,str2>>= hello
#        """
