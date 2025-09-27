Feature: basic

  Background:
    Given declaration jFactory =
    """
    new JFactory();
    """

  Rule: one property

    Background:

      Given the following bean class:
        """
        public class Bean {
          public String str1, str2, str3, str4;
        }
        """

    Scenario: link 2 properties with the same type directly
      And the following spec class:
        """
        public class ABean extends Spec<Bean> {
          public void main() {
            link("str1", "str2");
          }
        }
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).property("str1", "hello").create();
        """
      Then the result should:
        """
        <<str1,str2>>= hello
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).property("str2", "hello").create();
        """
      Then the result should:
        """
        <<str1,str2>>= hello
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).create();
        """
      Then the result should:
        """
        <<str1,str2>>= /^str1.*/
        """

    Scenario: consistency in different type
      Given the following bean class:
        """
        public class Bean {
          public String str;
          public int i;
        }
        """
      And the following spec class:
        """
        public class ABean extends Spec<Bean> {
          public void main() {
            consistent(String.class)
              .direct("str")
              .property("i").read(Object::toString).write(Integer::parseInt);
          }
        }
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).property("str", "100").create();
        """
      Then the result should:
        """
        : {
          str: '100'
          i: 100
        }
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).property("i", 50).create();
        """
      Then the result should:
        """
        : {
          str: '50'
          i: 50
        }
        """

    Scenario: allow same property for different composer in different consistency
      Given the following bean class:
        """
        public class Bean {
          public String str1, str2, str3;
        }
        """
      And the following spec class:
        """
        public class ABean extends Spec<Bean> {
          public void main() {
            consistent(String.class)
              .<String>property("str1")
                .read(s->s)
              .direct("str2");

            consistent(String.class)
              .<String>property("str1")
                .read(s->s+"!")
              .direct("str3");
          }
        }
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).property("str1", "hello").create();
        """
      Then the result should:
        """
        str1= hello, str2= hello, str3= hello!
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).property("str2", "hello").create();
        """
      Then the result should:
        """
        str1= /^str1.*/, str2= hello, str3= /^str1.*!/
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).property("str3", "hello").create();
        """
      Then the result should:
        """
        str1= /^str1.*/, str2= /^str1.*/, str3= hello
        """

  Rule: multiple properties

    Scenario: two properties consistency
      Given the following bean class:
        """
        public class Person {
          public String fullName, firstName, lastName, familyName, givenName;
        }
        """
      And the following spec class:
        """
        public class APerson extends Spec<Person> {
          public void main() {
            consistent(String.class)
              .direct("fullName")
              .properties("firstName", "lastName")
                .read((first,last) -> first+" "+last).write(s -> s.split(" "))
              .properties("familyName", "givenName")
                .read(names -> names[0]+" "+names[1]).write(s->s.split(" ")[0], s->s.split(" ")[1]);
          }
        }
        """
      When build:
        """
        jFactory.clear().spec(APerson.class).property("fullName", "James Anderson").create();
        """
      Then the result should:
        """
        : {
          fullName: 'James Anderson'
          firstName: James
          lastName: Anderson
          familyName: James
          givenName: Anderson
        }
        """
      When build:
        """
        jFactory.clear().spec(APerson.class).property("firstName", "James").property("lastName", "Anderson").create();
        """
      Then the result should:
        """
        : {
          fullName: 'James Anderson'
          firstName: James
          lastName: Anderson
          familyName: James
          givenName: Anderson
        }
        """
      When build:
        """
        jFactory.clear().spec(APerson.class).property("firstName", "James").property("givenName", "Anderson").create();
        """
      Then the result should:
        """
        : {
          fullName: /^James lastName.*/
          firstName: James
          lastName: /^lastName.*/
          familyName: James
          givenName: Anderson
        }
        """

    Scenario: three properties consistency
      Given the following bean class:
        """
        public class Bean {
          public String str1, str2, str3, str4, str5, str6;
        }
        """
      And the following spec class:
        """
        public class ABean extends Spec<Bean> {
            public void main() {
                consistent(String.class)
                .<String, String, String>properties("str1", "str2", "str3")
                    .read((s1,s2,s3)->s1+s2+s3)
                    .write(s->s.substring(0,1), s->s.substring(1,2), s->s.substring(2,3))
                .<String, String, String>properties("str4", "str5", "str6")
                    .read((s1,s2,s3)->s1+s2+s3)
                    .write(s->s.substring(0,1), s->s.substring(1,2), s->s.substring(2,3));
            }
        }
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).create();
        """
      Then the result should:
        """
        : {
          str1: /^str1.*/
          str2: /^str2.*/
          str3: /^str3.*/
          str4: s
          str5: t
          str6: r
        }
        """
      When build:
        """
        jFactory.clear().spec(ABean.class).property("str1", "x").create();
        """
      Then the result should:
        """
        : {
          str1: x
          str2: /^str2.*/
          str3: /^str3.*/
          str4: x
          str5: s
          str6: t
        }
        """

    Scenario: all composer and decomposer should computer only once
      Given the following bean class:
        """
        public class Bean {
          public String str1, str2, str3, str4;
        }
        """
      And the following spec class:
        """
        public class APerson extends Spec<Bean> {
          int w=0, r=10;
          public void main() {
            consistent(String.class)
              .property("str3")
                .read(s->{r++; return r+"_"+s;})
              .properties("str1", "str2")
                .write(s -> { w++; return new String[]{w+"_"+s,w+"_"+s}; });
          }
        }
        """
      When build:
        """
        jFactory.clear().spec(APerson.class).create();
        """
      Then the result should:
        """
        <<str1,str2>>= /^1_11_str3.*/, str3= /^str3.*/
        """

    Scenario: properties reader writer overlap
      Given the following bean class:
        """
        public class Order {
          public int quantity;
          public BigDecimal unitPrice, total, unitDiscount, discountTotal;
        }
        """
      And the following spec class:
        """
        public class OrderSpec extends Spec<Order> {
            public void main() {
              consistent(BigDecimal.class)
                .<Integer, BigDecimal>properties("quantity", "unitPrice")
                  .write(total -> new Object[]{2, total.divide(BigDecimal.valueOf(2))})
                .direct("total");

              consistent(BigDecimal.class)
                .<Integer, BigDecimal>properties("quantity", "unitDiscount")
                  .read((qty, discount) -> discount.multiply(BigDecimal.valueOf(qty)))
                .direct("discountTotal");
            }
        }
        """
      When build:
        """
        jFactory.clear().spec(OrderSpec.class).property("total", 100).create();
        """
      Then the result should:
        """
        : {
          quantity: 2
          unitPrice: 50
          total: 100
          unitDiscount: 1
          discountTotal: 2
        }
        """
      When build:
        """
        jFactory.clear().spec(OrderSpec.class).property("quantity", 50).create();
        """
      Then the result should:
        """
        : {
          quantity: 50
          unitPrice: 1
          total: 2
          unitDiscount: 2
          discountTotal: 100
        }
        """
      When build:
        """
        jFactory.clear().spec(OrderSpec.class).property("unitPrice", 50).create();
        """
      Then the result should:
        """
        : {
          quantity: 2
          unitPrice: 50
          total: 3
          unitDiscount: 3
          discountTotal: 6
        }
        """
      When build:
        """
        jFactory.clear().spec(OrderSpec.class).property("discountTotal", 50).create();
        """
      Then the result should:
        """
        : {
          quantity: 2
          unitPrice: 2
          total:  4
          unitDiscount: 4
          discountTotal: 50
        }
        """
      When build:
        """
        jFactory.clear().spec(OrderSpec.class).property("unitDiscount", 10).create();
        """
      Then the result should:
        """
        : {
          quantity: 2
          unitPrice: 2.5
          total:  5
          unitDiscount: 10
          discountTotal: 20
        }
        """
