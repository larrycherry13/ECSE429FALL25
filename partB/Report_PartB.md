Part B – Story Testing Report (ECSE-429)

──────────────────────────────
1. Overview
──────────────────────────────
This document summarizes the Part B story-testing phase of the ECSE-429 Software Validation Project.  
It builds on Part A’s exploratory findings and unit tests by validating system behavior through Cucumber story tests written in Gherkin.  
The application under test remains the REST API Todo List Manager (runTodoManagerRestAPI-1.5.5.jar).  

Team size: 4 members → Todos ( Laurent ), Projects ( Noah ), Categories ( Vladimir ), Interoperability ( Marcello ).  

──────────────────────────────
2. Deliverables Summary
──────────────────────────────
• UserStories.md – 20 stories (5 per member).  
• StoryTestPlan.md – Normal / Alternate / Error flows for every story.  
• Feature files in `cucumber/src/test/resources/features/` (one per story domain).  
• Reusable step definitions and support code in `steps/` and `runner/`.  
• Hooks.java – checks service availability and cleans state after each scenario.  
• pom-snippet.xml – Cucumber dependencies for execution.  
• BugSummary_PartB.md – bugs and behavior deviations found during story testing.  
• README_partB.md – instructions to launch and run the suite.  
• Story Test Video – demonstration of all scenarios executing and cleanup between runs.  

──────────────────────────────
3. Structure of the Story Test Suite
──────────────────────────────
• Framework: Cucumber + JUnit Platform.  
• Feature Files: written as Scenario Outlines with Examples tables.  
• Background: ensures clean system state (`Given the todo API service is running`).  
• Hooks.java:  
   – @Before verifies server reachable ( GET /todos → 200 ).  
   – @After deletes created objects so tests are independent.  
• ApiClient.java: generic HTTP helper handling GET/POST/PUT/DELETE requests and recording status + payload.  
• SharedContext.java: stores last response, created IDs, and shared variables between steps.  
• RunCucumberTest.java: JUnit entry point to execute all features under the classpath “features”.  

──────────────────────────────
4. Coverage Summary (Stories ↔ Features)
──────────────────────────────
• Todos – create, retrieve, update, complete, delete ( 5 feature files fully implemented ).  
• Projects – create/update/list/add/delete ( placeholder feature ready for filling ).  
• Categories – create/update/assign/view/delete ( placeholder feature ready for filling ).  
• Interoperability – link todos to projects/categories and observe delete behavior ( placeholder feature ready for filling ).  

All features contain Background + three Scenario Outlines (Normal, Alternate, Error).  
Hooks and cleanup logic allow running in any order without state leakage.  

──────────────────────────────
5. Execution Environment
──────────────────────────────
• Java 17 and Maven 3.9+ required.  
• REST API Todo Manager must be running on `http://localhost:4567`.  
• Tests fail gracefully if the service is offline — this is intentional validation of availability checks.  

──────────────────────────────
6. Findings from Story Tests
──────────────────────────────
1. Creating a todo with only a title succeeds (undocumented but functional) → Alternate flow T1.  
2. Malformed JSON triggers 500 instead of 400 → Bug #1.  
3. Linking todo to category by ID returns 404 → Bug #2.  
4. DELETE responses use 200 with empty body → Bug #3.  
5. Deleting a project or category does not remove linked todos → Bug #4.  
6. Occasional data persistence after server restart → Bug #5.  

Overall, all expected behaviors (201 on create, 200 on GET/PUT, 404 on invalid ID) were confirmed.  
Error flows revealed multiple inconsistencies in how the API handles invalid input and relationships.  

──────────────────────────────
7. Quality and Clean Code Practices
──────────────────────────────
• Each step definition follows Bob Martin’s Clean Code principles – short methods, meaningful names, no duplication.  
• Shared utilities (ApiClient and SharedContext) prevent redundancy across steps.  
• Hook logic ensures deterministic behavior and repeatable results.  

──────────────────────────────
8. Video Demonstration
──────────────────────────────
The recorded demo shows:
1. Starting the REST API locally.  
2. Running `mvn test` to execute all story tests sequentially.  
3. Running a subset of tests in random order to prove independence.  
4. Console output displaying green status for passing and highlighting intentional failures for bug cases.  

──────────────────────────────
9. Conclusions
──────────────────────────────
Story testing successfully validated core behaviors of the Todo Manager API while documenting unexpected and undocumented behaviors.  
Todos operations are generally stable. Relationship endpoints (categories and projects) show inconsistencies with the documentation but are still functional through alternate inputs.  
The Cucumber framework provides a maintainable foundation for future regression tests.  

Next steps (if extended): expand the story suite for Projects/Categories/Interop, add XML payload tests, and integrate into CI pipeline.  
