Feature: sftp

  Background:
    Given root folder "/tmp/work/test/sftp"
    Given ssh server on path "/tmp/work/test/sftp/":
      | host      | port | user | password |
      | 127.0.0.1 | 2222 | user | password |

  Scenario: root folder as a list
    Given root folder "/tmp/work/test/sftp/dir1"
    Given root folder "/tmp/work/test/sftp/dir2"
    Then got sftp:
    """
    : [... dir1 dir2 ...]
    """

  Scenario: root folder as a map
    Given root folder "/tmp/work/test/sftp/dir1"
    Then got sftp:
    """
    dir1: dir1
    """

  Scenario: checking all files in root folder
    Given root folder "/tmp/work/test/sftp/dir1"
    Given root folder "/tmp/work/test/sftp/dir2"
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
    Given root folder "/tmp/work/test/sftp/dir1/sub1"
    Given root folder "/tmp/work/test/sftp/dir1/sub2"
    Then got sftp:
    """
    dir1: [... sub1 sub2 ...]
    """

  Scenario: sub folder as a map
    Given root folder "/tmp/work/test/sftp/dir1/sub1"
    Then got sftp:
    """
    dir1.sub1: sub1
    """

  Scenario: checking all files in sub folder
    Given root folder "/tmp/work/test/sftp/dir1/sub1"
    Given root folder "/tmp/work/test/sftp/dir1/sub2"
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
    Given root folder "/tmp/work/test/sftp/dir1/sub1/subSub"
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
    And ssh server on path "/tmp/work/test/sftp/file.txt":
      | host      | port | user | password |
      | 127.0.0.1 | 2222 | user | password |
    Then got sftp:
    """
    string: 'hello-world'
    """

  Scenario: file content in folder
    Given root folder "/tmp/work/test/sftp/dir1"
    Given a file "/tmp/work/test/sftp/dir1/file.txt"
    """
    hello-world
    """
    Then got sftp:
    """
    dir1['file.txt'].string: 'hello-world'
    """

  Scenario: file content in folder
    Given root folder "/tmp/work/test/sftp/dir1"
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
    Scenario: dump root empty dir
      Then sftp "/tmp/work/test/sftp/" should dump:
      """
      sftp dir /tmp/work/test/sftp/
      """

    @ci-skip
    Scenario: dump root dir with one file
      Given a file "/tmp/work/test/sftp/file1.txt"
      """
      helloWorld
      """
      And set file attribute "/tmp/work/test/sftp/file1.txt"
      """
      rwxr-xr-x wheel leeonky 2022-10-09T06:47:01Z
      """
      Then sftp "/tmp/work/test/sftp/" should dump:
      """
      sftp dir /tmp/work/test/sftp/
      -rwxr-xr-x user user   10 2022-10-09T06:47:01Z file1.txt
      """

    @ci-skip
    Scenario: dump root file
      Given a file "/tmp/work/test/sftp/file1.txt"
      """
      helloWorld
      """
      And set file attribute "/tmp/work/test/sftp/file1.txt"
      """
      rwxr-xr-x wheel leeonky 2022-10-09T06:47:01Z
      """
      Then sftp "/tmp/work/test/sftp/file1.txt" should dump:
      """
      -rwxr-xr-x user user   10 2022-10-09T06:47:01Z file1.txt
      """

    @ci-skip
    Scenario: dump folder
      Given root folder "/tmp/work/test/sftp/dir"
      Given a file "/tmp/work/test/sftp/dir/file1.txt"
      """
      1
      """
      And set file attribute "/tmp/work/test/sftp/dir/file1.txt"
      """
      rwxr-xr-x wheel leeonky 2022-10-09T06:47:01Z
      """
      Then sftp "/tmp/work/test/sftp/" should dump:
      """
      sftp dir /tmp/work/test/sftp/
      dir/
          -rwxr-xr-x user user    1 2022-10-09T06:47:01Z file1.txt
      """
