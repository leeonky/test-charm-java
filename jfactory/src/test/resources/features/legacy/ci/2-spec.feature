Feature: use spec

  Background:
    Given declaration jFactory =
    """
    new JFactory();
    """

  Rule: circular dependency

    Background:
      Given the following bean class:
      """
      public class OrderLine {
        public String value;
        public Order order;
      }
      """

    Scenario: create parent, child is single property with reverseAssociation, string spec: should reference parent instance
      Given the following bean class:
      """
      public class Order {
        public OrderLine line;
        public String value;
      }
      """
      And the following spec class:
      """
      public class OrderLineSpec extends Spec<OrderLine> {
        public void main() {
          property("order").is("OrderSpec");
        }
      }
      """
      And the following spec class:
      """
      public class OrderSpec extends Spec<Order> {
        public void main() {
          property("line").reverseAssociation("order");
          property("line").is("OrderLineSpec");
        }
      }
      """
      When build:
      """
      jFactory.clear().spec(OrderSpec.class).create();
      """
      Then the result should:
      """
      line.order=::this
      """
      When build:
      """
      jFactory.clear().spec(OrderSpec.class).property("line.value", "hello").create();
      """
      Then the result should:
      """
      line: {
        order= ::root
        value= hello
      }
      """

    Scenario: create parent, child is single property with reverseAssociation, string spec query/create: should reference parent instance
      Given the following bean class:
      """
      public class Order {
        public OrderLine line;
      }
      """
      And the following spec class:
      """
      public class OrderLineSpec extends Spec<OrderLine> {
        public void main() {
          property("order").from("OrderSpec").and(b->b);
        }
      }
      """
      And the following spec class:
      """
      public class OrderSpec extends Spec<Order> {
        public void main() {
          property("line").reverseAssociation("order");
          property("line").is("OrderLineSpec");
        }
      }
      """
      When build:
      """
      jFactory.clear().spec(OrderSpec.class).create();
      """
      Then the result should:
      """
      line.order=::this
      """

    Scenario: create parent, child is single property with reverseAssociation, class spec: should reference parent instance
      Given the following bean class:
      """
      public class Order {
        public OrderLine line;
      }
      """
      And the following spec class:
      """
      public class OrderLineSpec extends Spec<OrderLine> {
        public void main() {
          property("order").is(OrderSpec.class);
        }
      }
      """
      And the following spec class:
      """
      public class OrderSpec extends Spec<Order> {
        public void main() {
          property("line").reverseAssociation("order");
          property("line").is("OrderLineSpec");
        }
      }
      """
      When build:
      """
      jFactory.clear().spec(OrderSpec.class).create();
      """
      Then the result should:
      """
      line.order=::this
      """

    Scenario: create parent, child is single property with reverseAssociation, class spec query/create: should reference parent instance
      Given the following bean class:
      """
      public class Order {
        public OrderLine line;
      }
      """
      And the following spec class:
      """
      public class OrderLineSpec extends Spec<OrderLine> {
        public void main() {
          property("order").from(OrderSpec.class).and(b->b);
        }
      }
      """
      And the following spec class:
      """
      public class OrderSpec extends Spec<Order> {
        public void main() {
          property("line").reverseAssociation("order");
          property("line").is("OrderLineSpec");
        }
      }
      """
      When build:
      """
      jFactory.clear().spec(OrderSpec.class).create();
      """
      Then the result should:
      """
      line.order=::this
      """

    Scenario: create parent, child is single property with reverseAssociation, class trait spec: should reference parent instance
      Given the following bean class:
      """
      public class Order {
        public OrderLine line;
      }
      """
      And the following spec class:
      """
      public class OrderLineSpec extends Spec<OrderLine> {
        public void main() {
          property("order").from(OrderSpec.class).which(s->{});
        }
      }
      """
      And the following spec class:
      """
      public class OrderSpec extends Spec<Order> {
        public void main() {
          property("line").reverseAssociation("order");
          property("line").is("OrderLineSpec");
        }
      }
      """
      When build:
      """
      jFactory.clear().spec(OrderSpec.class).create();
      """
      Then the result should:
      """
      line.order=::this
      """

    Scenario: create parent, child is single property with reverseAssociation, by factory: should reference parent instance
      Given the following bean class:
      """
      public class Order {
        public OrderLine line;
      }
      """
      And the following spec class:
      """
      public class OrderLineSpec extends Spec<OrderLine> {
        public void main() {
          property("order").byFactory();
        }
      }
      """
      And register:
      """
      jFactory.factory(Order.class).spec(spec-> spec
          .property("line").reverseAssociation("order")
          .property("line").is("OrderLineSpec")
      );
      """
      When build:
      """
      jFactory.clear().create(Order.class);
      """
      Then the result should:
      """
      line.order=::this
      """

    Scenario: create parent, child is single property with reverseAssociation, by factory query/create: should reference parent instance
      Given the following bean class:
      """
      public class Order {
        public OrderLine line;
      }
      """
      And the following spec class:
      """
      public class OrderLineSpec extends Spec<OrderLine> {
        public void main() {
          property("order").byFactory(b->b);
        }
      }
      """
      And register:
      """
      jFactory.factory(Order.class).spec(spec-> spec
          .property("line").reverseAssociation("order")
          .property("line").is("OrderLineSpec")
      );
      """
      When build:
      """
      jFactory.clear().create(Order.class);
      """
      Then the result should:
      """
      line.order=::this
      """

#    Scenario: create child, parent with reverseAssociation, string spec: should reference child instance
#      Given the following bean class:
#      """
#      public class Order {
#        public OrderLine line;
#        public String value;
#      }
#      """
#      And the following spec class:
#      """
#      public class OrderLineSpec extends Spec<OrderLine> {
#        public void main() {
#          property("order").is("OrderSpec");
#        }
#      }
#      """
#      And the following spec class:
#      """
#      public class OrderSpec extends Spec<Order> {
#        public void main() {
#          property("line").reverseAssociation("order");
#          property("line").is("OrderLineSpec");
#        }
#      }
#      """
#      When build:
#      """
#      jFactory.clear().spec(OrderLineSpec.class).create();
#      """
#      Then the result should:
#      """
#      order.line=::this
#      """
#      When build:
#      """
#      jFactory.clear().type(Order.class).property("value", "hello").create();
#      """
#      Then the result should:
#      """
#      : {
#        line= null
#        value= hello
#      }
#      """
#      When build:
#      """
#      jFactory.spec(OrderLineSpec.class).property("order.value", "hello").create();
#      """
#      Then the result should:
#      """
#      order: {
#        line= null
#        value= hello
#      }
#      """

#    Scenario: create child, parent with reverseAssociation, string spec: should reference child instance identify property by property name and type
#      Given the following bean class:
#      """
#      public class Order {
#        public OrderLine line;
#        public Customer customer;
#        public String value;
#      }
#      """
#      Given the following bean class:
#      """
#      public class Customer {
#        public String value;
#        public Order order;
#      }
#      """
#      And the following spec class:
#      """
#      public class OrderLineSpec extends Spec<OrderLine> {
#        public void main() {
#          property("order").is("OrderSpec");
#        }
#      }
#      """
#      And the following spec class:
#      """
#      public class OrderSpec extends Spec<Order> {
#        public void main() {
#          property("line").reverseAssociation("order");
#          property("line").is("OrderLineSpec");
#          property("customer").reverseAssociation("order");
#          property("customer").byFactory();
#        }
#      }
#      """
#      When build:
#      """
#      jFactory.clear().spec(OrderLineSpec.class).create();
#      """
#      Then the result should:
#      """
#      order: {
#        line=::root
#        customer: {...}
#      }
#      """
#      When build:
#      """
#      jFactory.clear().type(Order.class).property("value", "hello").create();
#      """
#      Then the result should:
#      """
#      : {
#        line= null
#        value= hello
#      }
#      """
#      When build:
#      """
#      jFactory.spec(OrderLineSpec.class).property("order.value", "hello").create();
#      """
#      Then the result should:
#      """
#      order: {
#        line= null
#        value= hello
#      }
#      """

    Scenario: create parent, child is collection with reverseAssociation, string spec: should reference parent instance
      Given the following bean class:
      """
      public class Order {
        public OrderLine line[];
      }
      """
      And the following spec class:
      """
      public class OrderLineSpec extends Spec<OrderLine> {
        public void main() {
          property("order").is("OrderSpec");
        }
      }
      """
      And the following spec class:
      """
      public class OrderSpec extends Spec<Order> {
        public void main() {
          property("line").reverseAssociation("order");
          property("line[]").is("OrderLineSpec");
          property("line[0]").is("OrderLineSpec");
        }
      }
      """
      When build:
      """
      jFactory.clear().spec(OrderSpec.class).create();
      """
      Then the result should:
      """
      line: [{order= ::root}]
      """

#    Scenario: create child, parent is collection with reverseAssociation, string spec: should reference parent instance
#      Given the following bean class:
#      """
#      public class Order {
#        public OrderLine line[];
#        public String value;
#      }
#      """
#      And the following spec class:
#      """
#      public class OrderLineSpec extends Spec<OrderLine> {
#        public void main() {
#          property("order").is("OrderSpec");
#        }
#      }
#      """
#      And the following spec class:
#      """
#      public class OrderSpec extends Spec<Order> {
#        public void main() {
#          property("line").reverseAssociation("order");
#          property("line[]").is("OrderLineSpec");
#          property("line[0]").is("OrderLineSpec");
#        }
#      }
#      """
#      When build:
#      """
#      jFactory.clear().spec(OrderLineSpec.class).create();
#      """
#      Then the result should:
#      """
#      order.line: [::root]
#      """
#      When build:
#      """
#      jFactory.clear().type(Order.class).property("value", "hello").create();
#      """
#      When build:
#      """
#      jFactory.spec(OrderLineSpec.class).property("order.value", "hello").create();
#      """
#      Then the result should:
#      """
#      order: {
#        line: null
#        value: hello
#      }
#      """

#    Scenario: create child, parent is collection with reverseAssociation, string spec: should reference parent instance identify property by property name and type
#      Given the following bean class:
#      """
#      public class Order {
#        public OrderLine line[];
#        public String value;
#        public Customer customer;
#      }
#      """
#      Given the following bean class:
#      """
#      public class Customer {
#        public String value;
#        public Order order;
#      }
#      """
#      And the following spec class:
#      """
#      public class OrderLineSpec extends Spec<OrderLine> {
#        public void main() {
#          property("order").is("OrderSpec");
#        }
#      }
#      """
#      And the following spec class:
#      """
#      public class OrderSpec extends Spec<Order> {
#        public void main() {
#          property("line").reverseAssociation("order");
#          property("line[]").is("OrderLineSpec");
#          property("line[0]").is("OrderLineSpec");
#          property("customer").reverseAssociation("order");
#          property("customer").byFactory();
#        }
#      }
#      """
#      When build:
#      """
#      jFactory.clear().spec(OrderLineSpec.class).create();
#      """
#      Then the result should:
#      """
#      order: {
#        line: [::root]
#        customer: {...}
#      }
#      """
#      When build:
#      """
#      jFactory.clear().type(Order.class).property("value", "hello").create();
#      """
#      When build:
#      """
#      jFactory.spec(OrderLineSpec.class).property("order.value", "hello").create();
#      """
#      Then the result should:
#      """
#      order: {
#        line: null
#        value: hello
#      }
#      """

  Rule: narrow java.lang.Object

    Scenario: create narrow single from input spec
      Given the following bean class:
      """
      public class Bean {
        public Object bean;
      }
      """
      Given the following bean class:
      """
      public class BeanData {
        public String value1, value2;
      }
      """
      Given the following spec class:
      """
      @Global
      public class BeanDataSpec extends Spec<BeanData> {
        @Override
        public void main() {
          property("value2").value("world");
        }

        @Trait
        public void hello() {
          property("value1").value("hello");
        }
      }
      """
      When build:
      """
      jFactory.type(Bean.class).property("bean(BeanDataSpec)", new HashMap<>()).create();
      """
      Then the result should:
      """
      bean: {
        class.simpleName= BeanData
        value2= world
      }
      """
      When operate:
      """
      jFactory.getDataRepository().clear();
      """
      When build:
      """
      jFactory.type(Bean.class).property("bean(BeanDataSpec).value1", "hello").create();
      """
      Then the result should:
      """
      bean: {
        class.simpleName= BeanData
        value1= hello
        value2= world
      }
      """
      When operate:
      """
      jFactory.getDataRepository().clear();
      """
      When build:
      """
      jFactory.type(Bean.class).property("bean(hello BeanDataSpec)", new HashMap<>()).create();
      """
      Then the result should:
      """
      bean: {
        class.simpleName= BeanData
        value1= hello
        value2= world
      }
      """

    Scenario: query narrow single from input spec
      Given the following bean class:
      """
      public class Bean {
        public Object bean;
      }
      """
      Given the following bean class:
      """
      public class BeanData {
        public String value1, value2;
      }
      """
      Given the following spec class:
      """
      @Global
      public class BeanDataSpec extends Spec<BeanData> {
        @Override
        public void main() {
          property("value2").value("world");
        }

        @Trait
        public void hello() {
          property("value1").value("hello");
        }
      }
      """
      When operate:
      """
      Object bean = jFactory.type(BeanData.class).property("value1", "hello").create();
      jFactory.type(Bean.class).property("bean", bean).create();
      """
      When build:
      """
      jFactory.type(Bean.class).property("bean(BeanDataSpec).value1", "hello").queryAll();
      """
      Then the result should:
      """
      : [{
        bean : {
          class.simpleName= BeanData
          value1= hello
        }
      }]
      """

    Scenario: create narrow single from parent spec
      Given the following bean class:
      """
      public class Bean {
        public Object bean;
      }
      """
      Given the following bean class:
      """
      public class BeanData {
        public String value1, value2;
      }
      """
      Given the following spec class:
      """
      public class BeanSpec extends Spec<Bean> {
        @Override
        public void main() {
          property("bean").is("BeanDataSpec");
        }
      }
      """
      Given the following spec class:
      """
      public class BeanDataSpec extends Spec<BeanData> {
        @Override
        public void main() {
          property("value2").value("world");
        }

        @Trait
        public void hello() {
          property("value1").value("hello");
        }
      }
      """
      When build:
      """
      jFactory.spec(BeanSpec.class).property("bean", new HashMap<>()).create();
      """
      Then the result should:
      """
      bean: {
        class.simpleName= BeanData
        value2= world
      }
      """
      When operate:
      """
      jFactory.getDataRepository().clear();
      """
      When build:
      """
      jFactory.spec(BeanSpec.class).property("bean.value1", "hello").create();
      """
      Then the result should:
      """
      bean: {
        class.simpleName= BeanData
        value1= hello
        value2= world
      }
      """
      When operate:
      """
      jFactory.getDataRepository().clear();
      """
      When build:
      """
      jFactory.spec(BeanSpec.class).property("bean(hello BeanDataSpec)", new HashMap<>()).create();
      """
      Then the result should:
      """
      bean: {
        class.simpleName= BeanData
        value1= hello
        value2= world
      }
      """

    Scenario: query narrow single from parent spec
      Given the following bean class:
      """
      public class Bean {
        public Object bean;
      }
      """
      Given the following bean class:
      """
      public class BeanData {
        public String value1, value2;
      }
      """
      Given the following spec class:
      """
      public class BeanSpec extends Spec<Bean> {
        @Override
        public void main() {
          property("bean").is("BeanDataSpec");
        }
      }
      """
      Given the following spec class:
      """
      @Global
      public class BeanDataSpec extends Spec<BeanData> {
        @Override
        public void main() {
          property("value2").value("world");
        }

        @Trait
        public void hello() {
          property("value1").value("hello");
        }
      }
      """
      When operate:
      """
      Object bean = jFactory.type(BeanData.class).property("value1", "hello").create();
      jFactory.type(Bean.class).property("bean", bean).create();
      """
      When build:
      """
      jFactory.spec(BeanSpec.class).property("bean.value1", "hello").queryAll();
      """
      Then the result should:
      """
      : [{
        bean : {
          class.simpleName= BeanData
          value1= hello
        }
      }]
      """

    Scenario: create narrow single override from input spec override parent spec
      Given the following bean class:
      """
      public class Bean {
        public Object bean;
      }
      """
      Given the following bean class:
      """
      public class BeanData {
        public String value1, value2;
      }
      """
      Given the following bean class:
      """
      public class BeanData2 {
        public String value3, value4;
      }
      """
      Given the following spec class:
      """
      public class BeanSpec extends Spec<Bean> {
        @Override
        public void main() {
          property("bean").is("BeanDataSpec");
        }
      }
      """
      Given the following spec class:
      """
      public class BeanDataSpec extends Spec<BeanData> {
        @Override
        public void main() {
          property("value2").value("world");
        }

        @Trait
        public void hello() {
          property("value1").value("hello");
        }
      }
      """
      Given the following spec class:
      """
      public class BeanData2Spec extends Spec<BeanData2> {
        @Override
        public void main() {
          property("value3").value("goodbye");
        }

        @Trait
        public void java() {
          property("value4").value("java");
        }
      }
      """
      When build:
      """
      jFactory.spec(BeanSpec.class).property("bean(BeanData2Spec)", new HashMap<>()).create();
      """
      Then the result should:
      """
      bean: {
        class.simpleName= BeanData2
        value3= goodbye
      }
      """
      When operate:
      """
      jFactory.getDataRepository().clear();
      """
      When build:
      """
      jFactory.spec(BeanSpec.class).property("bean(BeanData2Spec).value4", "cucumber").create();
      """
      Then the result should:
      """
      bean: {
        class.simpleName= BeanData2
        value3= goodbye
        value4= cucumber
      }
      """
      When operate:
      """
      jFactory.getDataRepository().clear();
      """
      When build:
      """
      jFactory.spec(BeanSpec.class).property("bean(java BeanData2Spec)", new HashMap<>()).create();
      """
      Then the result should:
      """
      bean: {
        class.simpleName= BeanData2
        value3= goodbye
        value4= java
      }
      """

    Scenario: query narrow single override from input spec override parent spec
      Given the following bean class:
      """
      public class Bean {
        public Object bean;
      }
      """
      Given the following bean class:
      """
      public class BeanData {
        public String value1, value2;
      }
      """
      Given the following bean class:
      """
      public class BeanData2 {
        public String value3, value4;
      }
      """
      Given the following spec class:
      """
      public class BeanSpec extends Spec<Bean> {
        @Override
        public void main() {
          property("bean").is("BeanDataSpec");
        }
      }
      """
      Given the following spec class:
      """
      public class BeanDataSpec extends Spec<BeanData> {
        @Override
        public void main() {
          property("value2").value("world");
        }

        @Trait
        public void hello() {
          property("value1").value("hello");
        }
      }
      """
      Given the following spec class:
      """
      public class BeanData2Spec extends Spec<BeanData2> {
        @Override
        public void main() {
          property("value3").value("goodbye");
        }

        @Trait
        public void java() {
          property("value4").value("java");
        }
      }
      """
      When operate:
      """
      Object beanData2 = jFactory.type(BeanData2.class).property("value4", "cucumber").create();
      jFactory.type(Bean.class).property("bean", beanData2).create();
      """
      When build:
      """
      jFactory.spec(BeanSpec.class).property("bean(BeanData2Spec).value4", "cucumber").queryAll();
      """
      Then the result should:
      """
      : [{
        bean: {
          class.simpleName= BeanData2
          value4= cucumber
        }
      }]
      """

    Scenario Outline: create narrow element in list <list> from input spec
      Given the following bean class:
      """
      public class Bean {
        public <list> bean;
      }
      """
      Given the following bean class:
      """
      public class BeanData {
        public String value1, value2;
      }
      """
      Given the following spec class:
      """
      @Global
      public class BeanDataSpec extends Spec<BeanData> {
        @Override
        public void main() {
          property("value2").value("world");
        }

        @Trait
        public void hello() {
          property("value1").value("hello");
        }
      }
      """
      When build:
      """
      jFactory.type(Bean.class).property("bean[0](BeanDataSpec)", new HashMap<>()).create();
      """
      Then the result should:
      """
      bean: [{
        class.simpleName= BeanData
        value2= world
      }]
      """
      When operate:
      """
      jFactory.getDataRepository().clear();
      """
      When build:
      """
      jFactory.type(Bean.class).property("bean[0](BeanDataSpec).value1", "hello").create();
      """
      Then the result should:
      """
      bean: [{
        class.simpleName= BeanData
        value1= hello
        value2= world
      }]
      """
      When operate:
      """
      jFactory.getDataRepository().clear();
      """
      When build:
      """
      jFactory.type(Bean.class).property("bean[0](hello BeanDataSpec)", new HashMap<>()).create();
      """
      Then the result should:
      """
      bean: [{
        class.simpleName= BeanData
        value1= hello
        value2= world
      }]
      """
      Examples:
        | list         |
        | Object[]     |
        | List<Object> |

    Scenario Outline: query narrow element in list <list> from input spec
      Given the following bean class:
      """
      public class Bean {
        public <list> bean;
      }
      """
      Given the following bean class:
      """
      public class BeanData {
        public String value1, value2;
      }
      """
      Given the following spec class:
      """
      @Global
      public class BeanDataSpec extends Spec<BeanData> {
        @Override
        public void main() {
          property("value2").value("world");
        }

        @Trait
        public void hello() {
          property("value1").value("hello");
        }
      }
      """
      When operate:
      """
      Object beanData = jFactory.type(BeanData.class).property("value1", "hello").create();
      jFactory.type(Bean.class).property("bean[0]", beanData).create();
      """
      When build:
      """
      jFactory.type(Bean.class).property("bean[0](BeanDataSpec).value1", "hello").queryAll();
      """
      Then the result should:
      """
      : [{
        bean: [{
          class.simpleName= BeanData
          value1= hello
          value2= world
        }]
      }]
      """
      Examples:
        | list         |
        | Object[]     |
        | List<Object> |

    Scenario Outline: create narrow element in list from parent spec
      Given the following bean class:
      """
      public class Bean {
        public <list> bean;
      }
      """
      Given the following bean class:
      """
      public class BeanData {
        public String value1, value2;
      }
      """
      Given the following spec class:
      """
      public class BeanSpec extends Spec<Bean> {
        @Override
        public void main() {
          property("bean[]").is("BeanDataSpec");
        }
      }
      """
      Given the following spec class:
      """
      @Global
      public class BeanDataSpec extends Spec<BeanData> {
        @Override
        public void main() {
          property("value2").value("world");
        }

        @Trait
        public void hello() {
          property("value1").value("hello");
        }
      }
      """
      When build:
      """
      jFactory.spec(BeanSpec.class).property("bean[0]", new HashMap<>()).create();
      """
      Then the result should:
      """
      bean: [{
        class.simpleName= BeanData
        value2= world
      }]
      """
      When operate:
      """
      jFactory.getDataRepository().clear();
      """
      When build:
      """
      jFactory.spec(BeanSpec.class).property("bean[0].value1", "hello").create();
      """
      Then the result should:
      """
      bean: [{
        class.simpleName= BeanData
        value1= hello
        value2= world
      }]
      """
      When operate:
      """
      jFactory.getDataRepository().clear();
      """
      When build:
      """
      jFactory.spec(BeanSpec.class).property("bean[0](hello BeanDataSpec)", new HashMap<>()).create();
      """
      Then the result should:
      """
      bean: [{
        class.simpleName= BeanData
        value1= hello
        value2= world
      }]
      """
      Examples:
        | list         |
        | Object[]     |
        | List<Object> |

    Scenario Outline: query narrow element in list from parent spec
      Given the following bean class:
      """
      public class Bean {
        public <list> bean;
      }
      """
      Given the following bean class:
      """
      public class BeanData {
        public String value1, value2;
      }
      """
      Given the following spec class:
      """
      public class BeanSpec extends Spec<Bean> {
        @Override
        public void main() {
          property("bean[]").is("BeanDataSpec");
        }
      }
      """
      Given the following spec class:
      """
      @Global
      public class BeanDataSpec extends Spec<BeanData> {
        @Override
        public void main() {
          property("value2").value("world");
        }

        @Trait
        public void hello() {
          property("value1").value("hello");
        }
      }
      """
      When operate:
      """
      Object beanData = jFactory.type(BeanData.class).property("value1", "hello").create();
      jFactory.type(Bean.class).property("bean[0]", beanData).create();
      """
      When build:
      """
      jFactory.spec(BeanSpec.class).property("bean[0].value1", "hello").queryAll();
      """
      Then the result should:
      """
      : [{
        bean: [{
          class.simpleName= BeanData
          value1= hello
          value2= world
        }]
      }]
      """
      Examples:
        | list         |
        | Object[]     |
        | List<Object> |

    Scenario Outline: create narrow element in list from input spec override parent spec
      Given the following bean class:
      """
      public class Bean {
        public <list> bean;
      }
      """
      Given the following bean class:
      """
      public class BeanData {
        public String value1, value2;
      }
      """
      Given the following bean class:
      """
      public class BeanData2 {
        public String value3, value4;
      }
      """
      Given the following spec class:
      """
      public class BeanSpec extends Spec<Bean> {
        @Override
        public void main() {
          property("bean[]").is("BeanDataSpec");
        }
      }
      """
      Given the following spec class:
      """
      public class BeanDataSpec extends Spec<BeanData> {
        @Override
        public void main() {
          property("value2").value("world");
        }

        @Trait
        public void hello() {
          property("value1").value("hello");
        }
      }
      """
      Given the following spec class:
      """
      public class BeanData2Spec extends Spec<BeanData2> {
        @Override
        public void main() {
          property("value3").value("goodbye");
        }

        @Trait
        public void java() {
          property("value4").value("java");
        }
      }
      """
      When build:
      """
      jFactory.spec(BeanSpec.class).property("bean[0](BeanData2Spec)", new HashMap<>()).create();
      """
      Then the result should:
      """
      bean: [{
        class.simpleName= BeanData2
        value3= goodbye
      }]
      """
      When operate:
      """
      jFactory.getDataRepository().clear();
      """
      When build:
      """
      jFactory.spec(BeanSpec.class).property("bean[0](BeanData2Spec).value4", "cucumber").create();
      """
      Then the result should:
      """
      bean: [{
        class.simpleName= BeanData2
        value3= goodbye
        value4= cucumber
      }]
      """
      When operate:
      """
      jFactory.getDataRepository().clear();
      """
      When build:
      """
      jFactory.spec(BeanSpec.class).property("bean[0](java BeanData2Spec)", new HashMap<>()).create();
      """
      Then the result should:
      """
      bean: [{
        class.simpleName= BeanData2
        value3= goodbye
        value4= java
      }]
      """
      Examples:
        | list         |
        | Object[]     |
        | List<Object> |

    Scenario Outline: query narrow element in list from input spec override parent spec
      Given the following bean class:
      """
      public class Bean {
        public <list> bean;
      }
      """
      Given the following bean class:
      """
      public class BeanData {
        public String value1, value2;
      }
      """
      Given the following bean class:
      """
      public class BeanData2 {
        public String value3, value4;
      }
      """
      Given the following spec class:
      """
      public class BeanSpec extends Spec<Bean> {
        @Override
        public void main() {
          property("bean[]").is("BeanDataSpec");
        }
      }
      """
      Given the following spec class:
      """
      public class BeanDataSpec extends Spec<BeanData> {
        @Override
        public void main() {
          property("value2").value("world");
        }

        @Trait
        public void hello() {
          property("value1").value("hello");
        }
      }
      """
      Given the following spec class:
      """
      public class BeanData2Spec extends Spec<BeanData2> {
        @Override
        public void main() {
          property("value3").value("goodbye");
        }

        @Trait
        public void java() {
          property("value4").value("java");
        }
      }
      """
      When operate:
      """
      Object beanData2 = jFactory.type(BeanData2.class).property("value4", "cucumber").create();
      jFactory.type(Bean.class).property("bean[0]", beanData2).create();
      """
      When build:
      """
      jFactory.spec(BeanSpec.class).property("bean[0](BeanData2Spec).value4", "cucumber").queryAll();
      """
      Then the result should:
      """
      : [{
        bean: [{
          class.simpleName= BeanData2
          value4= cucumber
        }]
      }]
      """
      Examples:
        | list         |
        | Object[]     |
        | List<Object> |

    Scenario: create generic bean
      Given the following bean class:
      """
      public class Bean<T> {
        public T bean;
      }
      """
      Given the following bean class:
      """
      public class BeanData {
        public String value1, value2;
      }
      """
      Given the following spec class:
      """
      @Global
      public class BeanDataSpec extends Spec<BeanData> {
        @Override
        public void main() {
          property("value2").value("world");
        }

        @Trait
        public void hello() {
          property("value1").value("hello");
        }
      }
      """
      Given the following spec class:
      """
      public class BeanSpec extends Spec<Bean<BeanData>> {
        @Override
        public void main() {
          property("bean").is("hello", "BeanDataSpec");
        }
      }
      """
      When build:
      """
      jFactory.spec(BeanSpec.class).create();
      """
      Then the result should:
      """
      : {
        bean : {
          class.simpleName= BeanData
          value1= hello
          value2= world
        }
      }
      """

  Rule: create top list

    Scenario: create from list spec
      Given the following bean class:
        """
        public class Bean { public String value; }
        """
      And the following spec class:
        """
        public class BeanListSpec extends Spec<List<Bean>> { }
        """
      When build:
        """
        jFactory.spec(BeanListSpec.class).property("[0].value", "hello").property("[1].value", "world").create();
        """
      Then the result should:
        """
        value[]: [hello world]
        """
      And operate:
        """
        jFactory.getDataRepository().clear();
        """
      When build:
        """
        jFactory.spec(BeanListSpec.class).property("[1].value", "world").create();
        """
      Then the result should:
        """
        : [null {value= world}]
        """

    Scenario: create from list spec and default element create
      Given the following bean class:
        """
        public class Bean { public String value; }
        """
      Given the following spec class:
        """
        public class BeanSpec extends Spec<Bean> {
          @Trait
          public void Default() { property("value").value("default"); }
        }
        """
      And the following spec class:
        """
        public class BeanListSpec extends Spec<List<Bean>> {
          public void main() { property("[]").is("Default", "BeanSpec"); }
        }
        """
      When build:
        """
        jFactory.spec(BeanListSpec.class).property("[0].value", "hello").property("[1].value", "world").create();
        """
      Then the result should:
        """
        value[]: [hello world]
        """
      And operate:
        """
        jFactory.getDataRepository().clear();
        """
      When build:
        """
        jFactory.spec(BeanListSpec.class).property("[1].value", "world").create();
        """
      Then the result should:
        """
        value[]: [default world]
        """

    Scenario Outline: create from spec list in test
      Given the following bean class:
        """
        public class Bean { public String value; }
        """
      Given the following spec class:
        """
        public class BeanSpec extends Spec<Bean> { }
        """
      When build:
        """
        jFactory.<type>.property("[1].value", "world").create();
        """
      Then the result should:
        """
        : {
          ::this: [{class.simpleName= Bean} {value= world}]
          class.simpleName= <typeName>
        }
        """
      Examples:
        | type                              | typeName  |
        | spec("BeanSpec[]")                | ArrayList |
        | specs(BeanSpec.class)             | Bean[]    |
        | specs(List.class, BeanSpec.class) | ArrayList |

  Rule: create sub list

    Scenario Outline: create sub list from list spec
      Given the following bean class:
        """
        public class ListObject { public <type> list; }
        """
      Given the following bean class:
        """
        public class Bean { public String value; }
        """
      Given the following spec class:
        """
        public class BeanSpec extends Spec<Bean> { }
        """
      And the following spec class:
        """
        public class BeanListSpec extends Spec<List<Bean>> {}
        """
      And operate:
        """
        jFactory.factory(ListObject.class).spec(spec -> spec.property("list").is("BeanListSpec") );
        """
      When build:
        """
        jFactory.type(ListObject.class).property("list(BeanListSpec)[0].value", "hello").property("list[1].value", "world").create();
        """
      Then the result should:
        """
        list.value[]: [hello world]
        """
      And operate:
        """
        jFactory.getDataRepository().clear();
        """
      When build:
        """
        jFactory.type(ListObject.class).property("list(BeanListSpec)[1].value", "world").create();
        """
      Then the result should:
        """
        list: [null {value= world}]
        """
      Examples:
        | type         |
        | Object       |
        | List         |
        | List<Object> |
        | List<Bean>   |

    Scenario Outline: create sub list from list spec and element default create by factory
      Given the following bean class:
        """
        public class ListObject { public <type> list; }
        """
      Given the following bean class:
        """
        public class Bean { public String value; }
        """
      Given the following spec class:
        """
        public class BeanSpec extends Spec<Bean> { }
        """
      And the following spec class:
        """
        public class BeanListSpec extends Spec<List<Bean>> {
          public void main() { property("[]").is("BeanSpec"); }
        }
        """
      And operate:
        """
        jFactory.factory(ListObject.class).spec(spec -> spec.property("list").is("BeanListSpec") );
        """
      When build:
        """
        jFactory.type(ListObject.class).property("list(BeanListSpec)[0].value", "hello").property("list[1].value", "world").create();
        """
      Then the result should:
        """
        list.value[]: [hello world]
        """
      And operate:
        """
        jFactory.getDataRepository().clear();
        """
      When build:
        """
        jFactory.type(ListObject.class).property("list(BeanListSpec)[1].value", "world").create();
        """
      Then the result should:
        """
        list: [{class.simpleName= Bean} {value= world}]
        """
      Examples:
        | type         |
        | Object       |
        | List         |
        | List<Object> |
        | List<Bean>   |

    Scenario Outline: create sub list from spec list in spec
      Given the following bean class:
        """
        public class ListObject { public <type> list; }
        """
      Given the following bean class:
        """
        public class Bean { public String value; }
        """
      Given the following spec class:
        """
        public class BeanSpec extends Spec<Bean> {
          @Trait
          public void Default() { property("value").value("default"); }
        }
        """
      And the following spec class:
        """
        public class ListObjectSpec extends Spec<ListObject> {
          public void main() { property("list[]").is("Default", "BeanSpec"); }
        }
        """
      When build:
        """
        jFactory.spec(ListObjectSpec.class).property("list[0].value", "hello").property("list[1].value", "world").create();
        """
      Then the result should:
        """
        list.value[]: [hello world]
        """
      And operate:
        """
        jFactory.getDataRepository().clear();
        """
      When build:
        """
        jFactory.spec(ListObjectSpec.class).property("list[1].value", "world").create();
        """
      Then the result should:
        """
        list.value[]= [default world]
        """
      Examples:
        | type         |
        | Object       |
        | List         |
        | List<Object> |
        | List<Bean>   |

    Scenario Outline: create sub list from spec list in test
      Given the following bean class:
        """
        public class Bean { public String value; }
        """
      Given the following spec class:
        """
        public class BeanSpec extends Spec<Bean> {
          @Trait
          public void Default() { property("value").value("default"); }
        }
        """
      Given the following bean class:
        """
        public class ListObject { public <type> list; }
        """
      When build:
        """
        jFactory.type(ListObject.class).property("list(BeanSpec[])[1].value", "hello").create();
        """
      Then the result should:
        """
        list: [{class.simpleName= Bean} {value= hello}]
        """
      And operate:
        """
        jFactory.getDataRepository().clear();
        """
      When build:
        """
        jFactory.type(ListObject.class).property("list(Default BeanSpec[])[1].value", "hello").create();
        """
      Then the result should:
        """
        list: [{value= default} {value= hello}]
        """
      Examples:
        | type         |
        | Object       |
        | List         |
        | List<Object> |
        | List<Bean>   |

  Rule: default sub spec

    Scenario: support define default spec (not default value, default value is null)
      Given the following bean class:
      """
      public class Bean {
        public SubBean sub;
      }
      """
      Given the following bean class:
      """
      public class SubBean {
        public String value1, value2;
        public String value;
      }
      """
      And the following spec class:
      """
      public class SubBeanSpec extends Spec<SubBean> {
        @Override
        public void main() {
          property("value1").value("hello");
        }

        @Trait()
        public void Default() {
          property("value2").value("world");
        }
      }
      """
      And register:
      """
        jFactory.factory(Bean.class).spec(spec ->
          spec.property("sub").apply("Default", "SubBeanSpec"));
      """
      When build:
        """
        jFactory.type(Bean.class).property("sub.value", "bye").create();
        """
      Then the result should:
        """
        sub: {
          value1= hello
          value2= world
          value= bye
        }
        """

    Scenario: not apply for collection
      Given the following declarations:
        """
        JFactory jFactory = new JFactory();
        """
      Given the following class definition:
        """
        public class Bean {
          public List sub;
        }
        """
      And the following spec definition:
        """
        public class SubBeanSpec extends Spec<SubBean> {}
        """
      Given the following class definition:
        """
        public class SubBean {
          public String value1, value2;
          public String value;
        }
        """
      And register as follows:
        """
        jFactory.register(SubBeanSpec.class);
        jFactory.factory(Bean.class).spec(spec ->
          spec.property("sub").apply("SubBeanSpec"));
        """
      When evaluating the following code:
        """
        jFactory.type(Bean.class).property("sub[0]", null).create();
        """
      Then the result should be:
        """
        ::throw.message= 'Cannot use `apply` on collection property'
        """

  Rule: create Map

    Scenario: support create a map from build-in spec MAP
      When build:
        """
        jFactory.spec("EMPTY_MAP").create();
        """
      Then the result should:
        """
        : {
          ::this= {}
          ::object.class.name= java.util.HashMap
        }
        """
