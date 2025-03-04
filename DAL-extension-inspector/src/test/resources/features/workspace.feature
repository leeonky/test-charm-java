Feature: workspace

  Scenario: copy new workspace with exist code
    Given Inspector in "FORCED" mode
    Given launch inspector web server
    Given launch inspector web page
    Given created DAL 'Ins1' with inspector extended
    And the 'Ins1' following input:
      """
      "hello"
      """
    And use DAL "Ins1" to evaluating the following:
      """
      ::inspect
      """
    When you:
      """
      WorkBench::await[Ins1].newWorkspace
      """
    Then you should see:
      """
      WorkBench[Ins1]::eventually: {
        Workspace.Current.header: '1'
      }

      WorkBench[Ins1].Workspace[0]::eventually: {
          Current: { header: Result }
                 : ```
                   java.lang.String
                   <hello>
                   ```
      }

      WorkBench[Ins1].Workspace[0]::eventually: {
          DAL: {
            value: '{}'
            classes= [... result ...]
          }

          Root: ```
                java.lang.String
                <hello>
                ```

          Error: ''

          Inspect: '{}'
      }

      WorkBench[Ins1].Workspace[1]::eventually: {
          Current: null
      }

      WorkBench[Ins1].Workspace[1]::eventually: {
          DAL: {
            value: '{}'
            classes= [code-editor]
          }

          Root: ''

          Result: ''

          Error: ''

          Inspect: ''
      }
      """
# execute in new space
    When you:
      """
      WorkBench[Ins1].Workspace[1].DAL: '= hello'
      """
    Then you should see:
      """
      WorkBench[Ins1].Workspace[1]::eventually: {
          Current: { header: Result }
                 : ```
                   java.lang.String
                   <hello>
                   ```
      }

      WorkBench[Ins1].Workspace[1]::eventually: {
          DAL: {
            value: '{}= hello'
            classes= [... result ...]
          }

          Root: ```
                java.lang.String
                <hello>
                ```

          Error: ''

          Inspect: "{}= 'hello'"
      }
      """
# copy to next
    When you:
      """
      WorkBench[Ins1].Workspace[0].newWorkspace
      """
    Then you should see:
      """
      WorkBench[Ins1]::eventually: {
        Workspace.Current.header: '1'
      }

      WorkBench[Ins1].Workspace[1]::eventually: {
          Current: null
      }

      WorkBench[Ins1].Workspace[1]::eventually: {
          DAL: {
            value: '{}'
            classes= [code-editor]
          }

          Root: ''

          Result: ''

          Error: ''

          Inspect: ''
      }
      """
