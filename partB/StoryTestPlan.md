Story Test Plan – Part B (ECSE-429)

Overview
This document outlines the planned acceptance-test coverage for all user stories.
Each story has:
• Normal flow – expected successful operation  
• Alternate flow – valid edge or undocumented but working case  
• Error flow – invalid data or operation that should fail  

──────────────────────────────
Laurent – Todos
──────────────────────────────
T1 Create Todo  
  • Normal – POST /todos with title and description → 201 Created  
  • Alternate – POST /todos with only title (no description) → 201 Created with default doneStatus false  
  • Error – POST /todos with malformed JSON → 400 Bad Request  

T2 Retrieve Todo  
  • Normal – GET /todos/{id} for existing id → 200 OK  
  • Alternate – GET /todos?title={title} → 200 OK (list contains that todo)  
  • Error – GET /todos/{invalidId} → 404 Not Found  

T3 Mark Todo Complete  
  • Normal – PUT /todos/{id} {doneStatus:true} → 200 OK  
  • Alternate – PATCH /todos/{id} with partial body → 200 OK  
  • Error – PUT /todos/{id} with invalid field name → 400 Bad Request  

T4 Update Todo Description  
  • Normal – PUT /todos/{id} {description:"Updated"} → 200 OK  
  • Alternate – Update with same description (no change) → 200 OK (no side effects)  
  • Error – PUT /todos/{invalidId} → 404 Not Found  

T5 Delete Todo  
  • Normal – DELETE /todos/{id} → 200 OK and subsequent GET 404  
  • Alternate – DELETE /todos/{id} twice → first 200, second 404  
  • Error – DELETE /todos/{nonNumericId} → 400 Bad Request  

──────────────────────────────
Noah – Projects
──────────────────────────────
P1 Create Project – POST /projects → 201; Alternate missing description → 201; Error malformed JSON → 400  
P2 Update Project – PUT /projects/{id} → 200; Alternate PATCH partial update → 200; Error invalid id → 404  
P3 List Project Todos – GET /projects/{id}/todos → 200; Alternate empty project → 200 empty array; Error bad id → 404  
P4 Add Todo to Project – POST /projects/{id}/todos → 201; Alternate re-add same todo → 409 or ignored; Error nonexistent todo → 404  
P5 Delete Project – DELETE /projects/{id} → 200; Alternate delete again → 404; Error invalid id → 400  

──────────────────────────────
Vladimir – Categories
──────────────────────────────
C1 Create Category – POST /categories → 201; Alternate without description → 201; Error malformed JSON → 400  
C2 Update Category – PUT /categories/{id} → 200; Alternate PATCH rename → 200; Error bad id → 404  
C3 Assign Todo to Category – POST /categories/{catId}/todos {title:"todo1"} → 201 (created and linked); Alternate use {id} → 404 (undocumented failure); Error missing body → 400  
C4 View Todos in Category – GET /categories/{id}/todos → 200; Alternate empty category → 200 []; Error bad id → 404  
C5 Delete Category – DELETE /categories/{id} → 200; Alternate delete twice → 404; Error invalid id → 400  

──────────────────────────────
Marcello – Interoperability
──────────────────────────────
I1 Link Todo to Project – POST /projects/{pid}/todos {id:todoId} → 201; Alternate link again → 409; Error bad id → 404  
I2 Link Todo to Category – POST /categories/{cid}/todos {title:"todo"} → 201; Alternate use id instead of title → 404; Error missing body → 400  
I3 Move Todo Between Projects – DELETE from old, POST to new → 200; Alternate copy → 201; Error target project not found → 404  
I4 View Relationships – GET /projects/{id}/todos and GET /categories/{id}/todos → 200; Alternate cross-check consistency; Error invalid id → 404  
I5 Delete Parent Object Behavior – DELETE /project or category while linked → 200 (no cascade); Alternate expect todo still exists → 200; Error broken link returns 404 on fetching relationship  

Notes
• Each story will have a Background ensuring a clean state before tests.  
• All story tests can run in any order (Hooks handles cleanup).  
• Each normal/alternate/error flow corresponds directly to a Scenario Outline in its .feature file.  
