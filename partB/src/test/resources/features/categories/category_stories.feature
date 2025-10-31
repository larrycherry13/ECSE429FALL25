Feature: Category stories
  As a user
  I want to organize todos by category
  So that I can filter work

  Background:
    Given the todo API service is running
    And the system is reset to a clean initial state

  # NOTE for Categories:
  # Linking an existing todo to an existing category by ID returns 404.
  # BUT sending { "title": "someTodo" } creates a new todo and links it.
  # This difference will be covered in Alternate vs Error flow.

  # Normal flow Scenario Outline (create category, assign todo using title)
  # Alternate flow Scenario Outline
  # Error flow Scenario Outline (404 on linking by id)