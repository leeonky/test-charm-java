Feature: resolve consistency and item

  Background:
    Given declaration jFactory =
    """
    new JFactory();
    """

  Rule: resolve consistency

    Background:

      Given the following bean class:
        """
        public class Bean {
          public String str1, str2, str3, str4;
        }
        """

    Scenario: single item of consistency do nothing
      And the following spec class:
        """
        public class ABean extends Spec<Bean> {
          public void main() {
            consistent(String.class)
              .direct("str1");
          }
        }
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).create();
        """
      Then the result should:
        """
        str1= /^str1.*/, str2= /^str2.*/
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).property("str1", "hello").create();
        """
      Then the result should:
        """
        str1= hello, str2= /^str2.*/
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).property("str2", "hello").create();
        """
      Then the result should:
        """
        str1= /^str1.*/, str2= hello
        """

  Rule: distinct item in list

    Scenario: remove duplicated item
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
            property("str1").value("foo");

            Function<String,String> f = s->s;
            Function<String,String> f2 = s->s+"hello";

            consistent(String.class)
              .<String>property("str1")
                .read(f)
                .write(f2)
              .<String>property("str1")
                .read(f)
                .write(f2)
              .direct("str2");
          }
        }
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).create();
        """
      Then the result should:
        """
        str1= foo, str2= foo
        """
