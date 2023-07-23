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

  Scenario: given header
    Given header by RESTful api:
    """
    {
      "key1": "value1",
      "key2": ["value2", "value3"]
    }
    """

  Scenario: should no header
    When DELETE "/index"
    Then got request:
    """
    : [{
      method: 'DELETE'
      path: '/index'
      headers: {
        key1: null
        key2: null
      }
    }]
    """

  Scenario: given header
    Given header by RESTful api:
    """
    {
      "key1": "value1",
      "key2": ["value2", "value3"]
    }
    """

  Scenario: should no header
    When POST "/index":
    """
    {}
    """
    Then got request:
    """
    : [{
      method: 'POST'
      path: '/index'
      headers: {
        key1: null
        key2: null
      }
    }]
    """

  Scenario: given header
    Given header by RESTful api:
    """
    {
      "key1": "value1",
      "key2": ["value2", "value3"]
    }
    """

  Scenario: should no header
    When PUT "/index":
    """
    {}
    """
    Then got request:
    """
    : [{
      method: 'PUT'
      path: '/index'
      headers: {
        key1: null
        key2: null
      }
    }]
    """

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
    Given response 200 on "GET" "/index":
    """
    Hello world
    """
    When GET "/index"

  Scenario: should no response
    Then response should be:
    """
    : null
    """

  Scenario: given response
    Given response 200 on "DELETE" "/index":
    """
    Hello world
    """
    When DELETE "/index"

  Scenario: should no response
    Then response should be:
    """
    : null
    """

  Scenario: given response
    Given response 200 on "POST" "/index":
    """
    Hello world
    """
    When POST "/index":
    """
    {}
    """

  Scenario: should no response
    Then response should be:
    """
    : null
    """

  Scenario: given response
    Given response 200 on "PUT" "/index":
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

