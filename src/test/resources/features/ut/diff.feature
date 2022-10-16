Feature: diff

  Scenario: no diff when both side is empty
    Given the left side:
    """
    """
    Given the right side:
    """
    """
    Then the diff should be:
    """
    """

  Scenario: no diff when both side have no new line
    Given the left side:
    """
    hello
    """
    Given the right side:
    """
    world
    """
    Then the diff should be:
    """
    """

  Scenario: same width on both side
    Given the left side:
    """
    1234567
    7654321
    """
    Given the right side:
    """
    1234567
    *654321
    """
    Then the diff should be:
    """
    Diff:
    Expect: | Actual:
    --------|--------
    1234567 | 1234567
    7654321 | *654321
    ^       | ^
    """

  Scenario: less than title
    Given the left side:
    """
    123456
    654321
    """
    Given the right side:
    """
    123456
    *54321
    """
    Then the diff should be:
    """
    Diff:
    Expect: | Actual:
    --------|--------
    123456  | 123456
    654321  | *54321
    ^       | ^
    """

  Scenario: more than title
    Given the left side:
    """
    12345678
    87654321
    """
    Given the right side:
    """
    12345678
    *7654321
    """
    Then the diff should be:
    """
    Diff:
    Expect:  | Actual:
    ---------|---------
    12345678 | 12345678
    87654321 | *7654321
    ^        | ^
    """
