Feature: error handler

  Rule: error notation

    Scenario: error in get value from consistency provider
      Given the following bean class:
        """
        public class Bean {
          public String str1;
          public String str2;
        }
        """
      And the following spec class:
        """
        public class ABean extends Spec<Bean> {
          public void main() {
            property("str2").value(()-> {
              throw new RuntimeException("get str2 error");
            });
            link("str1", "str2");
          }
        }
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).create();
        """
      Then should raise error:
        """
        message= ```
                 Got an error when composing the intermediate value from the properties <#package#Bean.str2>:
                     java.lang.RuntimeException: get str2 error

                 Consistency:
                     java.lang.Object:
                         #package#ABean.main(ABean.java:10)
                         - str1 => #package#ABean.main(ABean.java:10)
                             composer: #package#ABean.main(ABean.java:10)
                             decomposer: #package#ABean.main(ABean.java:10)
                         - str2 => #package#ABean.main(ABean.java:10)
                             composer: #package#ABean.main(ABean.java:10)
                             ^^^^^^^^^
                             decomposer: #package#ABean.main(ABean.java:10)
                 ```
        """

    Scenario: error info when value array and properties size not match
      Given the following bean class:
        """
        public class Bean {
          public String name, first, last;
        }
        """
      And the following spec class:
        """
        public class ABean extends Spec<Bean> {
          public void main() {
            consistent(String.class)
              .direct("name")
              .properties("first", "last")
                .read((f, v)-> f+ " "+v)
                .write(name -> name.split(" "));
          }
        }
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).create();
        """
      Then should raise error:
        """
        message= ```
                 Got an error when decomposing the intermediate value to the properties <#package#Bean.first, #package#Bean.last>:
                     Writer at #package#ABean.main(ABean.java:11) should return an array with size 2 but got an array with size 1

                 Consistency:
                     java.lang.String:
                         #package#ABean.main(ABean.java:7)
                         - name => #package#ABean.main(ABean.java:8)
                             composer: #package#ABean.main(ABean.java:8)
                             decomposer: #package#ABean.main(ABean.java:8)
                         - first, last => #package#ABean.main(ABean.java:9)
                             composer: #package#ABean.main(ABean.java:10)
                             decomposer: #package#ABean.main(ABean.java:11)
                             ^^^^^^^^^^^
                 ```
        """

    Scenario: error dump (no composer)
      Given the following bean class:
        """
        public class Bean {
          public String str1;
          public String str2;
        }
        """
      And the following spec class:
        """
        public class ABean extends Spec<Bean> {
          public void main() {
            property("str2").value(()-> {
              throw new RuntimeException("get str2 error");
            });
            consistent(String.class)
              .direct("str2")
              .property("str1")
                .write(s->(String)s);
          }
        }
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).create();
        """
      Then should raise error:
        """
        message= ```
                 Got an error when composing the intermediate value from the properties <#package#Bean.str2>:
                     java.lang.RuntimeException: get str2 error

                 Consistency:
                     java.lang.String:
                         #package#ABean.main(ABean.java:10)
                         - str2 => #package#ABean.main(ABean.java:11)
                             composer: #package#ABean.main(ABean.java:11)
                             ^^^^^^^^^
                             decomposer: #package#ABean.main(ABean.java:11)
                         - str1 => #package#ABean.main(ABean.java:12)
                             decomposer: #package#ABean.main(ABean.java:13)
                 ```
        """

    Scenario: error dump (no decomposer)
      Given the following bean class:
        """
        public class Bean {
          public String str1;
          public String str2;
        }
        """
      And the following spec class:
        """
        public class ABean extends Spec<Bean> {
          public void main() {
            property("str2").value(()-> {
              throw new RuntimeException("get str2 error");
            });
            consistent(String.class)
              .property("str2")
                .read(s->(String)s)
              .property("str1")
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
        message= ```
                 Got an error when composing the intermediate value from the properties <#package#Bean.str2>:
                     java.lang.RuntimeException: get str2 error

                 Consistency:
                     java.lang.String:
                         #package#ABean.main(ABean.java:10)
                         - str2 => #package#ABean.main(ABean.java:11)
                             composer: #package#ABean.main(ABean.java:12)
                             ^^^^^^^^^
                         - str1 => #package#ABean.main(ABean.java:13)
                             decomposer: #package#ABean.main(ABean.java:14)
                 ```
        """
