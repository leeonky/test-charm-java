Feature: Object to List

  Background:
    Given the following declarations:
      """
      JFactory jFactory = new JFactory();
      """
    And the following bean definition:
      """
      public class Bean {
        public Object list;
      }
      """
    And the following spec definition:
      """
      public class Element {
        public String value1, value2;
      }
      """

  Rule: In Parent Spec by is List<T>

    Background:
      And the following spec definition:
        """
        public class ListSpec extends Spec<java.util.List<Element>> {}
        """
      And the following spec definition:
        """
        public class BeanSpec extends Spec<Bean> {
          public void main() {
            property("list").is(ListSpec.class);
          }
        }
        """

    Scenario: Create Default Empty List without Input Properties
      When evaluating the following code:
        """
        jFactory.spec(BeanSpec.class).create();
        """
      Then the result should be:
        """
        list= []
        """

    Scenario: Create Default with Specified Default Sub
      When evaluating the following code:
        """
        jFactory.spec(BeanSpec.class).property("list[0]", new HashMap<>()).create();
        """
      Then the result should be:
        """
        list= [{
          value1= /^value1.*/
          value2= /^value2.*/
          class.simpleName= Element
        }]
        """

    Scenario: Create with Sub Property
      When evaluating the following code:
        """
        jFactory.spec(BeanSpec.class).property("list[0].value1", "v1").create();
        """
      Then the result should be:
        """
        list= [{
          value1= v1
          value2= /^value2.*/
          class.simpleName= Element
        }]
        """

    Scenario: Create with Sub Property Query
      Given register as follows:
        """
        jFactory.type(Element.class).property("value1", "v1").property("value2", "v2").create();
        """
      When evaluating the following code:
        """
        jFactory.spec(BeanSpec.class).property("list[0].value1", "v1").create();
        """
      Then the result should be:
        """
        list= [{
          value1= v1
          value2= v2
        }]
        """

    Scenario: Query with Sub Property
      Given register as follows:
        """
        Element e = jFactory.type(Element.class).property("value1", "v1").property("value2", "v2").create();
        jFactory.type(Bean.class).property("list", new Object[]{e}).create();
        """
      When evaluating the following code:
        """
        jFactory.spec(BeanSpec.class).property("list[0].value1", "v1").query();
        """
      Then the result should be:
        """
        list= [{
          value1= v1
          value2= v2
        }]
        """

  Rule: In Parent Spec by is []

    Background:
      Given the following spec definition:
        """
        public class ElementSpec extends Spec<Element> {}
        """
      And the following spec definition:
        """
        public class BeanSpec extends Spec<Bean> {
          public void main() {
            property("list[]").is(ElementSpec.class);
          }
        }
        """

    Scenario: Create Default Empty List without Input Properties
      When evaluating the following code:
        """
        jFactory.spec(BeanSpec.class).create();
        """
      Then the result should be:
        """
        list= []
        """

    Scenario: Create Default with Specified Default Sub
      When evaluating the following code:
        """
        jFactory.spec(BeanSpec.class).property("list[0]", new HashMap<>()).create();
        """
      Then the result should be:
        """
        list= [{
          value1= /^value1.*/
          value2= /^value2.*/
          class.simpleName= Element
        }]
        """

    Scenario: Create with Sub Property
      When evaluating the following code:
        """
        jFactory.spec(BeanSpec.class).property("list[0].value1", "v1").create();
        """
      Then the result should be:
        """
        list= [{
          value1= v1
          value2= /^value2.*/
          class.simpleName= Element
        }]
        """

    Scenario: Create with Sub Property Query
      Given register as follows:
        """
        jFactory.type(Element.class).property("value1", "v1").property("value2", "v2").create();
        """
      When evaluating the following code:
        """
        jFactory.spec(BeanSpec.class).property("list[0].value1", "v1").create();
        """
      Then the result should be:
        """
        list= [{
          value1= v1
          value2= v2
        }]
        """

    Scenario: Query with Sub Property
      Given register as follows:
        """
        Element e = jFactory.type(Element.class).property("value1", "v1").property("value2", "v2").create();
        jFactory.type(Bean.class).property("list", new Object[]{e}).create();
        """
      When evaluating the following code:
        """
        jFactory.spec(BeanSpec.class).property("list[0].value1", "v1").query();
        """
      Then the result should be:
        """
        list= [{
          value1= v1
          value2= v2
        }]
        """

  Rule: Input Property Spec

    Background:
      Given the following spec definition:
        """
        public class ElementSpec extends Spec<Element> {
          @Trait
          public void v2() {
            property("value2").value("v2");
          }
        }
        """
      And the following spec definition:
        """
        public class ListSpec extends Spec<java.util.List<Element>> {}
        """
      And register as follows:
        """
        jFactory.register(ElementSpec.class);
        jFactory.register(ListSpec.class);
        """

    Scenario: Create Default with Specified Default Sub
      When evaluating the following code:
        """
        jFactory.type(Bean.class).property("list(ListSpec)[0]", new HashMap()).create();
        """
      And the following spec definition:
        """
        public class ListSpec extends Spec<java.util.List<Element>> {}
        """
      Then the result should be:
        """
        list= [{
          value1= /^value1.*/
          value2= /^value2.*/
          class.simpleName= Element
        }]
        """

    Scenario: Create Default with Sub Properties
      When evaluating the following code:
        """
        jFactory.type(Bean.class).property("list(ListSpec)[0].value1", "v1").create();
        """
      Then the result should be:
        """
        list: [{
          value1= v1
          class.simpleName= Element
        }]
        """

    Scenario: Create with Sub Property
      When evaluating the following code:
        """
        jFactory.type(Bean.class).property("list(ListSpec)[0].value1", "v1").create();
        """
      Then the result should be:
        """
        list: [{
          value1= v1
          class.simpleName= Element
        }]
        """

    Scenario: Create with Sub Property Query
      Given register as follows:
        """
        jFactory.type(Element.class).property("value1", "v1").property("value2", "v2").create();
        """
      When evaluating the following code:
        """
        jFactory.type(Bean.class).property("list(ListSpec)[0].value1", "v1").create();
        """
      Then the result should be:
        """
        list: [{
          value1= v1
          value2= v2
          class.simpleName= Element
        }]
        """

    Scenario: Query with Sub Property
      Given register as follows:
        """
        Element e = jFactory.type(Element.class).property("value1", "v1").property("value2", "v2").create();
        jFactory.type(Bean.class).property("list", new Object[]{e}).create();
        """
      When evaluating the following code:
        """
        jFactory.type(Bean.class).property("list(ListSpec)[0].value1", "v1").query();
        """
      Then the result should be:
        """
        list: [{
          value1= v1
          value2= v2
          class.simpleName= Element
        }]
        """

#    Scenario: Use Trait in SubSpec
#      Given the following bean definition:
#        """
#        public class Bean {
#          public <type> list;
#        }
#        """
#      When evaluating the following code:
#        """
#        jFactory.type(Bean.class).property("list[0](v2 SubSpec).value1", "v1").create();
#        """
#      Then the result should be:
#        """
#        list: [{
#          value1= v1
#          value2= v2
#          class.simpleName= Sub
#        }]
#        """

    Scenario: Specify Element Spec
      Given the following spec definition:
        """
        public class ElementSpec extends Spec<Element> {
          public void main() {
            property("value2").value("v2");
          }
        }
        """
      When evaluating the following code:
        """
        jFactory.type(Bean.class).property("list(ListSpec)[0](ElementSpec).value1", "v1").create();
        """
      Then the result should be:
        """
        list: [{
          value1= v1
          value2= v2
          class.simpleName= Element
        }]
        """


    Scenario: Use Element Trait in Element Spec
      Given the following spec definition:
        """
        public class ElementSpec extends Spec<Element> {

          @Trait
          public void v2() {
            property("value2").value("v2");
          }
        }
        """
      When evaluating the following code:
        """
        jFactory.type(Bean.class).property("list(ListSpec)[0](v2 ElementSpec).value1", "v1").create();
        """
      Then the result should be:
        """
        list: [{
          value1= v1
          value2= v2
          class.simpleName= Element
        }]
        """
