Feature: clause

  Scenario: no parameter
    Given clause
    """
    no_arg = 1
    """
    Then clause should be:
    """
    : {
      clause= 'no_arg = 1'
      parameters= []
    }
    """

  Scenario: only one parameter
    Given clause
    """
    :pid
    """
    Then clause should be:
    """
    : {
      clause= '?'
      parameters= [pid]
    }
    """

  Scenario: one tail parameter with other statement
    Given clause
    """
    id=:pid
    """
    Then clause should be:
    """
    : {
      clause= 'id=?'
      parameters= [pid]
    }
    """

  Scenario: one head parameter with other statement
    Given clause
    """
    :pid=id
    """
    Then clause should be:
    """
    : {
      clause= '?=id'
      parameters= [pid]
    }
    """

  Scenario: more than one parameters
    Given clause
    """
    :pid=:tid or :pid = 1
    """
    Then clause should be:
    """
    : {
      clause= '?=? or ? = 1'
      parameters= [pid tid pid]
    }
    """

  Scenario Outline: valid char in parameter
    Given clause
    """
    :abc<char>
    """
    Then clause should be:
    """
    : {
      clause= '?'
      parameters= [abc<char>]
    }
    """
    Examples:
      | char |
      | a    |
      | 1    |
      | _    |


