Feature: Complete Project
  As a user
  I want to mark projects as completed
  So that I can track finished work

  Background:
    Given the todo API service is running
    And the system is reset to a clean initial state

  Scenario: Normal flow - mark project completed
    When I create a project with title "Deployment" and description "Production rollout"
    And I mark that project as completed
    Then the response status should be 200
    And the project should have completed status true

  Scenario: Alternate flow - mark completed again
    When I create a project with title "Deployment" and description "Production rollout"
    And I mark that project as completed
    And I mark that project as completed
    Then the response status should be 200

  Scenario: Error flow - mark invalid project
    When I delete a project that does not exist
    Then the response status should be 404
