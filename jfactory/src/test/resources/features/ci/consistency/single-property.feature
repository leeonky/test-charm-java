Feature: single property consistency

  Background:
    Given declaration jFactory =
    """
    new JFactory();
    """

  Rule: basic usage in same bean

    Scenario: link property with the same type directly
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
          jFactory.clear().spec(ABean.class).property("str1", "hello").create();
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
              .property("i").read(Object::toString).write(Integer::parseInt);
          }
        }
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).property("str", "100").create();
        """
      Then the result should:
        """
        : {
          str: '100'
          i: 100
        }
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).property("i", 50).create();
        """
      Then the result should:
        """
        : {
          str: '50'
          i: 50
        }
        """

  Rule: need merge

    Background:

      Given the following bean class:
        """
        public class Bean {
          public String str1, str2, str3;
        }
        """

    Scenario: merge in the same property with the same link
      Given the following spec class:
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

    Scenario: same property, same type, same composer, same decomposer merge
      Given the following spec class:
        """
        public class ABean extends Spec<Bean> {
          public void main() {
              Function<String,String> toUpper = String::toUpperCase;
              Function<String,String> toLower = String::toLowerCase;

              consistent(String.class)
              .<String>property("str1")
                .read(toUpper).write(toLower)
              .direct("str2");

              consistent(String.class)
              .<String>property("str1")
                .read(toUpper).write(toLower)
              .direct("str3");
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
          str1= hello
          str2= HELLO
          str3= HELLO
        }
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).property("str1", "HELLO").create();
        """
      Then the result should:
        """
        : {
          str1= HELLO
          str2= HELLO
          str3= HELLO
        }
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).property("str2", "HELLO").create();
        """
      Then the result should:
        """
        : {
          str1= hello
          str2= HELLO
          str3= HELLO
        }
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).property("str3", "HELLO").create();
        """
      Then the result should:
        """
        : {
          str1= hello
          str2= HELLO
          str3= HELLO
        }
        """

    Scenario: same property, same type, same composer, no decomposer merge
      Given the following spec class:
        """
        public class ABean extends Spec<Bean> {
          public void main() {
              Function<String,String> toUpper = String::toUpperCase;
              Function<String,String> toLower = String::toLowerCase;

              consistent(String.class)
              .<String>property("str1")
                .read(toUpper)
              .direct("str2");

              consistent(String.class)
              .<String>property("str1")
                .read(toUpper)
              .direct("str3");
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
          str1= hello
          str2= HELLO
          str3= HELLO
        }
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).property("str2", "HELLO").create();
        """
      Then the result should:
        """
        : {
          str1= /^str1.*/
          str2= HELLO
          str3= HELLO
        }
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).property("str3", "HELLO").create();
        """
      Then the result should:
        """
        : {
          str1= /^str1.*/
          str2= HELLO
          str3= HELLO
        }
        """

    Scenario: same property, same type, no composer, same decomposer merge
      Given the following spec class:
        """
        public class ABean extends Spec<Bean> {
          public void main() {
              Function<String,String> toUpper = String::toUpperCase;
              Function<String,String> toLower = String::toLowerCase;

              consistent(String.class)
              .<String>property("str1")
                .write(toLower)
              .direct("str2");

              consistent(String.class)
              .<String>property("str1")
                .write(toLower)
              .direct("str3");
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
          str1= hello
          str2= /^str2.*/
          str3= /^str2.*/
        }
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).property("str2", "HELLO").create();
        """
      Then the result should:
        """
        : {
          str1= hello
          str2= HELLO
          str3= HELLO
        }
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).property("str3", "HELLO").create();
        """
      Then the result should:
        """
        : {
          str1= hello
          str2= HELLO
          str3= HELLO
        }
        """

  Rule: conflict property

    Scenario: need merge but different consistency type
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
                .read(Object::toString)
                .write(Integer::parseInt);

            consistent(Integer.class)
              .direct("i")
              .<String>property("str2")
                .read(Integer::parseInt)
                .write(Object::toString);
          }
        }
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).create();
        """
      Then should raise error:
        """
        message.table:[
          ['Conflict consistency on property <i>, consistency type mismatch:'],
          ['', type composer decomposer],
          [#package#ABean.main(ABean.java:9) java.lang.String '(ABean.java:10)' '(ABean.java:11)'],
          [#package#ABean.main(ABean.java:14) java.lang.Integer '(ABean.java:14)' '(ABean.java:14)']
        ]
        """

    Scenario: error show correct location of link
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
                .read(Integer::parseInt)
                .write(Object::toString);
          }
        }
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).property("i", 100).create();
        """
      Then should raise error:
        """
        message.table: [
          ['Conflict consistency on property <str2>, consistency type mismatch:'],
          ['', type composer decomposer],
          [#package#ABean.main(ABean.java:7) java.lang.Object '(ABean.java:7)' '(ABean.java:7)'],
          [#package#ABean.main(ABean.java:11) java.lang.Integer '(ABean.java:12)' '(ABean.java:13)']
        ]
        """

    Scenario Outline: Same property + same type, different composer / decomposer: error
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
              Function<String, String> any1 = s -> {
                  throw new RuntimeException("Should not be called");
              };
              Function<String, String> any2 = s -> {
                  throw new RuntimeException("Should not be called");
              };
              consistent(String.class)
              .<String>property("str1")
                <composer1>
                <decomposer1>
              .<String>property("str2");

              consistent(String.class)
              .<String>property("str1")
                <composer2>
                <decomposer2>
              .<String>property("str3");
          }
        }
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).create();
        """
      Then should raise error:
        """
        message.table: [
          ['Conflict consistency on property <str1>, <error>:'],
          ['', type composer decomposer],
          [#package#ABean.main(ABean.java:14) java.lang.String '<composer1Pos>' '<decomposer1Pos>'],
          [#package#ABean.main(ABean.java:20) java.lang.String '<composer2Pos>' '<decomposer2Pos>']
        ]
        """
      Examples:
        | composer1   | composer2   | decomposer1  | decomposer2  | composer1Pos    | composer2Pos    | decomposer1Pos  | decomposer2Pos  | error                            |
        |             |             | .write(any1) | .write(any2) | null            | null            | (ABean.java:16) | (ABean.java:22) | decomposer mismatch              |
        | .read(any1) | .read(any2) |              |              | (ABean.java:15) | (ABean.java:21) | null            | null            | composer mismatch                |
        | .read(any1) | .read(any2) | .write(any1) | .write(any2) | (ABean.java:15) | (ABean.java:21) | (ABean.java:16) | (ABean.java:22) | composer and decomposer mismatch |
        | .read(any1) |             | .write(any1) | .write(any2) | (ABean.java:15) | null            | (ABean.java:16) | (ABean.java:22) | decomposer mismatch              |
        |             | .read(any2) | .write(any1) | .write(any2) | null            | (ABean.java:21) | (ABean.java:16) | (ABean.java:22) | decomposer mismatch              |
        | .read(any1) | .read(any2) |              | .write(any2) | (ABean.java:15) | (ABean.java:21) | null            | (ABean.java:22) | composer mismatch                |
        | .read(any1) | .read(any2) | .write(any1) |              | (ABean.java:15) | (ABean.java:21) | (ABean.java:16) | null            | composer mismatch                |

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
                    .read(String::toUpperCase)
                    .write(String::toLowerCase)
                  .direct("str2");

                consistent(String.class)
                  .<String>property("str1")
                    .read(String::toUpperCase)
                    .write(String::toLowerCase)
                  .direct("str3");
            }
        }
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).property("str1", "hello").create();
        """
      Then should raise error:
        """
        message.table: [
          ['Conflict consistency on property <str1>, composer and decomposer mismatch:'],
          ['', type composer decomposer],
          [#package#ABean.main(ABean.java:8) java.lang.String '(ABean.java:9)' '(ABean.java:10)'],
          [#package#ABean.main(ABean.java:14) java.lang.String '(ABean.java:15)' '(ABean.java:16)']
        ]
        """

    Scenario Outline: Same property + different type, association with composer and decomposer
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
              Function<Object, String> reader1= any -> {
                throw new RuntimeException();
              };
              Function<String, Object> writer1= any -> {
                throw new RuntimeException();
              };
              Function<Object, Object> reader2= any -> {
                throw new RuntimeException();
              };
              Function<Object, Object> writer2= any -> {
                throw new RuntimeException();
              };
              consistent(String.class)
              .property("str1")
                <composer1>
                <decomposer1>
              .property("str2");

              consistent(Object.class)
              .property("str1")
                <composer2>
                <decomposer2>
              .property("str3");
          }
        }
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).create();
        """
      Then should raise error:
        """
        message.table: [
          ['Conflict consistency on property <str1>, <error>:'],
          ['', type composer decomposer],
          [#package#ABean.main(ABean.java:20) java.lang.String '<composer1Pos>' '<decomposer1Pos>'],
          [#package#ABean.main(ABean.java:26) java.lang.Object '<composer2Pos>' '<decomposer2Pos>']
        ]
        """
      Examples:
        | composer1      | composer2      | decomposer1     | decomposer2     | composer1Pos    | composer2Pos    | decomposer1Pos  | decomposer2Pos  | error                     |
        |                |                | .write(writer1) | .write(writer2) | null            | null            | (ABean.java:22) | (ABean.java:28) | consistency type mismatch |
        | .read(reader1) | .read(reader2) |                 |                 | (ABean.java:21) | (ABean.java:27) | null            | null            | consistency type mismatch |
        | .read(reader1) | .read(reader2) | .write(writer1) | .write(writer2) | (ABean.java:21) | (ABean.java:27) | (ABean.java:22) | (ABean.java:28) | consistency type mismatch |
        | .read(reader1) | .read(reader2) |                 | .write(writer2) | (ABean.java:21) | (ABean.java:27) | null            | (ABean.java:28) | consistency type mismatch |
        | .read(reader1) | .read(reader2) | .write(writer1) |                 | (ABean.java:21) | (ABean.java:27) | (ABean.java:22) | null            | consistency type mismatch |
        | .read(reader1) |                | .write(writer1) | .write(writer2) | (ABean.java:21) | null            | (ABean.java:22) | (ABean.java:28) | consistency type mismatch |
        |                | .read(reader2) | .write(writer1) | .write(writer2) | null            | (ABean.java:27) | (ABean.java:22) | (ABean.java:28) | consistency type mismatch |

  Rule: Same property + same type no merge

    Background:
      Given the following bean class:
        """
        public class Bean {
          public String str1, str2, str3;
        }
        """

    Scenario: composer: null null and decomposer: null null
      And the following spec class:
        """
        public class ABean extends Spec<Bean> {
          public void main() {
              consistent(String.class)
              .<String>property("str1")
              .direct("str2");

              consistent(String.class)
              .<String>property("str1")
              .direct("str3");
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
          str2: /^str2.*/
          str3: /^str3.*/
          }
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).property("str2", "hello").create();
        """
      Then the result should:
        """
        : {
          str1: /^str1.*/
          str2: hello
          str3: /^str3.*/
        }
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).property("str3", "hello").create();
        """
      Then the result should:
        """
        : {
          str1: /^str1.*/
          str2: /^str2.*/
          str3: hello
        }
        """

    Scenario: composer: null null and decomposer: null not_null
      And the following spec class:
        """
        public class ABean extends Spec<Bean> {
          public void main() {
              consistent(String.class)
              .<String>property("str1")
              .direct("str2");

              consistent(String.class)
              .<String>property("str1")
                .write(s->s)
              .direct("str3");
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
          str2: /^str2.*/
          str3: /^str3.*/
          }
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).property("str2", "hello").create();
        """
      Then the result should:
        """
        : {
          str1: /^str3.*/
          str2: hello
          str3: /^str3.*/
        }
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).property("str3", "hello").create();
        """
      Then the result should:
        """
        : {
          str1: hello
          str2: /^str2.*/
          str3: hello
        }
        """

    Scenario: composer: null null and decomposer: not_null null
      And the following spec class:
        """
        public class ABean extends Spec<Bean> {
            public void main() {
                consistent(String.class)
                .<String>property("str1")
                    .write(s->s)
                .direct("str2");

                consistent(String.class)
                .<String>property("str1")
                .direct("str3");
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
          str2: /^str2.*/
          str3: /^str3.*/
        }
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).property("str2", "hello").create();
        """
      Then the result should:
        """
        : {
          str1: hello
          str2: hello
          str3: /^str3.*/
        }
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).property("str3", "hello").create();
        """
      Then the result should:
        """
        : {
          str1: /^str2.*/
          str2: /^str2.*/
          str3: hello
        }
        """

    Scenario: composer: null not_null and decomposer: null null
      And the following spec class:
        """
        public class ABean extends Spec<Bean> {
            public void main() {
                consistent(String.class)
                .<String>property("str1")
                .direct("str2");

                consistent(String.class)
                .<String>property("str1")
                    .read(s->s)
                .direct("str3");
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
          str2: /^str2.*/
          str3: hello
        }
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).property("str2", "hello").create();
        """
      Then the result should:
        """
        : {
          str1: /^str1.*/
          str2: hello
          str3: /^str1.*/
        }
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).property("str3", "hello").create();
        """
      Then the result should:
        """
        : {
          str1: /^str1.*/
          str2: /^str2.*/
          str3: hello
        }
        """

    Scenario: composer: not_null null and decomposer: null null
      And the following spec class:
        """
        public class ABean extends Spec<Bean> {
            public void main() {
                consistent(String.class)
                .<String>property("str1")
                    .read(s->s)
                .direct("str2");

                consistent(String.class)
                .<String>property("str1")
                .direct("str3");
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
          str2: hello
          str3: /^str3.*/
        }
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).property("str2", "hello").create();
        """
      Then the result should:
        """
        : {
          str1: /^str1.*/
          str2: hello
          str3: /^str3.*/
        }
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).property("str3", "hello").create();
        """
      Then the result should:
        """
        : {
          str1: /^str1.*/
          str2: /^str1.*/
          str3: hello
        }
        """

    Scenario: composer: not_null null and decomposer: not_null null
      And the following spec class:
        """
        public class ABean extends Spec<Bean> {
          public void main() {
              consistent(String.class)
              .<String>property("str1")
                  .read(s->s)
                  .write(s->s)
              .direct("str2");

              consistent(String.class)
              .<String>property("str1")
              .direct("str3");
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
          str2: hello
          str3: /^str3.*/
        }
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).property("str2", "hello").create();
        """
      Then the result should:
        """
        : {
          str1: hello
          str2: hello
          str3: /^str3.*/
        }
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).property("str3", "hello").create();
        """
      Then the result should:
        """
        : {
          str1: /^str1.*/
          str2: /^str1.*/
          str3: hello
        }
        """

    Scenario: composer: null not_null and decomposer: null not_null
      And the following spec class:
        """
        public class ABean extends Spec<Bean> {
          public void main() {
              consistent(String.class)
              .<String>property("str1")
              .direct("str2");

              consistent(String.class)
              .<String>property("str1")
                  .read(s->s)
                  .write(s->s)
              .direct("str3");
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
          str2: /^str2.*/
          str3: hello
        }
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).property("str2", "hello").create();
        """
      Then the result should:
        """
        : {
          str1: /^str1.*/
          str2: hello
          str3: /^str1.*/
        }
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).property("str3", "hello").create();
        """
      Then the result should:
        """
        : {
          str1: hello
          str2: /^str2.*/
          str3: hello
        }
        """

  Rule: Same property + different type no merge

    Background:
      Given the following bean class:
        """
        public class Bean {
          public String str1, str2, str3;
        }
        """

    Scenario: composer: null null and decomposer: null null
      And the following spec class:
        """
        public class ABean extends Spec<Bean> {
          public void main() {
              consistent(String.class)
                .<String>property("str1")
                .direct("str2");

              consistent(StringBuilder.class)
                .property("str1")
                .<String>property("str3")
                  .read(StringBuilder::new)
                  .write(StringBuilder::toString);
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
          str2: /^str2.*/
          str3: /^str3.*/
        }
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).property("str2", "hello").create();
        """
      Then the result should:
        """
        : {
          str1: /^str1.*/
          str2: hello
          str3: /^str3.*/
        }
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).property("str3", "hello").create();
        """
      Then the result should:
        """
        : {
          str1: /^str1.*/
          str2: /^str2.*/
          str3: hello
        }
        """

    Scenario: composer: null null and decomposer: null not_null
      And the following spec class:
        """
        public class ABean extends Spec<Bean> {
          public void main() {
              consistent(String.class)
                  .<String>property("str1")
                  .direct("str2");

              consistent(StringBuilder.class)
                  .property("str1")
                    .write(StringBuilder::toString)
                  .<String>property("str3")
                    .read(StringBuilder::new)
                    .write(StringBuilder::toString);
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
          str2: /^str2.*/
          str3: /^str3.*/
        }
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).property("str2", "hello").create();
        """
      Then the result should:
        """
        : {
          str1: /^str3.*/
          str2: hello
          str3: /^str3.*/
        }
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).property("str3", "hello").create();
        """
      Then the result should:
        """
        : {
          str1: hello
          str2: /^str2.*/
          str3: hello
        }
        """

    Scenario: composer: null null and decomposer: not_null null
      And the following spec class:
        """
        public class ABean extends Spec<Bean> {
          public void main() {
              consistent(String.class)
                  .<String>property("str1")
                      .write(s->s)
                  .direct("str2");

              consistent(StringBuilder.class)
                  .property("str1")
                  .<String>property("str3")
                      .read(StringBuilder::new)
                      .write(StringBuilder::toString);
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
          str2: /^str2.*/
          str3: /^str3.*/
        }
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).property("str2", "hello").create();
        """
      Then the result should:
        """
        : {
          str1: hello
          str2: hello
          str3: /^str3.*/
        }
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).property("str3", "hello").create();
        """
      Then the result should:
        """
        : {
          str1: /^str2.*/
          str2: /^str2.*/
          str3: hello
        }
        """

    Scenario: composer: null not_null and decomposer: null null
      And the following spec class:
        """
        public class ABean extends Spec<Bean> {
          public void main() {
              consistent(String.class)
                  .property("str1")
                  .direct("str2");

              consistent(StringBuilder.class)
                  .<String>property("str1")
                      .read(StringBuilder::new)
                  .<String>property("str3")
                      .read(StringBuilder::new)
                      .write(StringBuilder::toString);
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
          str2: /^str2.*/
          str3: hello
        }
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).property("str2", "hello").create();
        """
      Then the result should:
        """
        : {
          str1: /^str1.*/
          str2: hello
          str3: /^str1.*/
        }
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).property("str3", "hello").create();
        """
      Then the result should:
        """
        : {
          str1: /^str1.*/
          str2: /^str2.*/
          str3: hello
        }
        """

    Scenario: composer: not_null null and decomposer: null null
      And the following spec class:
        """
        public class ABean extends Spec<Bean> {
          public void main() {
              consistent(String.class)
                  .<String>property("str1")
                    .read(s->s)
                  .direct("str2");

              consistent(StringBuilder.class)
                  .property("str1")
                  .<String>property("str3")
                      .read(StringBuilder::new)
                      .write(StringBuilder::toString);
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
          str2: hello
          str3: /^str3.*/
        }
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).property("str2", "hello").create();
        """
      Then the result should:
        """
        : {
          str1: /^str1.*/
          str2: hello
          str3: /^str3.*/
        }
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).property("str3", "hello").create();
        """
      Then the result should:
        """
        : {
          str1: /^str1.*/
          str2: /^str1.*/
          str3: hello
        }
        """

    Scenario Outline: Same property + same type, composer and decomposer - no merge
      And the following spec class:
        """
        public class ABean extends Spec<Bean> {
          public void main() {
              Function<String, String> any = s -> {
                throw new RuntimeException("Should not be called");
              };
              consistent(String.class)
              .<String>property("str1")
                <composer1>
                <decomposer1>
              .<String>property("str2");

              consistent(String.class)
              .<String>property("str1")
                <composer2>
                <decomposer2>
              .<String>property("str3");
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
          str2: /^str2.*/
          str3: /^str3.*/
          }
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).property("str2", "hello").create();
        """
      Then the result should:
        """
        : {
          str1: /^str1.*/
          str2: hello
          str3: /^str3.*/
        }
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).property("str3", "hello").create();
        """
      Then the result should:
        """
        : {
          str1: /^str1.*/
          str2: /^str2.*/
          str3: hello
        }
        """
      Examples:
        | composer1  | composer2  | decomposer1 | decomposer2 |
#        |            |            |             |             |
#        |            |            |             | .write(any) |
#        |            |            | .write(any) |             |
#        | .read(any) |            |             |             |
#        |            | .read(any) |             |             |
#        |            | .read(any) |             | .write(any) |
        | .read(any) |            |             | .write(any) |
        |            | .read(any) | .write(any) |             |
#        | .read(any) |            | .write(any) |             |

  Rule: resolution order

    Background:
      Given the following bean class:
        """
        public class Bean {
          public String str1, str2, str3, str4;
        }
        """

    Scenario: should resolve property which has writer first and reader last
      And the following spec class:
        """
        public class ABean extends Spec<Bean> {
          public void main() {
              consistent(String.class)
                .<String>property("str1")
                  .read(s->s)
                .direct("str2");

              consistent(String.class)
                .<String>property("str1")
                  .write(s->s)
                .direct("str3");
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
          str2: hello
          str3: /^str3.*/
        }
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).property("str2", "hello").create();
        """
      Then the result should:
        """
        : {
          str1: /^str3.*/
          str2: hello
          str3: /^str3.*/
        }
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).property("str3", "hello").create();
        """
      Then the result should:
        """
        : {
          str1: hello
          str2: hello
          str3: hello
        }
        """

    Scenario: raise error when conflict dependent between two consistencies
      And the following spec class:
        """
        public class ABean extends Spec<Bean> {
          public void main() {
              consistent(String.class)
                .<String>property("str1")
                  .read(s->s)
                .<String>property("str2")
                  .write(s->s)
                .direct("str3");

              consistent(String.class)
                .<String>property("str1")
                  .write(s->s)
                .<String>property("str2")
                  .read(s->s)
                .direct("str4");
          }
        }
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).create();
        """
      Then should raise error:
        """
        message.table: [
          ['Conflict dependency between consistencies:'],
          ['', type composer decomposer],
          [#package#ABean.main(ABean.java:8) java.lang.String '(ABean.java:9)' 'null'],
          [#package#ABean.main(ABean.java:15) java.lang.String 'null' '(ABean.java:16)'],
          ['', type composer decomposer],
          [#package#ABean.main(ABean.java:17) java.lang.String '(ABean.java:18)' 'null'],
          [#package#ABean.main(ABean.java:10) java.lang.String 'null' '(ABean.java:11)'],
        ]
        """

    Scenario: raise error when recursive dependent between consistencies
      And the following spec class:
        """
        public class ABean extends Spec<Bean> {
          public void main() {
              consistent(String.class)
                .<String>property("str1")
                  .read(s->s)
                .<String>property("str2")
                  .write(s->s);

              consistent(String.class)
                .<String>property("str1")
                  .write(s->s)
                .<String>property("str3")
                  .read(s->s);

              consistent(String.class)
                .<String>property("str2")
                  .read(s->s)
                .<String>property("str3")
                  .write(s->s);
          }
        }
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).create();
        """
      Then should raise error:
        """
        message: ```
                 Circular dependency detected between:
                   java.lang.String:
                     #package#ABean.main(ABean.java:7)
                   java.lang.String:
                     #package#ABean.main(ABean.java:13)
                   java.lang.String:
                     #package#ABean.main(ABean.java:19)
                 ```
        """

      # multi properties dependency check