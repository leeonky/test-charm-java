Feature: sftp

  Background:
    Given root folder "/tmp/work/test/sftp/dir1"
    Given root folder "/tmp/work/test/sftp/dir2"
    Given root folder "/tmp/work/test/sftp/dir1/sub1"
    Given root folder "/tmp/work/test/sftp/dir1/sub2"
    Given root folder "/tmp/work/test/sftp/dir1/sub1/subSub"
    Given ssh server on path "/tmp/work/test/sftp/":
      | host      | port | user | password |
      | 127.0.0.1 | 2222 | user | password |

  Scenario: root folder as a list
    Then got sftp:
    """
    : [... dir1 dir2 ...]
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
    dir1: [... sub1 sub2 ...]
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

  Scenario: file content on root
    Given a file "/tmp/work/test/sftp/file.txt"
    """
    hello-world
    """
    Then got sftp:
    """
    ['file.txt'].string: 'hello-world'
    """

  Scenario: file content in folder
    Given a file "/tmp/work/test/sftp/dir1/file.txt"
    """
    hello-world
    """
    Then got sftp:
    """
    dir1['file.txt'].string: 'hello-world'
    """

  Scenario: file content in folder
    Given a file "/tmp/work/test/sftp/dir1/file.txt"
    """
    hello-world
    """
    Then got sftp:
    """
    : {
      dir1['file.txt'].string: 'hello-world'
      dir1/file.txt: 'hello-world'
    }
    """

  Rule: dump

    @ci-skip
    Scenario: dump dir
      And set file attribute "/tmp/work/test/dir"
      """
      rwxr-xr-x wheel leeonky 2022-10-09T06:47:01Z
      """
      Then sftp "/tmp/work/" should dump:
      """
      sftp {}
      """

    @ci-skip
    Scenario: dump file
      Given a file "/tmp/work/test/dir/file1.txt"
      """
      hello1
      """
      And set file attribute "/tmp/work/test/dir/file1.txt"
      """
      rwxr-xr-x wheel leeonky 2022-10-09T06:47:01Z
      """
#      TODO root is a file
      Then sftp "/tmp/work/test/dir/file1.txt" should dump:
      """
      java.io.File rwxr-xr-x wheel leeonky 2022-10-09T06:47:01Z 6
      """
