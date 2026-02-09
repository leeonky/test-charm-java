Feature: Flat List

  Rule: Type Object

    Background:
      Given the following declarations:
        """
        JFactory jFactory = new JFactory();
        """
      Given the following declarations:
        """
        Collector collector = jFactory.collector();
        """

    Scenario: create a Collection
      When "collector" collect and build with the following properties:
        """
        = [hello world]
        """
      Then the result should be:
        """
        : {
          ::this= [hello world]
          ::object.class.simpleName= Object[]
        }
        """

    Scenario: use : [] to create Default Collection
      When "collector" collect and build with the following properties:
        """
        = []
        """
      Then the result should be:
        """
        : {
          ::this= []
          ::object.class.simpleName= Object[]
        }
        """

    Scenario: use = [] to create Default Collection
      When "collector" collect and build with the following properties:
        """
        = []
        """
      Then the result should be:
        """
        : {
          ::this= []
          ::object.class.simpleName= Object[]
        }
        """

#TODO object Type with :
#TODO object Type with =
#TODO Bean Type with =
#TODO Default Spec of Bean with =
#TODO Given Spec of Map :/=
#TODO Given Spec of Bean :/=
