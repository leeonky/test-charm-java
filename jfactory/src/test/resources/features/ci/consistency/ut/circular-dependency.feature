Feature: circular dependency

  Rule: circular dependency produce

    Background:
      Given the following bean class:
        """
        public class Bean {
          public String str1, str2, str3, str4;
        }
        """

    Scenario: str1 <-> str2
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

    Scenario: str1 -> str2 -> str3 -> str1
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
      Then the result should:
        """
        str1= .str2,
        str2= .str3
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

    Scenario: whole collection and readonly item
      Given the following bean class:
      """
      public class Bean {
        public String strings[];
      }
      """
      And the following spec class:
      """
      public class ABean extends Spec<Bean> {
        @Override
        public void main() {
          property("strings[2]").dependsOn("strings[1]", obj -> obj);
          property("strings[1]").dependsOn("strings[0]", obj -> obj);
        }
      }
      """
      When build:
      """
      jFactory.spec(ABean.class)
        .property("strings[0]", "hello")
      .create();
      """
      Then the result should:
      """
      strings= [hello hello hello]
      """
