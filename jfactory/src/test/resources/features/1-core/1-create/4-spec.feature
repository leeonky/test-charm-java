Feature: Spec

  Background:
    Given the following declarations:
      """
      JFactory jFactory = new JFactory();
      """
    And the following bean definition:
      """
      public class Bean {
        public String stringValue;
        public int intValue;
      }
      """

  Rule: Type Spec

    Scenario: Base Spec for a Type - Define Default Base Rules for a Type and Create an Object with the Base Spec
      When register as follows:
        """
        jFactory.factory(Bean.class).spec(ins -> ins.spec()
          .property("stringValue").value("hello")
          .property("intValue").value(100)
        );
        """
      And evaluating the following code:
        """
        jFactory.create(Bean.class);
        """
      Then the result should be:
        """
        : {
          stringValue= hello
          intValue= 100
        }
        """

    Scenario: Trait - Define Naming Spec as a Trait
      When register as follows:
        """
        jFactory.factory(Bean.class)
          .spec("hello", ins -> ins.spec().property("stringValue").value("hello"));
        """
      And evaluating the following code:
        """
        jFactory.type(Bean.class).traits("hello").create();
        """
      Then the result should be:
        """
        stringValue= hello
        """

    Scenario: Regex Trait Registration — Define a Regex Trait, then Match and Bind Captured Params on Create
      When register as follows:
        """
        jFactory.factory(Bean.class)
          .spec("string_(.*)", ins -> ins.spec().property("stringValue").value(ins.traitParams()[0]));
        """
      And evaluating the following code:
        """
        jFactory.type(Bean.class).traits("string_hello").create();
        """
      Then the result should be:
        """
        stringValue= hello
        """

  Rule: Use Spec Class

    Scenario: Spec Class - Define a Spec as a Class and Create an Object by Spec
      Given the following class definition:
        """
        import com.github.leeonky.jfactory.Spec;
        public class ABean extends Spec<Bean> {}
        """
      When evaluating the following code:
        """
        jFactory.spec(ABean.class).property("stringValue", "hello").create();
        """
      Then the result should be:
        """
        stringValue= hello
        """

    Scenario: Spec Name - Use a Spec by Name
      Given the following spec definition:
        """
        public class ABean extends Spec<Bean> {}
        """
      And register as follows:
        """
        jFactory.register(ABean.class);
        """
      When evaluating the following code:
        """
        jFactory.spec("ABean").property("stringValue", "hello").create();
        """
      Then the result should be:
        """
        stringValue= hello
        """

    Scenario: Custom Spec Name - Define a New Spec Name in the Spec Class
      Given the following spec definition:
        """
        public class ABean extends Spec<Bean> {
          protected String getName() { return "OneBean"; }
        }
        """
      And register as follows:
        """
        jFactory.register(ABean.class);
        """
      When evaluating the following code:
        """
        jFactory.spec("OneBean").property("stringValue", "hello").create();
        """
      Then the result should be:
        """
        stringValue= hello
        """

    Scenario: Trait in Spec Class - Define Traits in a Spec Class and Create an Object by Trait
      Given the following spec definition:
        """
        public class ABean extends Spec<Bean> {

          @Trait
          public void helloTrait() {
            property("stringValue").value("hello");
          }
        }
        """
      When evaluating the following code:
        """
        jFactory.spec(ABean.class).traits("helloTrait").create();
        """
      Then the result should be:
        """
        stringValue= hello
        """

    Scenario: Trait Name via Annotation — Use @Trait("name") instead of method name
      Given the following spec definition:
        """
        public class ABean extends Spec<Bean> {

          @Trait("helloTrait") //Define Trait with a Name instead of method
          public void t1() {
            property("stringValue").value("hello");
          }
        }
        """
      When evaluating the following code:
        """
        jFactory.spec(ABean.class).traits("helloTrait").create();
        """
      Then the result should be:
        """
        stringValue= hello
        """

    Scenario: Regex Trait in Spec Class - Match the Trait Name and Bind Captured Groups to Trait Method Parameters
      Given the following spec definition:
        """
        public class ABean extends Spec<Bean> {

          @Trait("value_(.*)_(.*)")
          public void stringTrait(String s, int i) {
            property("stringValue").value(s);
            property("intValue").value(i);
          }
        }
        """
      When evaluating the following code:
        """
        jFactory.spec(ABean.class).traits("value_hello_100").create();
        """
      Then the result should be:
        """
        : {
          stringValue= hello
          intValue= 100
        }
        """

#TODO Spec class and Type spec (merge, trait override)
#TODO regex trait(lambda, spec class)
#TODO global spec class as base type
#TODO global spec and base type (merge, trait override)
#TODO Spec class / global Spec class / type spec (merge, trait override)
#TODO error handler missing name missing pattern
