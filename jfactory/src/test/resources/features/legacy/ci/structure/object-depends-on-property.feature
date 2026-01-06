Feature: object population depends on another property

  Background:
    Given declaration jFactory =
      """
      new JFactory();
      """
    Given the following bean class:
      """
      public class Bean {
        public String str;
      }
      """
    Given the following bean class:
      """
      public class Beans {
        public Bean bean;
        public String flag1, value, flag2, flag3, flag4;
      }
      """

  Scenario: object population depends on another property
    And register:
      """
      jFactory.factory(Beans.class).spec(spec -> {
        spec.property("value").value("hello");
        spec.structure("bean").dependsOn("flag1").when(f-> f.equals("yes")).populate((s, f) -> {
          s.byFactory();
        });
      });
      """
    When build:
      """
      jFactory.clear().type(Beans.class).property("flag1", "no").create();
      """
    Then the result should:
      """
      : {
        flag1: no
        bean: null
      }
      """
    When build:
      """
      jFactory.clear().type(Beans.class).property("flag1", "yes").create();
      """
    Then the result should:
      """
      : {
        flag1: yes
        bean: {
          class.simpleName: Bean
        }
      }
      """

  Scenario: object population depends on multi properties
    And register:
      """
      jFactory.factory(Beans.class).spec(spec -> {
        spec.structure("value").<String, String>dependsOn("flag1", "flag2").when((f1,f2)-> f1.equals("yes") && f2.equals("Y")).populate((s, f1, f2) -> {
          s.value(f1+f2);
        });
      });
      """
    When build:
      """
      jFactory.clear().type(Beans.class).property("flag1", "no").property("flag2", "any").create();
      """
    Then the result should:
      """
      : {
        flag1: no
        flag2: any
        value: /^value.*/
      }
      """
    When build:
      """
      jFactory.clear().type(Beans.class).property("flag1", "yes").property("flag2", "Y").create();
      """
    Then the result should:
      """
      : {
        flag1: yes
        flag2: Y
        value: yesY
      }
      """

  Scenario: object population depends on property array
    And register:
      """
      jFactory.factory(Beans.class).spec(spec -> {
        spec.structure("value").dependsOn("flag1", "flag2", "flag3").when(fs-> fs[0].equals("yes") && fs[1].equals("Y")).populate((s, fs) -> {
          s.value(""+fs[0]+fs[1]+fs[2]);
        });
      });
      """
    When build:
      """
      jFactory.clear().type(Beans.class).property("flag1", "no").property("flag2", "any").create();
      """
    Then the result should:
      """
      : {
        flag1: no
        flag2: any
        value: /^value.*/
      }
      """
    When build:
      """
      jFactory.clear().type(Beans.class).property("flag1", "yes").property("flag2", "Y").create();
      """
    Then the result should:
      """
      : {
        flag1: yes
        flag2: Y
        value: /^yesYflag3.*/
      }
      """

  Scenario: raise error when dependent value changed
    And the following spec class:
      """
      public class BeansSpec extends Spec<Beans>{
        public void main() {
          structure("value").dependsOn("flag1", "flag2", "flag3").when(fs-> fs[0].equals("yes") && fs[1].equals("Y")).populate((s, fs) -> {
            s.value(""+fs[0]+fs[1]+fs[2]);
          });
          link("flag3", "flag4");
        }
      }
      """
    When build:
      """
      jFactory.clear().spec(BeansSpec.class).property("flag1", "yes").property("flag2", "Y").create();
      """
    Then the result should:
      """
      : {
        flag1: yes
        flag2: Y
        value: /^yesYflag3.*/
      }
      """
    When build:
      """
      jFactory.clear().spec(BeansSpec.class).property("flag1", "yes").property("flag2", "Y").property("flag4", "any").create();
      """
    Then should raise error:
      """
      message: ```
               The value of #package#Beans.flag3 changed after the structure was populated.
               ```
      """

  Scenario: object population depends on another property in sub object
    Given the following bean class:
      """
      public class BeansObject {
        public Beans beans;
      }
      """
    And register:
      """
      jFactory.factory(Beans.class).spec(spec -> {
        spec.property("value").value("hello");
        spec.structure("bean").dependsOn("flag1").when(f-> f.equals("yes")).populate((s, f) -> {
          s.byFactory();
        });
      });
      """
    When build:
      """
      jFactory.clear().type(BeansObject.class).property("beans.flag1", "no").create();
      """
    Then the result should:
      """
      beans: {
        flag1: no
        bean: null
      }
      """
    When build:
      """
      jFactory.clear().type(BeansObject.class).property("beans.flag1", "yes").create();
      """
    Then the result should:
      """
      beans: {
        flag1: yes
        bean: {
          class.simpleName: Bean
        }
      }
      """

  Scenario: raise error when dependent value changed in sub object
    Given the following bean class:
      """
      public class BeansObject {
        public Beans beans;
      }
      """
    And the following spec class:
      """
      public class BeansSpec extends Spec<Beans>{
        public void main() {
          structure("value").dependsOn("flag1", "flag2", "flag3").when(fs-> fs[0].equals("yes") && fs[1].equals("Y")).populate((s, fs) -> {
            s.value(""+fs[0]+fs[1]+fs[2]);
          });
          link("flag3", "flag4");
        }
      }
      """
    When build:
      """
      jFactory.clear().type(BeansObject.class).property("beans(BeansSpec).flag1", "yes").property("beans(BeansSpec).flag2", "Y").create();
      """
    Then the result should:
      """
      beans: {
        flag1: yes
        flag2: Y
        value: /^yesYflag3.*/
      }
      """
    When build:
      """
      jFactory.clear().type(BeansObject.class).property("beans(BeansSpec).flag1", "yes").property("beans(BeansSpec).flag2", "Y").property("beans(BeansSpec).flag4", "any").create();
      """
    Then should raise error:
      """
      message: ```
               The value of #package#Beans.flag3 changed after the structure was populated.
               ```
      """

  Scenario: list population depends on another property
    Given the following bean class:
      """
      public class Beans {
        public Bean[] beans;
        public int size;
      }
      """
    And register:
      """
      jFactory.factory(Beans.class).spec(spec -> {
        spec.structure("beans").<Integer>dependsOn("size").populate((s, f) -> {
          for(int i=0; i<f; i++)
            s.element(i).byFactory();
        });
      });
      """
    When build:
      """
      jFactory.clear().type(Beans.class).property("size", 5).create();
      """
    Then the result should:
      """
      : {
        beans.class[].simpleName= [Bean Bean Bean Bean Bean ]
      }
      """
