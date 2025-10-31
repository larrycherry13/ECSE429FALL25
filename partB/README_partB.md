Part B – Story Testing Suite (ECSE-429)

Overview
This folder contains everything required for Part B of the ECSE-429 project:
- User stories (5 per team member)
- Gherkin feature files with acceptance tests (normal / alternate / error)
- Cucumber step definitions and hooks
- Bug summary for story tests
- Part B written report

What is being tested
We are testing the REST API todo list manager (runTodoManagerRestAPI-1.5.5.jar) on localhost:4567.
All stories and tests are based on the API behavior we observed in Part A (exploratory testing + unit tests).

How to run the API under test
1. Launch the REST API locally:
   java -jar runTodoManagerRestAPI-1.5.5.jar
2. The service must be reachable at:
   http://localhost:4567
3. If the service is not running, our story tests are expected to fail immediately. This is required by the assignment.

How to run the story tests (Cucumber)
The runnable test code for Part B is under:
partB/cucumber/src/test/

To execute it:
1. Copy the contents of partB/cucumber/src/test into the project's test source set
   (for example into src/test in a Maven project).
2. Add the dependencies from partB/pom-snippet.xml into the main pom.xml.
3. Run:
   mvn test

What is included in story tests
- Background section in every feature file to set clean initial state
- Scenario Outlines with Examples tables
- Three flows for each story:
  - Normal flow (expected successful behavior)
  - Alternate flow (valid variation / edge case / undocumented but working behavior)
  - Error flow (invalid input, not found, malformed data)

Independence / cleanup
Hooks.java:
- Checks that the API service is running before each scenario.
- Tracks any data created during the scenario.
- Deletes that data after the scenario.
This satisfies:
- Tests fail if service is not running.
- Each test restores the system to the initial state.
- Tests can run in any order.

Deliverables for submission
- UserStories.md: 5 user stories per team member.
- StoryTestPlan.md: planned normal / alternate / error flows for each story.
- cucumber/src/test/resources/features/...: all Gherkin .feature files.
- cucumber/src/test/java/...: all Cucumber step definitions, hooks, and runner.
- BugSummary_PartB.md: bugs found and which story they relate to.
- Report_PartB.md: written 5–10 page report for Part B.

Video requirement
For the final demo video for Part B:
1. Start the API.
2. Run mvn test once and show all story tests executing.
3. Re-run tests in a different order (e.g. run only some feature files in a different order).
The video should demonstrate that tests are independent and do cleanup.
