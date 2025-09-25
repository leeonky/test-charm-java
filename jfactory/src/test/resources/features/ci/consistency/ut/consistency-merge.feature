Feature: consistency merge

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

  Scenario: merge repeatedly until no items can be merged
    Given the following spec class:
      """
      public class ABean extends Spec<Bean> {
        public void main() {
          linkNew("str1", "str2");
          linkNew("str3", "str4");
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
