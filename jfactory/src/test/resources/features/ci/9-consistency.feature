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
            consistent(String.class)
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

  Rule: multiple properties consistency

    Scenario: two properties consistency
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
                .compose((first,last) -> first+" "+last).decompose(s -> s.split(" "))
              .properties("familyName", "givenName")
                .compose(names -> names[0]+" "+names[1]).decompose(s->s.split(" ")[0], s->s.split(" ")[1]);
          }
        }
        """
      When build:
        """
        jFactory.clear().spec(APerson.class).property("fullName", "James Anderson").create();
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
      When build:
        """
        jFactory.clear().spec(APerson.class).property("firstName", "James").property("lastName", "Anderson").create();
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

  Rule: merge consistency

    Scenario: merge with the same link
      Given the following bean class:
        """
        public class Bean {
          public String str1, str2, str3;
        }
        """
      And the following spec class:
        """
        public class ABean extends Spec<Bean> {
          public void main() {
            linkNew("str1", "str2");
            linkNew("str2", "str3");
          }
        }
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).property("str1", "hello").create();
        """
      Then the result should:
        """
        <<str1,str2,str3>>= hello
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).property("str2", "hello").create();
        """
      Then the result should:
        """
        <<str1,str2,str3>>= hello
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).property("str3", "hello").create();
        """
      Then the result should:
        """
        <<str1,str2,str3>>= hello
        """

    Scenario: raise error when different consistency type
      Given the following bean class:
        """
        public class Bean {
          public String str1, str2;
          public int i;
        }
        """
      And the following spec class:
        """
        public class ABean extends Spec<Bean> {
          public void main() {
            consistent(String.class)
              .direct("str1")
              .property("i")
                .compose(Object::toString)
                .decompose(Integer::parseInt);

            consistent(Integer.class)
              .direct("i")
              .<String>property("str2")
                .compose(Integer::parseInt)
                .decompose(Object::toString);
          }
        }
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).property("i", 100).create();
        """
      Then should raise error:
        """
        message: ```
                 Conflict consistency on property <i>, consistency type mismatch:
                                                                   | type              | composer        | decomposer      |
                   #package#ABean.main(ABean.java:9)  | java.lang.String  | (ABean.java:10) | (ABean.java:11) |
                   #package#ABean.main(ABean.java:14) | java.lang.Integer | (ABean.java:14) | (ABean.java:14) |
                 ```
        """

    Scenario: raise error when different consistency type - show location in link
      Given the following bean class:
        """
        public class Bean {
          public String str1, str2;
          public int i;
        }
        """
      And the following spec class:
        """
        public class ABean extends Spec<Bean> {
          public void main() {
            linkNew("str1", "str2");

            consistent(Integer.class)
              .direct("i")
              .<String>property("str2")
                .compose(Integer::parseInt)
                .decompose(Object::toString);
          }
        }
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).property("i", 100).create();
        """
      Then should raise error:
        """
        message: ```
                 Conflict consistency on property <str2>, consistency type mismatch:
                                                                   | type              | composer        | decomposer      |
                   #package#ABean.main(ABean.java:7)  | java.lang.Object  | (ABean.java:7)  | (ABean.java:7)  |
                   #package#ABean.main(ABean.java:11) | java.lang.Integer | (ABean.java:12) | (ABean.java:13) |
                 ```
        """

    Scenario: merge with the same consistency type, property, composer, decomposer
      Given the following bean class:
        """
        public class Bean {
          public String str1, str2, str3;
        }
        """
      And the following spec class:
        """
        public class ABean extends Spec<Bean> {
            public void main() {
                Function<String,String> toUpper = s -> s.toUpperCase();
                Function<String,String> toLower = s -> s.toLowerCase();

                consistent(String.class)
                  .<String>property("str1")
                    .compose(toUpper)
                    .decompose(toLower)
                  .<String>property("str2")
                    .compose(String::toLowerCase)
                    .decompose(String::toUpperCase);

                consistent(String.class)
                  .<String>property("str1")
                    .compose(toUpper)
                    .decompose(toLower)
                  .<String>property("str3")
                    .compose(String::toLowerCase)
                    .decompose(String::toUpperCase);
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
          str2: HELLO
          str3: HELLO
        }
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).property("str2", "HELLO").create();
        """
      Then the result should:
        """
        : {
          str1: hello
          str2: HELLO
          str3: HELLO
        }
        """

    Scenario: raise error when composer and decompose is not the same lambda instance
      Given the following bean class:
        """
        public class Bean {
          public String str1, str2, str3;
        }
        """
      And the following spec class:
        """
        public class ABean extends Spec<Bean> {
            public void main() {
                consistent(String.class)
                  .<String>property("str1")
                    .compose(String::toUpperCase)
                    .decompose(String::toLowerCase)
                  .<String>property("str2")
                    .compose(String::toLowerCase)
                    .decompose(String::toUpperCase);

                consistent(String.class)
                  .<String>property("str1")
                    .compose(String::toUpperCase)
                    .decompose(String::toLowerCase)
                  .<String>property("str3")
                    .compose(String::toLowerCase)
                    .decompose(String::toUpperCase);
            }
        }
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).property("str1", "hello").create();
        """
      Then should raise error:
        """
        message: ```
                 Conflict consistency on property <str1>, composer and decomposer mismatch:
                                                                   | type             | composer        | decomposer      |
                   #package#ABean.main(ABean.java:8)  | java.lang.String | (ABean.java:9)  | (ABean.java:10) |
                   #package#ABean.main(ABean.java:16) | java.lang.String | (ABean.java:17) | (ABean.java:18) |
                 ```
        """

    Scenario: raise error when composer is not the same lambda instance
      Given the following bean class:
        """
        public class Bean {
          public String str1, str2, str3;
        }
        """
      And the following spec class:
        """
        public class ABean extends Spec<Bean> {
            public void main() {
                consistent(String.class)
                  .<String>property("str1")
                    .compose(String::toUpperCase)
                  .<String>property("str2")
                    .compose(String::toLowerCase);

                consistent(String.class)
                  .<String>property("str1")
                    .compose(String::toUpperCase)
                  .<String>property("str3")
                    .compose(String::toLowerCase);
            }
        }
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).property("str1", "hello").create();
        """
      Then should raise error:
        """
        message: ```
                 Conflict consistency on property <str1>, composer mismatch:
                                                                   | type             | composer        | decomposer |
                   #package#ABean.main(ABean.java:8)  | java.lang.String | (ABean.java:9)  | null       |
                   #package#ABean.main(ABean.java:14) | java.lang.String | (ABean.java:15) | null       |
                 ```
        """

    Scenario: raise error when decomposer is not the same lambda instance
      Given the following bean class:
        """
        public class Bean {
          public String str1, str2, str3;
        }
        """
      And the following spec class:
        """
        public class ABean extends Spec<Bean> {
            public void main() {
                consistent(String.class)
                  .<String>property("str1")
                    .decompose(String::toLowerCase)
                  .<String>property("str2")
                    .decompose(String::toUpperCase);

                consistent(String.class)
                  .<String>property("str1")
                    .decompose(String::toLowerCase)
                  .<String>property("str3")
                    .decompose(String::toUpperCase);
            }
        }
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).property("str1", "hello").create();
        """
      Then should raise error:
        """
        message: ```
                 Conflict consistency on property <str1>, decomposer mismatch:
                                                                   | type             | composer | decomposer      |
                   #package#ABean.main(ABean.java:8)  | java.lang.String | null     | (ABean.java:9)  |
                   #package#ABean.main(ABean.java:14) | java.lang.String | null     | (ABean.java:15) |
                 ```
        """

