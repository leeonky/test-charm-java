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
        public String flag1, value, flag2;
      }
      """

  Scenario: object population depends on another property
    And register:
      """
      jFactory.factory(Beans.class).spec(ins -> {
        ins.spec().property("value").value("hello");
        ins.spec().structure("bean").dependsOn("flag1").when(f-> f.equals("yes")).populate((s, f) -> {
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
      jFactory.factory(Beans.class).spec(ins -> {
        ins.spec().structure("value").<String, String>dependsOn("flag1", "flag2").when((f1,f2)-> f1.equals("yes") && f2.equals("Y")).populate((s, f1, f2) -> {
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

#TODO  depends on multi properties
#TODO  raise error when property value changed (single, multi properties)
