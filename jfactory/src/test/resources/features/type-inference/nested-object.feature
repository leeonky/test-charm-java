Feature: Nested object specialization via Spec
  Create a concrete subtype for a nested property

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

  Rule: By is(...) in the parent spec

    Background:
      Given the following spec definition:
        """
        public class BeanSpec extends Spec<Bean> {
          public void main() {
            property("object").is(SubSpec.class);
          }
        }
        """

    Scenario Outline: Create Root Object without any Sub-Object Properties
      Given the following bean definition:
        """
        public class Bean<generic> {
          public <type> object;
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
          value2= /^value2.*/
          class.simpleName= Sub
        }
        """
      Examples:
        | type   | generic |
        | Sub    |         |
        | Super  |         |
        | Object |         |
        | T      | <T>     |

    Scenario Outline: Create Default Sub-Object without Specifying its Properties
      Given the following bean definition:
        """
        public class Bean<generic> {
          public <type> object;
        }
        """
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
      Examples:
        | type   | generic |
        | Sub    |         |
        | Super  |         |
        | Object |         |
        | T      | <T>     |

    Scenario Outline: Create Sub-Object with Given Properties
      Given the following bean definition:
        """
        public class Bean<generic> {
          public <type> object;
        }
        """
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
      Examples:
        | type   | generic |
        | Sub    |         |
        | Super  |         |
        | Object |         |
        | T      | <T>     |

    Scenario Outline: Reuse Previously Created Sub-Object by Matching its Properties
      Given the following bean definition:
        """
        public class Bean<generic> {
          public <type> object;
        }
        """
      And register as follows:
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
      Examples:
        | type   | generic |
        | Sub    |         |
        | Super  |         |
        | Object |         |
        | T      | <T>     |

    Scenario Outline: Query Root Object by Sub-Object Properties
      Given the following bean definition:
        """
        public class Bean<generic> {
          public <type> object;
        }
        """
      And register as follows:
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
      Examples:
        | type   | generic |
        | Sub    |         |
        | Super  |         |
        | Object |         |
        | T      | <T>     |

    Scenario Outline: Use Trait in SubSpec
      Given the following bean definition:
        """
        public class Bean<generic> {
          public <type> object;
        }
        """
      And the following spec definition:
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
      Examples:
        | type   | generic |
        | Sub    |         |
        | Super  |         |
        | Object |         |
        | T      | <T>     |

  Rule: By apply(...) in the parent spec

    Background:
      Given the following spec definition:
        """
        public class BeanSpec extends Spec<Bean> {
          public void main() {
            property("object").apply("SubSpec");
          }
        }
        """

    Scenario: Create Root Object without any Sub-Object Properties
      When evaluating the following code:
        """
        jFactory.spec(BeanSpec.class).create();
        """
      Then the result should be:
        """
        object: null
        """

    Scenario Outline: Create Default Sub-Object without Specifying its Properties
      Given the following bean definition:
        """
        public class Bean<generic> {
          public <type> object;
        }
        """
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
      Examples:
        | type   | generic |
        | Sub    |         |
        | Super  |         |
        | Object |         |
        | T      | <T>     |

    Scenario Outline: Create Sub-Object with Given Properties
      Given the following bean definition:
        """
        public class Bean<generic> {
          public <type> object;
        }
        """
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
      Examples:
        | type   | generic |
        | Sub    |         |
        | Super  |         |
        | Object |         |
        | T      | <T>     |

    Scenario Outline: Reuse Previously Created Sub-Object by Matching its Properties
      Given the following bean definition:
        """
        public class Bean<generic> {
          public <type> object;
        }
        """
      And register as follows:
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
      Examples:
        | type   | generic |
        | Sub    |         |
        | Super  |         |
        | Object |         |
        | T      | <T>     |

    Scenario Outline: Query Root Object by Sub-Object Properties
      Given the following bean definition:
        """
        public class Bean<generic> {
          public <type> object;
        }
        """
      And register as follows:
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
      Examples:
        | type   | generic |
        | Sub    |         |
        | Super  |         |
        | Object |         |
        | T      | <T>     |

    Scenario Outline: Use Trait in SubSpec
      Given the following bean definition:
        """
        public class Bean<generic> {
          public <type> object;
        }
        """
      And the following spec definition:
        """
        public class BeanSpec extends Spec<Bean> {
          public void main() {
            property("object").apply("v2", "SubSpec");
          }
        }
        """
      When evaluating the following code:
        """
        jFactory.spec(BeanSpec.class).property("object", new HashMap()).create();
        """
      Then the result should be:
        """
        object: {
          value1= /^value1.*/
          value2= v2
          class.simpleName= Sub
        }
        """
      Examples:
        | type   | generic |
        | Sub    |         |
        | Super  |         |
        | Object |         |
        | T      | <T>     |

  Rule: By input child property Spec

    Scenario Outline: Create Default Sub-Object without Specifying its Properties
      Given the following bean definition:
        """
        public class Bean<generic> {
          public <type> object;
        }
        """
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
      Examples:
        | type   | generic |
        | Sub    |         |
        | Super  |         |
        | Object |         |
        | T      | <T>     |

    Scenario Outline: Create Sub-Object with Given Properties
      Given the following bean definition:
        """
        public class Bean<generic> {
          public <type> object;
        }
        """
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
      Examples:
        | type   | generic |
        | Sub    |         |
        | Super  |         |
        | Object |         |
        | T      | <T>     |

    Scenario Outline: Reuse Previously Created Sub-Object by Matching its Properties
      Given the following bean definition:
        """
        public class Bean<generic> {
          public <type> object;
        }
        """
      And register as follows:
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
      Examples:
        | type   | generic |
        | Sub    |         |
        | Super  |         |
        | Object |         |
        | T      | <T>     |

    Scenario Outline: Query Root Object by Sub-Object Properties
      Given the following bean definition:
        """
        public class Bean<generic> {
          public <type> object;
        }
        """
      And register as follows:
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
      Examples:
        | type   | generic |
        | Sub    |         |
        | Super  |         |
        | Object |         |
        | T      | <T>     |

    Scenario Outline: Use Trait in SubSpec
      Given the following bean definition:
        """
        public class Bean<generic> {
          public <type> object;
        }
        """
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
      Examples:
        | type   | generic |
        | Sub    |         |
        | Super  |         |
        | Object |         |
        | T      | <T>     |

    Scenario Outline: Create with Sub Properties (Merge Spec)
      Given the following bean definition:
        """
        public class Bean<generic> {
          public <type> object;
        }
        """
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
      Examples:
        | type   | generic |
        | Sub    |         |
        | Super  |         |
        | Object |         |
        | T      | <T>     |

  Rule: By input child property spec override in parent spec is(...)

    Background:
      Given the following spec definition:
        """
        public class OriginalSupSpec extends Spec<Sub> {}
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

    Scenario Outline: Create Default Sub-Object without Specifying its Properties -x
      Given the following bean definition:
        """
        public class Bean<generic> {
          public <type> object;
        }
        """
      When evaluating the following code:
        """
        jFactory.spec(BeanSpec.class).property("object(SubSpec)", new HashMap()).create();
        """
      Then the result should be:
        """
        object: {
          value1= /^value1.*/
          value2= New
          class.simpleName= Sub
        }
        """
      Examples:
        | type   | generic |
        | Sub    |         |
        | Super  |         |
        | Object |         |
        | T      | <T>     |

    Scenario Outline: Create Sub-Object with Given Properties x
      Given the following bean definition:
        """
        public class Bean<generic> {
          public <type> object;
        }
        """
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
      Examples:
        | type   | generic |
        | Sub    |         |
        | Super  |         |
        | Object |         |
        | T      | <T>     |

    Scenario Outline: Reuse Previously Created Sub-Object by Matching its Properties
      Given the following bean definition:
        """
        public class Bean<generic> {
          public <type> object;
        }
        """
      And register as follows:
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
      Examples:
        | type   | generic |
        | Sub    |         |
        | Super  |         |
        | Object |         |
        | T      | <T>     |

    Scenario Outline: Query Root Object by Sub-Object Properties
      Given the following bean definition:
        """
        public class Bean<generic> {
          public <type> object;
        }
        """
      And register as follows:
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
      Examples:
        | type   | generic |
        | Sub    |         |
        | Super  |         |
        | Object |         |
        | T      | <T>     |

    Scenario Outline: Use Trait in SubSpec
      Given the following bean definition:
        """
        public class Bean<generic> {
          public <type> object;
        }
        """
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
      Examples:
        | type   | generic |
        | Sub    |         |
        | Super  |         |
        | Object |         |
        | T      | <T>     |

    Scenario Outline: Create with Sub Properties (Merge Spec)
      Given the following bean definition:
        """
        public class Bean<generic> {
          public <type> object;
        }
        """
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
      Examples:
        | type   | generic |
        | Sub    |         |
        | Super  |         |
        | Object |         |
        | T      | <T>     |

  Rule: By input child property spec override in parent spec apply(...)

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
            property("object").apply("OriginalSupSpec");
          }
        }
        """
      And register as follows:
        """
        jFactory.register(OriginalSupSpec.class);
        jFactory.register(SubSpec.class);
        """

    Scenario Outline: Create Default Sub-Object without Specifying its Properties
      Given the following bean definition:
        """
        public class Bean<generic> {
          public <type> object;
        }
        """
      When evaluating the following code:
        """
        jFactory.spec(BeanSpec.class).property("object(SubSpec)", new HashMap()).create();
        """
      Then the result should be:
        """
        object: {
          value1= /^value1.*/
          value2= New
          class.simpleName= Sub
        }
        """
      Examples:
        | type   | generic |
        | Sub    |         |
        | Super  |         |
        | Object |         |
        | T      | <T>     |

    Scenario Outline: Create Sub-Object with Given Properties
      Given the following bean definition:
        """
        public class Bean<generic> {
          public <type> object;
        }
        """
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
      Examples:
        | type   | generic |
        | Sub    |         |
        | Super  |         |
        | Object |         |
        | T      | <T>     |

    Scenario Outline: Reuse Previously Created Sub-Object by Matching its Properties
      Given the following bean definition:
        """
        public class Bean<generic> {
          public <type> object;
        }
        """
      And register as follows:
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
      Examples:
        | type   | generic |
        | Sub    |         |
        | Super  |         |
        | Object |         |
        | T      | <T>     |

    Scenario Outline: Query Root Object by Sub-Object Properties
      Given the following bean definition:
        """
        public class Bean<generic> {
          public <type> object;
        }
        """
      And register as follows:
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
      Examples:
        | type   | generic |
        | Sub    |         |
        | Super  |         |
        | Object |         |
        | T      | <T>     |

    Scenario Outline: Use Trait in SubSpec
      Given the following bean definition:
        """
        public class Bean<generic> {
          public <type> object;
        }
        """
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
      Examples:
        | type   | generic |
        | Sub    |         |
        | Super  |         |
        | Object |         |
        | T      | <T>     |

    Scenario Outline: Create with Sub Properties (Merge Spec)
      Given the following bean definition:
        """
        public class Bean<generic> {
          public <type> object;
        }
        """
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
      Examples:
        | type   | generic |
        | Sub    |         |
        | Super  |         |
        | Object |         |
        | T      | <T>     |
