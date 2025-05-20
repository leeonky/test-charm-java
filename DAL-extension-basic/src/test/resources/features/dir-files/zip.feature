Feature: zip file

  Background:
    Given root folder "/tmp/work/test/dir"
    Given root folder "/tmp/work/test/tmp"

  Scenario: unzip empty zip is empty folder
    Given an empty zip file "/tmp/work/test/dir/empty.zip"
    Then java.io.File "/tmp/work/test/dir/empty.zip" should:
    """
    unzip: []
    """
    And java.io.File "/tmp/work/test/dir/empty.zip" should:
    """
    unzip= {}
    """

  Scenario: unzip files
    Given a file "/tmp/work/test/tmp/file1.txt"
    """
    hello
    """
    And a file "/tmp/work/test/tmp/file2.txt"
    """
    world
    """
    And a zip file "/tmp/work/test/dir/file.zip":
      | /tmp/work/test/tmp/file1.txt |
      | /tmp/work/test/tmp/file2.txt |
    Then java.io.File "/tmp/work/test/dir/file.zip" should:
    """
    unzip: [ file1.txt  file2.txt ]
    """
    Then java.io.File "/tmp/work/test/dir/file.zip" should:
    """
    unzip: [{
      name: file1.txt
      string: hello
    }{
      name: file2.txt
      string: world
    }]
    """
    Then java.io.File "/tmp/work/test/dir/file.zip" should:
    """
    unzip= {
      'file1.txt': {
        name: file1.txt
        string: hello
      }
      'file2.txt': {
        name: file2.txt
        string: world
      }
    }
    """
    Then java.io.File "/tmp/work/test/dir/file.zip" should:
    """
    unzip: {
      file1.txt: hello
      file2.txt: world
    }
    """

  Scenario: file not exist in zip
    Given an empty zip file "/tmp/work/test/dir/empty.zip"
    Then java.io.File "/tmp/work/test/dir/empty.zip" should failed:
    """
    unzip= {
      'not-exist.txt': {...}
    }
    """
    And error message should be:
    """
    unzip= {
      'not-exist.txt': {...}
      ^
    }

    java.io.FileNotFoundException: File or File Group <not-exist.txt> not found
    """

  Scenario: verify all files in zip
    Given a file "/tmp/work/test/tmp/file.txt"
    """
    hello
    """
    And a zip file "/tmp/work/test/dir/file.zip":
      | /tmp/work/test/tmp/file.txt |
    Then java.io.File "/tmp/work/test/dir/file.zip" should failed:
    """
    unzip= {}
    """
    And error message should be:
    """
    unzip= {}
         ^

    Unexpected fields `file.txt` in unzip
    """

  Scenario: zip has a empty folder
    Given a folder "/tmp/work/test/tmp/empty"
    And a zip file "/tmp/work/test/dir/file.zip":
      | /tmp/work/test/tmp/empty |
    Then java.io.File "/tmp/work/test/dir/file.zip" should:
    """
    unzip: [empty]
    """
    And java.io.File "/tmp/work/test/dir/file.zip" should:
    """
    unzip= {
      empty= []
      empty= {}
    }
    """

  Scenario: one folder with one file
    Given a file "/tmp/work/test/tmp/file.txt"
    """
    hello
    """
    Given a folder "/tmp/work/test/tmp/empty"
    And a zip file "/tmp/work/test/dir/file.zip":
      | /tmp/work/test/tmp/file.txt |
      | /tmp/work/test/tmp/empty    |
    Then java.io.File "/tmp/work/test/dir/file.zip" should:
    """
    unzip: [empty file.txt]
    """
    And java.io.File "/tmp/work/test/dir/file.zip" should:
    """
    unzip= {
      empty= []
      empty= {}
      file.txt: hello
    }
    """

  Scenario: folder with file
    Given a folder "/tmp/work/test/tmp/folder"
    Given a file "/tmp/work/test/tmp/folder/file.txt"
    """
    hello
    """
    And a zip file "/tmp/work/test/dir/file.zip":
      | /tmp/work/test/tmp/folder |
    Then java.io.File "/tmp/work/test/dir/file.zip" should:
    """
    unzip: [folder],
    unzip: {
      folder: [file.txt]
      folder: {
        file.txt: hello
      }
    }
    """

  Scenario: folder with folder has a file
    Given a folder "/tmp/work/test/tmp/folder"
    Given a folder "/tmp/work/test/tmp/folder/child"
    Given a file "/tmp/work/test/tmp/folder/child/file.txt"
    """
    hello
    """
    And a zip file "/tmp/work/test/dir/file.zip":
      | /tmp/work/test/tmp/folder |
    Then java.io.File "/tmp/work/test/dir/file.zip" should:
    """
    unzip: [folder],
    unzip: {
      folder/child= {
        file.txt: hello
      }
    }
    """

  Scenario: raise error when extension not exist in group
    Given a folder "/tmp/work/test/tmp/folder"
    Given a file "/tmp/work/test/tmp/folder/file.txt"
    """
    hello
    """
    Given a file "/tmp/work/test/tmp/file.txt"
    """
    hello
    """
    And a zip file "/tmp/work/test/dir/file.zip":
      | /tmp/work/test/tmp/file.txt |
      | /tmp/work/test/tmp/folder   |
    Then java.io.File "/tmp/work/test/dir/file.zip" should failed:
    """
    unzip: {
      file.json: txt
    }
    """
    And error message should be:
    """
    unzip: {
      file.json: txt
           ^
    }

    java.io.FileNotFoundException: File `file.json` not exist
    """
    Then java.io.File "/tmp/work/test/dir/file.zip" should failed:
    """
    unzip: {
      folder/file.json: txt
    }
    """
    And error message should be:
    """
    unzip: {
      folder/file.json: txt
                  ^
    }

    java.io.FileNotFoundException: File `file.json` not exist
    """

  Scenario: nested zip files
    Given a folder "/tmp/work/test/tmp/folder"
    Given a folder "/tmp/work/test/tmp/zip1"
    Given a file "/tmp/work/test/tmp/zip1/file1.txt"
    """
    hello
    """
    Given a file "/tmp/work/test/tmp/zip1/file2.txt"
    """
    world
    """
    And a zip file "/tmp/work/test/tmp/folder/zip1.zip":
      | /tmp/work/test/tmp/zip1 |
    Given a file "/tmp/work/test/tmp/folder/foo.txt"
    """
    foo
    """
    Given a file "/tmp/work/test/tmp/bar.txt"
    """
    bar
    """
    And a zip file "/tmp/work/test/dir/zip.zip":
      | /tmp/work/test/tmp/folder  |
      | /tmp/work/test/tmp/bar.txt |

    Then java.io.File "/tmp/work/test/dir" should:
    """
    : {
      zip.zip: [
        bar.txt
        folder
      ]
    }
    """
    Then java.io.File "/tmp/work/test/dir" should:
    """
    = {
      zip.zip: {
        bar.txt: bar
        folder= {
          foo.txt= foo
          zip1.zip: {
            zip1: {
            file1.txt: hello
            file2.txt: world
            }
          }
        }
      }
    }
    """
    Then java.io.File "/tmp/work/test/dir" should:
    """
    zip.zip= {
        bar.txt= bar
        folder/foo.txt= foo
        folder/zip1.zip/zip1/file1.txt= hello
        folder/zip1.zip/zip1/file2.txt= world
    }
    """

  Rule: dump

    Scenario: inspect root empty zip
      Given an empty zip file "/tmp/work/test/dir/empty.zip"
      Then zip file "/tmp/work/test/dir/empty.zip" should dump:
      """
      zip archive
      """

    Scenario: inspect root zip files
      Given a file "/tmp/work/test/tmp/file1.txt"
      """
      hello
      """
      And set file attribute "/tmp/work/test/tmp/file1.txt"
      """
      rwxr-xr-x wheel leeonky 2000-01-02T00:00:00Z
      """
      Given a file "/tmp/work/test/tmp/file2.txt"
      """
      world
      """
      And set file attribute "/tmp/work/test/tmp/file2.txt"
      """
      rwxr-xr-x wheel leeonky 2000-01-02T00:00:10Z
      """
      And a zip file "/tmp/work/test/dir/file.zip":
        | /tmp/work/test/tmp/file1.txt |
        | /tmp/work/test/tmp/file2.txt |
      Then zip file "/tmp/work/test/dir/file.zip" should dump:
      """
      zip archive
      2000-01-02T00:00:00Z      5 file1.txt
      2000-01-02T00:00:10Z      5 file2.txt
      """

    Scenario: inspect zip with folder with file
      Given a folder "/tmp/work/test/tmp/dir"
      Given a file "/tmp/work/test/tmp/dir/file1.txt"
      """
      hello
      """
      And set file attribute "/tmp/work/test/tmp/dir/file1.txt"
      """
      rwxr-xr-x wheel leeonky 2000-01-02T00:00:10Z
      """
      And a zip file "/tmp/work/test/dir/file.zip":
        | /tmp/work/test/tmp/dir |
      Then zip file "/tmp/work/test/dir/file.zip" should dump:
      """
      zip archive
      dir/
          2000-01-02T00:00:10Z      5 file1.txt
      """

    Scenario: inspect zip with empty nested zip
      Given an empty zip file "/tmp/work/test/dir/empty.zip"
      And a zip file "/tmp/work/test/dir/file.zip":
        | /tmp/work/test/dir/empty.zip |
      Then zip file "/tmp/work/test/dir/file.zip" should dump:
      """
      zip archive
      empty.zip
      """

    Scenario: inspect zip with nested zip with files
      Given a file "/tmp/work/test/tmp/file1.txt"
      """
      hello
      """
      And set file attribute "/tmp/work/test/tmp/file1.txt"
      """
      rwxr-xr-x wheel leeonky 2000-01-02T00:00:00Z
      """
      Given a file "/tmp/work/test/tmp/file2.txt"
      """
      world
      """
      And set file attribute "/tmp/work/test/tmp/file2.txt"
      """
      rwxr-xr-x wheel leeonky 2000-01-02T00:00:10Z
      """
      And a zip file "/tmp/work/test/dir/zip.zip":
        | /tmp/work/test/tmp/file1.txt |
        | /tmp/work/test/tmp/file2.txt |
      And a zip file "/tmp/work/test/dir/file.zip":
        | /tmp/work/test/dir/zip.zip |
      Then zip file "/tmp/work/test/dir/file.zip" should dump:
      """
      zip archive
      zip.zip
          2000-01-02T00:00:00Z      5 file1.txt
          2000-01-02T00:00:10Z      5 file2.txt
      """

    Scenario: inspect zip with nested zip with folder with files
      Given a folder "/tmp/work/test/tmp/dir"
      Given a file "/tmp/work/test/tmp/dir/file1.txt"
      """
      hello
      """
      And set file attribute "/tmp/work/test/tmp/dir/file1.txt"
      """
      rwxr-xr-x wheel leeonky 2000-01-02T00:00:10Z
      """
      Given a file "/tmp/work/test/tmp/dir/file2.txt"
      """
      world
      """
      And set file attribute "/tmp/work/test/tmp/dir/file2.txt"
      """
      rwxr-xr-x wheel leeonky 2000-01-02T00:00:20Z
      """
      And a zip file "/tmp/work/test/dir/zip.zip":
        | /tmp/work/test/tmp/dir |
      And a zip file "/tmp/work/test/dir/file.zip":
        | /tmp/work/test/dir/zip.zip |
      Then zip file "/tmp/work/test/dir/file.zip" should dump:
      """
      zip archive
      zip.zip
          dir/
              2000-01-02T00:00:10Z      5 file1.txt
              2000-01-02T00:00:20Z      5 file2.txt
      """
