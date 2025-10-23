Feature: consistency merge

  Background:
    Given declaration jFactory =
    """
    new JFactory();
    """

  Rule: without merge

    Background:

      Given the following bean class:
      """
      public class Bean {
        public String str1, str2, str3, str4;
      }
      """

    Scenario: merge in the same property with the same link
      Given the following spec class:
        """
        public class ABean extends Spec<Bean> {
          public void main() {
            link("str1", "str2");
            link("str2", "str3");
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

    Scenario: merge repeatedly until no items can be merged
      Given the following spec class:
        """
        public class ABean extends Spec<Bean> {
          public void main() {
            link("str1", "str2");
            link("str3", "str4");
            link("str2", "str3");
          }
        }
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).property("str1", "hello").create();
        """
      Then the result should:
        """
        <<str1,str2,str3,str4>>= hello
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).property("str2", "hello").create();
        """
      Then the result should:
        """
        <<str1,str2,str3,str4>>= hello
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).property("str3", "hello").create();
        """
      Then the result should:
        """
        <<str1,str2,str3,str4>>= hello
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).property("str4", "hello").create();
        """
      Then the result should:
        """
        <<str1,str2,str3,str4>>= hello
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

    Scenario: merge multi properties
      Given the following bean class:
        """
        public class Bean {
          public String str1, str2, str3, str4;
        }
        """
      And the following spec class:
        """
        public class ABean extends Spec<Bean> {
            public void main() {
                BiFunction<String,String,String> join = (s1, s2)-> s1+"#"+s2;
                Function<String,Object[]> divide = s->s.split("#");
                consistent(String.class)
                  .<String, String>properties("str1", "str2")
                    .read(join)
                    .write(divide)
                  .direct("str3");

                consistent(String.class)
                  .<String, String>properties("str1", "str2")
                    .read(join)
                    .write(divide)
                  .direct("str4");
            }
        }
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).property("str4", "hello#world").create();
        """
      Then the result should:
        """
        : {
          str1: hello
          str2: world
          str3: hello#world
          str4: hello#world
        }
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).property("str3", "good#bye").create();
        """
      Then the result should:
        """
        : {
          str1: good
          str2: bye
          str3: good#bye
          str4: good#bye
        }
        """

  Rule: without merge

    Background:
      Given the following bean class:
        """
        public class Bean {
          public String str1, str2, str3, str4, str5;
        }
        """

    Scenario: single reader and single writer not merge
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

    Scenario: multi properties single reader and single writer not merge
      And the following spec class:
        """
        public class BeanSpec extends Spec<Bean> {
            public void main() {
              consistent(String.class)
                .<String, String>properties("str1", "str2")
                  .write(s -> new Object[]{s, s})
                .direct("str4");

              consistent(String.class)
                .<String, String>properties("str1", "str3")
                  .read((s1, s2) -> s1+"#"+s2)
                .direct("str5");
            }
        }
        """
      When build:
        """
        jFactory.clear().spec(BeanSpec.class).property("str1", "hello").create();
        """
      Then the result should:
        """
        str1= hello, str2= /^str4.*/, str3= /^str3.*/, str4= /^str4.*/, str5= /^hello#str3.*/
        """
      When build:
        """
        jFactory.clear().spec(BeanSpec.class).property("str2", "hello").create();
        """
      Then the result should:
        """
        str1= /^str4.*/, str2= hello, str3= /^str3.*/, str4= /^str4.*/, str5= /^str4.*#str3.*/
        """
      When build:
        """
        jFactory.clear().spec(BeanSpec.class).property("str4", "hello").create();
        """
      Then the result should:
        """
        str1= hello, str2= hello, str3= /^str3.*/, str4= hello, str5= /^hello#str3.*/
        """
      When build:
        """
        jFactory.clear().spec(BeanSpec.class).property("str3", "hello").create();
        """
      Then the result should:
        """
        str1= /^str4.*/, str2= /^str4.*/, str3= hello, str4= /^str4.*/, str5= /^str4.*#hello/
        """
      When build:
        """
        jFactory.clear().spec(BeanSpec.class).property("str5", "hello").create();
        """
      Then the result should:
        """
        str1= /^str4.*/, str2= /^str4.*/, str3= /^str3.*/, str4= /^str4.*/, str5= hello
        """
