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

    Get property `not-exist.txt` failed, property can be:
      1. public field
      2. public getter
      3. public no args method
      4. Map key value
      5. customized type getter
      6. static method extension
    java.io.FileNotFoundException: File or File Group <not-exist.txt> not found
    Implicit list mapping is not allowed in current version of DAL, use `not-exist.txt[]` instead
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

    Get property `json` failed, property can be:
      1. public field
      2. public getter
      3. public no args method
      4. Map key value
      5. customized type getter
      6. static method extension
    java.io.FileNotFoundException: File `file.json` not exist
    Implicit list mapping is not allowed in current version of DAL, use `json[]` instead
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

    Get property `json` failed, property can be:
      1. public field
      2. public getter
      3. public no args method
      4. Map key value
      5. customized type getter
      6. static method extension
    java.io.FileNotFoundException: File `file.json` not exist
    Implicit list mapping is not allowed in current version of DAL, use `json[]` instead
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

    Scenario: dump empty dir
      Given an empty zip file "/tmp/work/test/dir/empty.zip"
      Then zip file "/tmp/work/test/dir/empty.zip" should dump:
      """
      zip archive
      """

    Scenario: dump zip file
      Given a file "/tmp/work/test/tmp/file1.txt"
      """
      hello
      """
      And set file attribute "/tmp/work/test/tmp/file1.txt"
      """
      rwxr-xr-x wheel leeonky 2022-10-09T06:47:01Z
      """
      And a zip file "/tmp/work/test/dir/file.zip":
        | /tmp/work/test/tmp/file1.txt |
      Then zip file "/tmp/work/test/dir/file.zip" should dump:
      """
      zip archive
      2022-10-09T06:47:00Z      5 file1.txt
      """

    Scenario: dump zip empty folder
      Given a folder "/tmp/work/test/tmp/empty"
      And a zip file "/tmp/work/test/dir/file.zip":
        | /tmp/work/test/tmp/empty |
      Then zip file "/tmp/work/test/dir/file.zip" should dump:
      """
      zip archive
      empty/
      """

    Scenario: dump zip with folder with file
      Given a folder "/tmp/work/test/tmp/dir"
      Given a file "/tmp/work/test/tmp/dir/file1.txt"
      """
      hello
      """
      And set file attribute "/tmp/work/test/tmp/dir/file1.txt"
      """
      rwxr-xr-x wheel leeonky 2022-10-09T06:47:01Z
      """
      And a zip file "/tmp/work/test/dir/file.zip":
        | /tmp/work/test/tmp/dir |

      Then zip file "/tmp/work/test/dir/file.zip" should dump:
      """
      zip archive
      dir/
          2022-10-09T06:47:00Z      5 file1.txt
      """

    Scenario: dump nested zip files
      Given a folder "/tmp/work/test/tmp/folder"
      Given a folder "/tmp/work/test/tmp/zip1"
      Given a file "/tmp/work/test/tmp/zip1/file1.txt"
      """
      hello
      """
      And set file attribute "/tmp/work/test/tmp/zip1/file1.txt"
      """
      rwxr-xr-x wheel leeonky 2022-10-09T06:47:30Z
      """
      Given a file "/tmp/work/test/tmp/zip1/file2.txt"
      """
      world
      """
      And set file attribute "/tmp/work/test/tmp/zip1/file2.txt"
      """
      rwxr-xr-x wheel leeonky 2022-10-09T06:47:02Z
      """
      And a zip file "/tmp/work/test/tmp/folder/zip1.zip":
        | /tmp/work/test/tmp/zip1 |
      Given a file "/tmp/work/test/tmp/folder/foo.txt"
      """
      foo
      """
      And set file attribute "/tmp/work/test/tmp/folder/foo.txt"
      """
      rwxr-xr-x wheel leeonky 2022-10-09T06:47:04Z
      """
      Given a file "/tmp/work/test/tmp/bar.txt"
      """
      bar
      """
      And set file attribute "/tmp/work/test/tmp/bar.txt"
      """
      rwxr-xr-x wheel leeonky 2022-10-09T06:47:10Z
      """
      And a zip file "/tmp/work/test/dir/zip.zip":
        | /tmp/work/test/tmp/folder  |
        | /tmp/work/test/tmp/bar.txt |
      Then zip file "/tmp/work/test/dir/zip.zip" should dump:
      """
      zip archive
      2022-10-09T06:47:10Z      3 bar.txt
      folder/
          2022-10-09T06:47:04Z      3 foo.txt
          zip1.zip zip1/
              2022-10-09T06:47:30Z      5 file1.txt
              2022-10-09T06:47:02Z      5 file2.txt
      """
