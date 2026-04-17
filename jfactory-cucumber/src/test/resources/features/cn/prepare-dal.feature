# language: zh-CN
功能: 用DAL准备数据

  场景: 准备商品-DAL格式
    假如存在"商品"：
    """
    name: 'book'.toUpperCase
    color: red
    """
    那么所有"商品"应为：
    """
    :  | name | color |
       | BOOK | red   |
    """

  场景: 准备多个商品-DAL格式
    假如存在"商品"：
    """
    | name | color |
    | book | red   |
    | iPad | white |
    """
    那么所有"商品"应为：
    """
    :  | name | color |
       | book | red   |
       | iPad | white |
    """

  场景: 准备商品-all in one: single object
    假如存在：
    """
    商品: {
      name: 'book'.toUpperCase
      color: red
    }
    """
    那么所有"商品"应为：
    """
    :  | name | color |
       | BOOK | red   |
    """

  场景: 准备商品-all in one: list
    假如存在：
    """
    商品: | name | color |
          | BOOK | red   |
    """
    那么所有"商品"应为：
    """
    :  | name | color |
       | BOOK | red   |
    """

  场景: 准备商品-all in one with out properties
    假如存在：
    """
    商品
    商品
    商品
    """
    那么所有"商品"应为：
    """
    ::size= 3
    """

  场景: 为已有数据添加一对一关联数据
    假如存在"订单"：
      | customer |
      | Tom      |
    并且存在"订单.customer[Tom].product"的"商品"：
      """
      name: bicycle
      """
    那么"订单"应为：
      """
      .customer='Tom'
      and .product.name='bicycle'
      """

  场景: 为已有数据添加反向一对多关联数据
    假如存在"商品"：
      | name |
      | book |
    并且存在如下"库存"，并且其"product"为"商品.name[book]"：
      """
      size: A3
      count: 10
      """
    那么"商品"应为：
    """
      .stocks.size=1
      and .stocks[0].size='A3'
      and .stocks[0].count=10
    """
