Feature: Super => Sub
  Specify property to Sub Type

  Background:
    Given the following declarations:
      """
      JFactory jFactory = new JFactory();
      """
    Given the following bean definition:
      """
      public class Super {}
      """
    And the following bean definition:
      """
      public class Sub extends Super{
        public String value1, value2;
      }
      """
    And the following bean definition:
      """
      public class Bean {
        public Super object;
      }
      """
    And the following spec definition:
      """
      public class SubSpec extends Spec<Sub> {
        @Trait
        public void v2() {
          property("value2").value("v2");
        }
      }
      """
    And register as follows:
      """
      jFactory.register(SubSpec.class);
      """

  Rule: In Parent Spec by is

    Background:
      Given the following spec definition:
        """
        public class BeanSpec extends Spec<Bean> {
          public void main() {
            property("object").is(SubSpec.class);
          }
        }
        """

    Scenario: Create Default without Input Properties
      When evaluating the following code:
        """
        jFactory.spec(BeanSpec.class).create();
        """
      Then the result should be:
        """
        object: {
          value1= /^value1.*/
          value2= /^value2.*/
          class.simpleName= Sub
        }
        """

    Scenario: Create with Specified Default Sub
      When evaluating the following code:
        """
        jFactory.spec(BeanSpec.class).property("object", new HashMap()).create();
        """
      Then the result should be:
        """
        object: {
          value1= /^value1.*/
          value2= /^value2.*/
          class.simpleName= Sub
        }
        """

    Scenario: Create with Sub Property
      When evaluating the following code:
        """
        jFactory.spec(BeanSpec.class).property("object.value1", "v1").create();
        """
      Then the result should be:
        """
        object: {
          value1= v1
          value2= /^value2.*/
          class.simpleName= Sub
        }
        """

    Scenario: Create with Sub Property Query
      Given register as follows:
        """
        jFactory.type(Sub.class).property("value1", "v1").property("value2", "v2").create();
        """
      When evaluating the following code:
        """
        jFactory.spec(BeanSpec.class).property("object.value1", "v1").create();
        """
      Then the result should be:
        """
        object: {
          value1= v1
          value2= v2
          class.simpleName= Sub
        }
        """

    Scenario: Query with Sub Property
      Given register as follows:
        """
        Sub sub = jFactory.type(Sub.class).property("value1", "v1").property("value2", "v2").create();
        jFactory.type(Bean.class).property("object", sub).create();
        """
      When evaluating the following code:
        """
        jFactory.spec(BeanSpec.class).property("object.value1", "v1").query();
        """
      Then the result should be:
        """
        object: {
          value1= v1
          value2= v2
          class.simpleName= Sub
        }
        """

    Scenario: Use Trait in SubSpec
      Given the following spec definition:
        """
        public class BeanSpec extends Spec<Bean> {
          public void main() {
            property("object").is("v2", "SubSpec");
          }
        }
        """
      When evaluating the following code:
        """
        jFactory.spec(BeanSpec.class).create();
        """
      Then the result should be:
        """
        object: {
          value1= /^value1.*/
          value2= v2
          class.simpleName= Sub
        }
        """

  Rule: In Parent Spec by apply

    Background:
      Given the following spec definition:
        """
        public class BeanSpec extends Spec<Bean> {
          public void main() {
            property("object").apply("SubSpec");
          }
        }
        """

    Scenario: Create Default without Input Properties Sub and Get (null)
      When evaluating the following code:
        """
        jFactory.spec(BeanSpec.class).create();
        """
      Then the result should be:
        """
        object: null
        """

    Scenario: Create with Specified Default Sub
      When evaluating the following code:
        """
        jFactory.spec(BeanSpec.class).property("object", new HashMap()).create();
        """
      Then the result should be:
        """
        object: {
          value1= /^value1.*/
          value2= /^value2.*/
          class.simpleName= Sub
        }
        """

    Scenario: Create with Sub Property
      When evaluating the following code:
        """
        jFactory.spec(BeanSpec.class).property("object.value1", "v1").create();
        """
      Then the result should be:
        """
        object: {
          value1= v1
          value2= /^value2.*/
          class.simpleName= Sub
        }
        """

    Scenario: Create with Sub Property Query
      Given register as follows:
        """
        jFactory.type(Sub.class).property("value1", "v1").property("value2", "v2").create();
        """
      When evaluating the following code:
        """
        jFactory.spec(BeanSpec.class).property("object.value1", "v1").create();
        """
      Then the result should be:
        """
        object: {
          value1= v1
          value2= v2
          class.simpleName= Sub
        }
        """

    Scenario: Query with Sub Property
      Given register as follows:
        """
        Sub sub = jFactory.type(Sub.class).property("value1", "v1").property("value2", "v2").create();
        jFactory.type(Bean.class).property("object", sub).create();
        """
      When evaluating the following code:
        """
        jFactory.spec(BeanSpec.class).property("object.value1", "v1").query();
        """
      Then the result should be:
        """
        object: {
          value1= v1
          value2= v2
          class.simpleName= Sub
        }
        """

    Scenario: Use Trait in SubSpec
      Given the following spec definition:
        """
        public class BeanSpec extends Spec<Bean> {
          public void main() {
            property("object").is("v2", "SubSpec");
          }
        }
        """
      When evaluating the following code:
        """
        jFactory.spec(BeanSpec.class).create();
        """
      Then the result should be:
        """
        object: {
          value1= /^value1.*/
          value2= v2
          class.simpleName= Sub
        }
        """

  Rule: Input Property Spec

    Scenario: Create Default with Specified Default Sub
      When evaluating the following code:
        """
        jFactory.type(Bean.class).property("object(SubSpec)", new HashMap()).create();
        """
      Then the result should be:
        """
        object: {
          value1= /^value1.*/
          value2= /^value2.*/
          class.simpleName= Sub
        }
        """

    Scenario: Create with Sub Property
      When evaluating the following code:
        """
        jFactory.type(Bean.class).property("object(SubSpec).value1", "v1").create();
        """
      Then the result should be:
        """
        object: {
          value1= v1
          value2= /^value2.*/
          class.simpleName= Sub
        }
        """

    Scenario: Create with Sub Property Query
      Given register as follows:
        """
        jFactory.type(Sub.class).property("value1", "v1").property("value2", "v2").create();
        """
      When evaluating the following code:
        """
        jFactory.type(Bean.class).property("object(SubSpec).value1", "v1").create();
        """
      Then the result should be:
        """
        object: {
          value1= v1
          value2= v2
          class.simpleName= Sub
        }
        """

    Scenario: Query with Sub Property
      Given register as follows:
        """
        Sub sub = jFactory.type(Sub.class).property("value1", "v1").property("value2", "v2").create();
        jFactory.type(Bean.class).property("object", sub).create();
        """
      When evaluating the following code:
        """
        jFactory.type(Bean.class).property("object(SubSpec).value1", "v1").query();
        """
      Then the result should be:
        """
        object: {
          value1= v1
          value2= v2
          class.simpleName= Sub
        }
        """

    Scenario: Use Trait in SubSpec
      When evaluating the following code:
        """
        jFactory.type(Bean.class).property("object(v2 SubSpec).value1", "v1").create();
        """
      Then the result should be:
        """
        object: {
          value1= v1
          value2= v2
          class.simpleName= Sub
        }
        """

    Scenario: Create with Sub Properties (Merge Spec)
      When evaluating the following code:
        """
        jFactory.type(Bean.class).property("object(SubSpec).value1", "hello").property("object.value2", "world").create();
        """
      Then the result should be:
        """
        object: {
          value1= hello
          value2= world
          class.simpleName= Sub
        }
        """

  Rule: Input Property Spec override Original Spec in Parent

    Background:
      Given the following spec definition:
        """
        public class OriginalSupSpec extends Spec<Super> {}
        """
      And the following spec definition:
        """
        public class SubSpec extends Spec<Sub> {
          public void main() {
            property("value2").value("New");
          }

          @Trait
          public void v2() {
            property("value2").value("v2");
          }
        }
        """
      And the following spec definition:
        """
        public class BeanSpec extends Spec<Bean> {
          public void main() {
            property("object").is(OriginalSupSpec.class);
          }
        }
        """
      And register as follows:
        """
        jFactory.register(SubSpec.class);
        """

    Scenario: Create Default with Sub Properties
      When evaluating the following code:
        """
        jFactory.spec(BeanSpec.class).property("object(SubSpec).value1", "v1").create();
        """
      Then the result should be:
        """
        object: {
          value1= v1
          value2= New
          class.simpleName= Sub
        }
        """

    Scenario: Create with Sub Property Query
      Given register as follows:
        """
        jFactory.type(Sub.class).property("value1", "v1").property("value2", "v2").create();
        """
      When evaluating the following code:
        """
        jFactory.spec(BeanSpec.class).property("object(SubSpec).value1", "v1").create();
        """
      Then the result should be:
        """
        object: {
          value1= v1
          value2= v2
          class.simpleName= Sub
        }
        """

    Scenario: Query with Sub Property
      Given register as follows:
        """
        Sub sub = jFactory.type(Sub.class).property("value1", "v1").property("value2", "v2").create();
        jFactory.type(Bean.class).property("object", sub).create();
        """
      When evaluating the following code:
        """
        jFactory.spec(BeanSpec.class).property("object(SubSpec).value1", "v1").query();
        """
      Then the result should be:
        """
        object: {
          value1= v1
          value2= v2
          class.simpleName= Sub
        }
        """

    Scenario: Use Trait in SubSpec
      When evaluating the following code:
        """
        jFactory.spec(BeanSpec.class).property("object(v2 SubSpec).value1", "v1").create();
        """
      Then the result should be:
        """
        object: {
          value1= v1
          value2= v2
          class.simpleName= Sub
        }
        """

    Scenario: Create with Sub Properties (Merge Spec)
      When evaluating the following code:
        """
        jFactory.spec(BeanSpec.class).property("object(SubSpec).value1", "hello").property("object.value2", "world").create();
        """
      Then the result should be:
        """
        object: {
          value1= hello
          value2= world
          class.simpleName= Sub
        }
        """
