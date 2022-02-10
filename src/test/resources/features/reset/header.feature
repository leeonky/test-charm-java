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
    When GET "/index"
    Then got request:
    """
    : [{
      method: 'GET'
      path: '/index'
      headers: {
        key1: null
        key2: null
      }
    }]
    """

#  TODO reset RESTfulStep response
