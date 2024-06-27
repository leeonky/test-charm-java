Feature: reset RESTful states

  Background:
    Given base url "http://www.a.com"

  Scenario: given header
    Given header by RESTful api:
    """
    {
      "key1": "value1",
      "key2": ["value2", "value3"]
    }
    """

  Scenario: should no header
    When PATCH "/index":
    """
    {}
    """
    Then got request:
    """
    : [{
      method: 'PATCH'
      path: '/index'
      headers: {
        key1: null
        key2: null
      }
    }]
    """

  Scenario: given response
    Given response 200 on "PATCH" "/index":
    """
    Hello world
    """
    When PUT "/index":
    """
    {}
    """

  Scenario: should no response
    Then response should be:
    """
    : null
    """

