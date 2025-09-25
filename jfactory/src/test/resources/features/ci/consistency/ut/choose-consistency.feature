Feature: choose consistency by priority to resolve

  Rule: basic usage in same bean

    Background:

      Given the following bean class:
        """
        public class Bean {
          public String str1, str2, str3, str4;
        }
        """

    Scenario: resolve all consistencies
      And the following spec class:
        """
        public class ABean extends Spec<Bean> {
          public void main() {
            linkNew("str1", "str2");
            linkNew("str3", "str4");
          }
        }
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).property("str1", "hello").create();
        """
      Then the result should:
        """
        <<str1,str2>>= hello,
        <<str3,str4>>= /^str3.*/
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).property("str2", "hello").create();
        """
      Then the result should:
        """
        <<str1,str2>>= hello,
        <<str3,str4>>= /^str3.*/
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).create();
        """
      Then the result should:
        """
        <<str1,str2>>= /^str1.*/,
        <<str3,str4>>= /^str3.*/
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).property("str3", "hello").create();
        """
      Then the result should:
        """
        <<str1,str2>>= /^str1.*/,
        <<str3,str4>>= hello
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).property("str4", "hello").create();
        """
      Then the result should:
        """
        <<str1,str2>>= /^str1.*/,
        <<str3,str4>>= hello
        """
