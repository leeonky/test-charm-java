Feature: resolve consistency

  Rule: cascade processing consistency

    Background:
      Given the following bean class:
        """
        public class Bean {
          public String str1, str2, str3, str4;
        }
        """

    Scenario: resolve unrelated consistencies
      And the following spec class:
        """
        public class ABean extends Spec<Bean> {
          public void main() {
            link("str1", "str2");
            link("str3", "str4");
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

    Scenario: should resolve cascaded impacts after one consistency is resolved
      And the following spec class:
        """
        public class ABean extends Spec<Bean> {
          public void main() {
            consistent(String.class)
              .direct("str1")
              .direct("str2");

            consistent(String.class)
              .direct("str3")
              .<String>property("str2")
                .read(s->s)
                .write(s->s);

            consistent(String.class)
              .direct("str4")
              .<String>property("str2")
                .read(s->s)
                .write(s->s);
          }
        }
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).property("str1", "hello").create();
        """
      Then the result should:
        """
        <<str1, str2, str3, str4>>= hello
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).property("str2", "hello").create();
        """
      Then the result should:
        """
        <<str1, str2, str3, str4>>= hello
        """

  Rule: consistency priority

    Background:
      Given the following bean class:
        """
        public class Bean {
          public String str1, str2, str3, str4;
        }
        """

    Scenario: choose consistency with higher priority of input data
      And the following spec class:
        """
        public class ABean extends Spec<Bean> {
          public void main() {
            consistent(String.class)
              .direct("str2")
              .direct("str1");

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
        jFactory.clear().spec(ABean.class).property("str3", "hello").create();
        """
      Then the result should:
        """
        <<str1,str2,str3>>= hello
        """

    Scenario: dependency order in consistency definition should not impact the result
      And register:
        """
        jFactory.factory(Bean.class).spec(spec -> {
          spec.property("str1").dependsOn("str2");
          spec.property("str3").dependsOn("str4");
          spec.property("str2").dependsOn("str3");
        });
        """
      When build:
        """
        jFactory.clear().type(Bean.class).create();
        """
      Then the result should:
        """
        <<str1,str2,str3, str4>>= /^str4.*/
        """
