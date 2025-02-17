Feature: exchange

  Rule: inspect not active, web server started and launch page
    Background:
      When launch inspector web server

    Scenario: show all DAL instance name on page
      Given created DAL 'Ins1' with inspector extended
      And created DAL 'Ins2' with inspector extended
      When launch inspector web page
      Then you will see all remote DAL instances:
        | Ins1 | Ins2 |

    Scenario: update instance names on page when create more DAL instance with inspector extension
      When launch inspector web page
      Given created DAL 'Ins1' with inspector extended
      And created DAL 'Ins2' with inspector extended
      Then you will see all remote DAL instances:
        | Ins1 | Ins2 |

  Rule: inspect not active, launch opened and start web server
    Background:
      Given launch inspector web server
      And launch inspector web page
      And shutdown web server

    Scenario: show all DAL instance name on page
      Given created DAL 'Ins1' with inspector extended
      And created DAL 'Ins2' with inspector extended
      When launch inspector web server
      Then you will see all remote DAL instances:
        | Ins1 | Ins2 |

    Scenario: update instance names on page when create more DAL instance with inspector extension
      Given created DAL 'Ins1' with inspector extended
      And created DAL 'Ins2' with inspector extended
      Then you will see all remote DAL instances:
        | Ins1 | Ins2 |

#  Rule: inspect active, web server started and launch page
#    Background:
#      When launch inspector web server
#
#    Scenario: inspect will suspend, web page will catch the code and result
#      When evaluating the following:
