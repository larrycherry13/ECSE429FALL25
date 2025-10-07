package com.ecse429.todoapi;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Assumptions;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Category test suite for Todo REST API (v1.5.5)
 * Focus: CRUD operations and relationships for categories
  */
@TestMethodOrder(MethodOrderer.Random.class)
public class CategoryTests {

    private static final String BASE = "http://localhost";
    private static final int PORT = 4567;

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = BASE;
        RestAssured.port = PORT;

        // Fail fast if service is not running
        given().when().get("/todos").then().statusCode(anyOf(is(200), is(204)));
    }

    @AfterEach
    void tearDown() {
        // Clean up all data after each test to ensure test isolation
        TestHelper.cleanupAllData();
    }

    // Helpers

    private static String createCategory(String title, String description) {
        return TestHelper.createCategory(title, description);
    }

    private static String createTodo(String title, boolean done, String description) {
        return TestHelper.createTodo(title, done, description);
    }

    private static String createProject(String title, String description) {
        return TestHelper.createProject(title, description);
    }

    private static void deleteIfExists(String path) {
        TestHelper.deleteIfExists(path);
    }

    // CRUD tests

    @Test
    void get_categories_returns_list() {
        String categoryId = createCategory("TestCat-" + System.nanoTime(), "test");

        Response res = given().when().get("/categories").then().statusCode(200).extract().response();
        List<Map<String, Object>> categories = res.path("categories");
        Assertions.assertNotNull(categories);
        Assertions.assertTrue(categories.size() >= 1);

        deleteIfExists("/categories/" + categoryId);
    }

    @Test
    void head_categories_returns_200() {
        given().when().head("/categories").then().statusCode(200);
    }

    @Test
    void post_categories_creates_new_category() {
        String title = "NewCat-" + System.nanoTime();
        Response res = given()
            .contentType("application/json")
            .body("{\"title\":\"" + title + "\",\"description\":\"test desc\"}")
            .when().post("/categories")
            .then().statusCode(anyOf(is(200), is(201)))
            .extract().response();

        String id = TestHelper.extractId(res, "categories");
        Assertions.assertNotNull(id);

        // Verify it exists
        given().when().get("/categories/" + id).then().statusCode(200);

        deleteIfExists("/categories/" + id);
    }

    @Test
    void get_category_by_id_returns_category() {
        String id = createCategory("GetCat-" + System.nanoTime(), "get test");

        given().when().get("/categories/" + id)
            .then().statusCode(200)
            .body("categories[0].id", equalTo(id))
            .body("categories[0].title", containsString("GetCat-"));

        deleteIfExists("/categories/" + id);
    }

    @Test
    void get_nonexistent_category_returns_404() {
        given().when().get("/categories/999999").then().statusCode(404);
    }

    @Test
    void head_category_by_id_returns_200() {
        String id = createCategory("HeadCat-" + System.nanoTime(), "head test");

        given().when().head("/categories/" + id).then().statusCode(200);

        deleteIfExists("/categories/" + id);
    }

    @Test
    void put_category_updates_category() {
        String id = createCategory("PutCat-" + System.nanoTime(), "original");

        given().contentType("application/json")
            .body("{\"title\":\"UpdatedCat-" + System.nanoTime() + "\",\"description\":\"updated\"}")
            .when().put("/categories/" + id)
            .then().statusCode(anyOf(is(200), is(201)));

        // Verify update
        given().when().get("/categories/" + id)
            .then().statusCode(200)
            .body("categories[0].title", containsString("UpdatedCat-"));

        deleteIfExists("/categories/" + id);
    }

    @Test
    void put_category_with_id_in_body_returns_400() {
        String id = createCategory("PutBadCat-" + System.nanoTime(), "bad put");

        given().contentType("application/json")
            .body("{\"id\":\"" + id + "\",\"title\":\"BadUpdate\"}")
            .when().put("/categories/" + id)
            .then().statusCode(400);

        deleteIfExists("/categories/" + id);
    }

    @Test
    void post_category_by_id_acts_like_put() {
        String id = createCategory("PostPutCat-" + System.nanoTime(), "post put");

        // POST on existing id should behave like PUT
        given().contentType("application/json")
            .body("{\"title\":\"PostUpdated-" + System.nanoTime() + "\"}")
            .when().post("/categories/" + id)
            .then().statusCode(anyOf(is(200), is(201)));

        deleteIfExists("/categories/" + id);
    }

    @Test
    void post_category_by_id_with_id_in_body_returns_400() {
        String id = createCategory("PostBadCat-" + System.nanoTime(), "bad post");

        given().contentType("application/json")
            .body("{\"id\":\"" + id + "\",\"title\":\"BadPost\"}")
            .when().post("/categories/" + id)
            .then().statusCode(400);

        deleteIfExists("/categories/" + id);
    }

    @Test
    void delete_category_removes_category() {
        String id = createCategory("DelCat-" + System.nanoTime(), "delete test");

        given().when().delete("/categories/" + id).then().statusCode(200);

        // Verify deleted
        given().when().get("/categories/" + id).then().statusCode(404);
    }

    @Test
    void delete_nonexistent_category_returns_404() {
        given().when().delete("/categories/999999").then().statusCode(404);
    }

    // Relationship tests

    @Test
    void get_category_todos_returns_todos() {
        String categoryId = createCategory("CatTodos-" + System.nanoTime(), "todos test");
        String todoId = createTodo("TodoForCat-" + System.nanoTime(), false, "test");

        // Link via todo endpoint (since category endpoint doesn't work for existing)
        given().contentType("application/json")
            .body("{\"id\":\"" + categoryId + "\"}")
            .when().post("/todos/" + todoId + "/categories")
            .then().statusCode(anyOf(is(200), is(201)));

        given().when().get("/categories/" + categoryId + "/todos")
            .then().statusCode(200)
            .body("todos.find { it.id == '" + todoId + "' }", notNullValue());

        deleteIfExists("/todos/" + todoId + "/categories/" + categoryId);
        deleteIfExists("/categories/" + categoryId);
        deleteIfExists("/todos/" + todoId);
    }

    @Test
    void get_category_todos_returns_todos_observed() {
        String categoryId = createCategory("CatTodosObs-" + System.nanoTime(), "todos test observed");
        String todoId = createTodo("TodoForCatObs-" + System.nanoTime(), false, "test observed");

        given().contentType("application/json")
            .body("{\"id\":\"" + categoryId + "\"}")
            .when().post("/todos/" + todoId + "/categories")
            .then().statusCode(anyOf(is(200), is(201)));

        Response response = given().when().get("/categories/" + categoryId + "/todos")
            .then().statusCode(200)
            .extract().response();

        // Assert that the relationship is broken
        List<Map<String, Object>> todos = response.path("todos");
        boolean todoFound = todos != null && todos.stream()
            .anyMatch(t -> String.valueOf(t.get("id")).equals(todoId));
        Assertions.assertFalse(todoFound, "Todo should NOT appear in category's todos due to broken relationship");

        deleteIfExists("/todos/" + todoId + "/categories/" + categoryId);
        deleteIfExists("/categories/" + categoryId);
        deleteIfExists("/todos/" + todoId);
    }

    @Test
    void get_nonexistent_category_todos_returns_empty_not_404() {
        // As per session notes, returns empty list instead of 404
        given().when().get("/categories/999/todos")
            .then().statusCode(200)
            .body("todos", anyOf(nullValue(), hasSize(0)));
    }

    @Test
    void head_category_todos_returns_200() {
        String categoryId = createCategory("HeadTodosCat-" + System.nanoTime(), "head todos");

        given().when().head("/categories/" + categoryId + "/todos").then().statusCode(200);

        deleteIfExists("/categories/" + categoryId);
    }

    @Test
    void post_category_todos_with_id_returns_404() {
        String categoryId = createCategory("PostTodosCat-" + System.nanoTime(), "post todos");
        String todoId = createTodo("PostTodo-" + System.nanoTime(), false, "post test");

        // As per session notes, linking existing returns 404
        given().contentType("application/json")
            .body("{\"id\":\"" + todoId + "\"}")
            .when().post("/categories/" + categoryId + "/todos")
            .then().statusCode(404);

        deleteIfExists("/categories/" + categoryId);
        deleteIfExists("/todos/" + todoId);
    }

    @Test
    void post_category_todos_with_id_returns_404_observed() {
        String categoryId = createCategory("PostTodosCatObs-" + System.nanoTime(), "post todos observed");
        String todoId = createTodo("PostTodoObs-" + System.nanoTime(), false, "post test observed");

        Response response = given().contentType("application/json")
            .body("{\"id\":\"" + todoId + "\"}")
            .when().post("/categories/" + categoryId + "/todos")
            .then().statusCode(201)
            .extract().response();

        // Assert the unexpected success response
        Assertions.assertEquals(201, response.getStatusCode(), "Unexpectedly succeeded with 201 instead of failing with 404");
        String responseBody = response.getBody().asString();
        Assertions.assertNotNull(responseBody, "Response should contain body content");

        deleteIfExists("/categories/" + categoryId);
        deleteIfExists("/todos/" + todoId);
    }

    @Test
    void post_category_todos_with_title_creates_new_todo_and_links() {
        String categoryId = createCategory("NewTodoCat-" + System.nanoTime(), "new todo");
        String todoTitle = "NewTodo-" + System.nanoTime();

        Response res = given().contentType("application/json")
            .body("{\"title\":\"" + todoTitle + "\"}")
            .when().post("/categories/" + categoryId + "/todos")
            .then().statusCode(anyOf(is(200), is(201)))
            .extract().response();

        String newTodoId = TestHelper.extractId(res, "todos");

        // Verify it appears in category todos
        given().when().get("/categories/" + categoryId + "/todos")
            .then().statusCode(200)
            .body("todos.find { it.id == '" + newTodoId + "' }", notNullValue());

        // But not linked via todo categories
        Response rev = given().when().get("/todos/" + newTodoId + "/categories")
            .then().statusCode(200)
            .extract().response();

        List<Map<String, Object>> cats = rev.path("categories");
        boolean linked = cats != null && cats.stream()
            .anyMatch(c -> String.valueOf(c.get("id")).equals(categoryId));

        Assumptions.assumeFalse(linked, "Note: New todo created via category endpoint not linked in reverse.");

        deleteIfExists("/categories/" + categoryId + "/todos/" + newTodoId);
        deleteIfExists("/categories/" + categoryId);
        deleteIfExists("/todos/" + newTodoId);
    }

    @Test
    void delete_category_todo_removes_relationship() {
        String categoryId = createCategory("DelRelCat-" + System.nanoTime(), "del rel");
        String todoId = createTodo("DelRelTodo-" + System.nanoTime(), false, "del rel");

        // Link via todo
        given().contentType("application/json")
            .body("{\"id\":\"" + categoryId + "\"}")
            .when().post("/todos/" + todoId + "/categories")
            .then().statusCode(anyOf(is(200), is(201)));

        // Delete via category
        given().when().delete("/categories/" + categoryId + "/todos/" + todoId)
            .then().statusCode(200);

        // Verify removed
        given().when().get("/categories/" + categoryId + "/todos")
            .then().statusCode(200)
            .body("todos.find { it.id == '" + todoId + "' }", nullValue());

        deleteIfExists("/categories/" + categoryId);
        deleteIfExists("/todos/" + todoId);
    }

    @Test
    void delete_category_todo_removes_relationship_observed() {
        String categoryId = createCategory("DelRelCatObs-" + System.nanoTime(), "del rel observed");
        String todoId = createTodo("DelRelTodoObs-" + System.nanoTime(), false, "del rel observed");

        given().contentType("application/json")
            .body("{\"id\":\"" + categoryId + "\"}")
            .when().post("/todos/" + todoId + "/categories")
            .then().statusCode(anyOf(is(200), is(201)));

        // Observed behavior: DELETE returns 404 error instead of 200
        Response errorResponse = given().when().delete("/categories/" + categoryId + "/todos/" + todoId)
            .then().statusCode(404)
            .extract().response();

        Assertions.assertEquals(404, errorResponse.getStatusCode(), "Expected 404 error status code");
        
        deleteIfExists("/categories/" + categoryId);
        deleteIfExists("/todos/" + todoId);
    }

    @Test
    void get_category_projects_returns_projects() {
        String categoryId = createCategory("CatProj-" + System.nanoTime(), "projects test");
        String projectId = createProject("ProjForCat-" + System.nanoTime(), "test");

        // Link via project endpoint
        given().contentType("application/json")
            .body("{\"id\":\"" + categoryId + "\"}")
            .when().post("/projects/" + projectId + "/categories")
            .then().statusCode(anyOf(is(200), is(201)));

        given().when().get("/categories/" + categoryId + "/projects")
            .then().statusCode(200)
            .body("projects.find { it.id == '" + projectId + "' }", notNullValue());

        deleteIfExists("/projects/" + projectId + "/categories/" + categoryId);
        deleteIfExists("/categories/" + categoryId);
        deleteIfExists("/projects/" + projectId);
    }

    @Test
    void get_category_projects_returns_projects_observed() {
        String categoryId = createCategory("CatProjObs-" + System.nanoTime(), "projects test observed");
        String projectId = createProject("ProjForCatObs-" + System.nanoTime(), "test observed");

        given().contentType("application/json")
            .body("{\"id\":\"" + categoryId + "\"}")
            .when().post("/projects/" + projectId + "/categories")
            .then().statusCode(anyOf(is(200), is(201)));

        // Observed behavior: project is NOT in category's projects list even after linking
        Response response = given().when().get("/categories/" + categoryId + "/projects")
            .then().statusCode(200)
            .extract().response();

        List<Map<String, Object>> projects = response.path("projects");
        boolean projectFound = projects != null && projects.stream()
            .anyMatch(p -> String.valueOf(p.get("id")).equals(projectId));
        Assertions.assertFalse(projectFound, "Project should NOT appear in category's projects due to broken relationship");

        deleteIfExists("/projects/" + projectId + "/categories/" + categoryId);
        deleteIfExists("/categories/" + categoryId);
        deleteIfExists("/projects/" + projectId);
    }

    @Test
    void head_category_projects_returns_200() {
        String categoryId = createCategory("HeadProjCat-" + System.nanoTime(), "head proj");

        given().when().head("/categories/" + categoryId + "/projects").then().statusCode(200);

        deleteIfExists("/categories/" + categoryId);
    }

    @Test
    void post_category_projects_with_id_returns_404() {
        String categoryId = createCategory("PostProjCat-" + System.nanoTime(), "post proj");
        String projectId = createProject("PostProj-" + System.nanoTime(), "post test");

        given().contentType("application/json")
            .body("{\"id\":\"" + projectId + "\"}")
            .when().post("/categories/" + categoryId + "/projects")
            .then().statusCode(404);

        deleteIfExists("/categories/" + categoryId);
        deleteIfExists("/projects/" + projectId);
    }

    @Test
    void post_category_projects_with_id_returns_404_observed() {
        String categoryId = createCategory("PostProjCatObs-" + System.nanoTime(), "post proj observed");
        String projectId = createProject("PostProjObs-" + System.nanoTime(), "post test observed");

        // Observed behavior: linking existing project returns 201 instead of expected 404
        Response response = given().contentType("application/json")
            .body("{\"id\":\"" + projectId + "\"}")
            .when().post("/categories/" + categoryId + "/projects")
            .then().statusCode(201)
            .extract().response();

        Assertions.assertEquals(201, response.getStatusCode(), "Unexpectedly succeeded with 201 instead of failing with 404");
        String responseBody = response.getBody().asString();
        Assertions.assertNotNull(responseBody, "Response should contain body content");

        deleteIfExists("/categories/" + categoryId);
        deleteIfExists("/projects/" + projectId);
    }

    @Test
    void post_category_projects_with_title_creates_new_project_and_links() {
        String categoryId = createCategory("NewProjCat-" + System.nanoTime(), "new proj");
        String projTitle = "NewProj-" + System.nanoTime();

        Response res = given().contentType("application/json")
            .body("{\"title\":\"" + projTitle + "\"}")
            .when().post("/categories/" + categoryId + "/projects")
            .then().statusCode(anyOf(is(200), is(201)))
            .extract().response();

        String newProjId = TestHelper.extractId(res, "projects");

        given().when().get("/categories/" + categoryId + "/projects")
            .then().statusCode(200)
            .body("projects.find { it.id == '" + newProjId + "' }", notNullValue());

        deleteIfExists("/categories/" + categoryId + "/projects/" + newProjId);
        deleteIfExists("/categories/" + categoryId);
        deleteIfExists("/projects/" + newProjId);
    }

    @Test
    void delete_category_project_removes_relationship() {
        String categoryId = createCategory("DelProjRelCat-" + System.nanoTime(), "del proj rel");
        String projectId = createProject("DelProjRel-" + System.nanoTime(), "del rel");

        // Link via project
        given().contentType("application/json")
            .body("{\"id\":\"" + categoryId + "\"}")
            .when().post("/projects/" + projectId + "/categories")
            .then().statusCode(anyOf(is(200), is(201)));

        given().when().delete("/categories/" + categoryId + "/projects/" + projectId)
            .then().statusCode(200);

        given().when().get("/categories/" + categoryId + "/projects")
            .then().statusCode(200)
            .body("projects.find { it.id == '" + projectId + "' }", nullValue());

        deleteIfExists("/categories/" + categoryId);
        deleteIfExists("/projects/" + projectId);
    }

    @Test
    void delete_category_project_removes_relationship_observed() {
        String categoryId = createCategory("DelProjRelCatObs-" + System.nanoTime(), "del proj rel observed");
        String projectId = createProject("DelProjRelObs-" + System.nanoTime(), "del rel observed");

        given().contentType("application/json")
            .body("{\"id\":\"" + categoryId + "\"}")
            .when().post("/projects/" + projectId + "/categories")
            .then().statusCode(anyOf(is(200), is(201)));

        // Observed behavior: DELETE returns 404 error instead of 200
        Response errorResponse = given().when().delete("/categories/" + categoryId + "/projects/" + projectId)
            .then().statusCode(404)
            .extract().response();

        Assertions.assertEquals(404, errorResponse.getStatusCode(), "Expected 404 error status code");
        
        deleteIfExists("/categories/" + categoryId);
        deleteIfExists("/projects/" + projectId);
    }

    // Edge cases

    @Test
    void large_id_returns_404() {
        given().when().get("/categories/9999999999999").then().statusCode(404);
        given().when().delete("/categories/9999999999999").then().statusCode(404);
    }

    @Test
    void negative_id_returns_404() {
        given().when().get("/categories/-1").then().statusCode(404);
        given().when().delete("/categories/-1").then().statusCode(404);
    }

    @Test
    void malformed_payload_returns_client_error() {
        String categoryId = createCategory("Malformed-" + System.nanoTime(), "malformed");

        given().contentType("application/json")
            .body("{}")
            .when().post("/categories/" + categoryId + "/todos")
            .then().statusCode(anyOf(is(400), is(422)));

        given().contentType("application/json")
            .body("{\"id\":\"abc\"}")
            .when().post("/categories/" + categoryId + "/projects")
            .then().statusCode(anyOf(is(400), is(404), is(422)));

        deleteIfExists("/categories/" + categoryId);
    }

    @Test
    void unexpected_fields_in_payload() {
        // Test POST with extra fields
        Response res = given().contentType("application/json")
            .body("{\"title\":\"ExtraField-" + System.nanoTime() + "\",\"description\":\"test\",\"extra\":\"field\"}")
            .when().post("/categories")
            .then().statusCode(anyOf(is(200), is(201)))
            .extract().response();

        String id = TestHelper.extractId(res, "categories");
        Assertions.assertNotNull(id);

        deleteIfExists("/categories/" + id);
    }

    @Test
    void unexpected_fields_in_payload_observed() {
        // Observed behavior: POST with extra fields returns 400 error instead of 200/201
        Response errorResponse = given().contentType("application/json")
            .body("{\"title\":\"ExtraFieldObs-" + System.nanoTime() + "\",\"description\":\"test\",\"extra\":\"field\"}")
            .when().post("/categories")
            .then().statusCode(400)
            .extract().response();

        Assertions.assertEquals(400, errorResponse.getStatusCode(), "Expected 400 error status code");
        String errorMessage = errorResponse.getBody().asString();
        Assertions.assertNotNull(errorMessage, "Error response should contain a message");
    }

    @Test
    void delete_category_with_relationships() {
        String categoryId = createCategory("DelWithRel-" + System.nanoTime(), "del with rel");
        String todoId = createTodo("TodoWithCat-" + System.nanoTime(), false, "rel");

        // Link
        given().contentType("application/json")
            .body("{\"id\":\"" + categoryId + "\"}")
            .when().post("/todos/" + todoId + "/categories")
            .then().statusCode(anyOf(is(200), is(201)));

        // Delete category - should work
        given().when().delete("/categories/" + categoryId).then().statusCode(200);

        // Todo should still exist
        given().when().get("/todos/" + todoId).then().statusCode(200);

        deleteIfExists("/todos/" + todoId);
    }
}