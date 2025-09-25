Feature: circular dependency

  Rule: circular dependency produce

    Background:
      Given the following bean class:
        """
        public class Bean {
          public String str1, str2, str3, str4;
        }
        """

    Scenario: simple circular dependency
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
                .<String>property("str2")
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
        str1= /^str1.*/, str2= /^str1.*/
        """
