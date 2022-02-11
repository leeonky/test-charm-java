Feature: RESTful api steps

  Background:
    Given base url "http://www.a.com"

  Scenario Outline: get with no params
    When <method> "/index"
    Then "http://www.a.com" got a "<method>" request on "/index"
    Examples:
      | method |
      | GET    |
      | DELETE |

  Scenario Outline: get with header
    Given header by RESTful api:
    """
    {
      "key1": "value1",
      "key2": ["value2", "value3"]
    }
    """
    When <method> "/index"
    Then got request:
    """
    : [{
      method: '<method>'
      path: '/index'
      headers: {
        key1: ['value1']
        key2: ['value2', 'value3']
      }
    }]
    """
    And "http://www.a.com" got a "<method>" request on "/index"
    Examples:
      | method |
      | GET    |
      | DELETE |

  Scenario Outline: get response
    Given response 200 on "<method>" "/index":
    """
    Hello world
    """
    When <method> "/index"
    Then response should be:
    """
    : {
      code=200
      body.string='Hello world'
      raw.class.simpleName='Response'
    }
    """
    Examples:
      | method |
      | GET    |
      | DELETE |

#  TODO header for POST PUT DELETE