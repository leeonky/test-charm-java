Feature: sftp

  Background:
    Given root folder "/tmp/test/sftp/dir1"
    Given root folder "/tmp/test/sftp/dir2"
    Given root folder "/tmp/test/sftp/dir1/sub1"
    Given root folder "/tmp/test/sftp/dir1/sub2"
    Given root folder "/tmp/test/sftp/dir1/sub1/subSub"
    Given ssh server on path "/tmp/test/sftp/":
      | host      | port | user | password |
      | 127.0.0.1 | 2222 | user | password |

  Scenario: root folder as a list
    Then got sftp:
    """
    : [dir1 dir2]
    """

  Scenario: root folder as a map
    Then got sftp:
    """
    dir1: dir1
    """

  Scenario: checking all files in root folder
    When evaluate sftp:
    """
    = {
      dir1: *
    }
    """
    Then error message should be:
    """
    Unexpected fields `dir2`
    """

  Scenario: sub folder as a list
    Then got sftp:
    """
    dir1: [sub1 sub2]
    """

  Scenario: sub folder as a map
    Then got sftp:
    """
    dir1.sub1: sub1
    """

  Scenario: checking all files in sub folder
    When evaluate sftp:
    """
    dir1= {
      sub1: *
    }
    """
    Then error message should be:
    """
    Unexpected fields `sub2` in dir1
    """

  Scenario: checking all files in sub sub folder
    Then got sftp:
    """
    dir1.sub1= {
      subSub: *
    }
    """