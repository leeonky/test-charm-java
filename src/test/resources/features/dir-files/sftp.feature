Feature: sftp

  Background:
    Given root folder "/tmp/test/dir"
    Given root folder "/tmp/test/dir2"
    Given ssh server:
      | host      | port | user | password |
      | 127.0.0.1 | 2222 | user | password |

  Scenario: root folder as a list
    Then got sftp:
    """
    : [... mnt var tmp ...]
    """

  Scenario: root folder as a map
    Then got sftp:
    """
    tmp: tmp
    """

  Scenario: checking all files in root folder
    When evaluate sftp:
    """
    = {
      tmp: *
    }
    """
    Then error message should be:
    """
    Unexpected fields `media`, `mnt`, `var`, `dev`, `srv`, `usr`, `package`, `bin`, `command`, `opt`, `sys`, `etc`, `home`, `sbin`, `root`, `proc`, `run`, `init`, `lib`, `app`, `defaults`, `.dockerenv`, `keygen.sh`, `docker-mods`, `config`
    """

  Scenario: sub folder as a list
    Then got sftp:
    """
    tmp: [... test ...]
    """

  Scenario: sub folder as a map
    Then got sftp:
    """
    tmp.test: test
    """

  Scenario: checking all files in sub folder
    When evaluate sftp:
    """
    tmp.test= {
      dir: *
    }
    """
    Then error message should be:
    """
    Unexpected fields `dir2`, `tmp` in tmp.test
    """
