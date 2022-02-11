Feature: RESTful api steps

  Background:
    Given base url "http://www.a.com"

  Scenario Outline: <method> with no params
    When <method> "/index"
    Then "http://www.a.com" got a "<method>" request on "/index"
    Examples:
      | method |
      | GET    |
      | DELETE |

  Scenario Outline: <method> with body and no params
    When <method> "/index":
    """
    { "text": "Hello world" }
    """
    Then "http://www.a.com" got a "<method>" request on "/index" with body
    """
    { "text": "Hello world" }
    """
    Examples:
      | method |
      | POST   |
      | PUT    |

  Scenario Outline: <method> with header
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

  Scenario Outline: <method> with body and header
    Given header by RESTful api:
    """
    {
      "key1": "value1",
      "key2": ["value2", "value3"]
    }
    """
    When <method> "/index":
    """
    { "text": "Hello world" }
    """
    Then got request:
    """
    : [{
      method: '<method>'
      path: '/index'
      headers: {
        ['Content-Type']: ['application/json']
        key1: ['value1']
        key2: ['value2', 'value3']
      }
    }]
    """
    And "http://www.a.com" got a "<method>" request on "/index"
    Examples:
      | method |
      | POST   |
      | PUT    |

  Scenario Outline: <method> with content type
    When <method> "/index":
    """text/html
    { "text": "Hello world" }
    """
    Then got request:
    """
    : [{
      headers['Content-Type']: ['text/html']
    }]
    """
    Examples:
      | method |
      | POST   |
      | PUT    |

  Scenario Outline: <method> response
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

  Scenario Outline: <method> with body and response
    Given response 200 on "<method>" "/index":
    """
    Hello world
    """
    When <method> "/index":
    """
    { "text": "Hello world" }
    """
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
      | POST   |
      | PUT    |
