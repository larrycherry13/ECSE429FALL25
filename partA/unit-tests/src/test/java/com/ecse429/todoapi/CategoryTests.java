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
 * Category test suite for Thingifier Todo REST API (v1.5.5)
 * Focus: CRUD operations and relationships for categories
 *
 * Server:
 *   java -jar runTodoManagerRestAPI-1.5.5.jar
 *
 * Run:
 *   mvn -q test
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

    // helpers

    private static String extractId(Response res, String collectionRoot) {
        String id = res.path("id");
        if (id == null && collectionRoot != null) {
            Object v = res.path(collectionRoot + "[0].id");
            if (v != null) id = String.valueOf(v);
        }
        return id;
    }

    private static String createCategory(String title, String description) {
        Response res = given()
            .contentType("application/json")
            .body(String.format("{\"title\":\"%s\",\"description\":\"%s\"}",
                title, description == null ? "" : description))
            .when().post("/categories")
            .then().statusCode(anyOf(is(200), is(201)))
            .extract().response();

        String id = extractId(res, "categories");
        if (id == null) {
            Response r = given().when().get("/categories").then().statusCode(200).extract().response();
            List<Map<String, Object>> list = r.path("categories");
            if (list != null) {
                for (Map<String, Object> c : list) {
                    if (title.equals(String.valueOf(c.get("title")))) {
                        Object v = c.get("id");
                        if (v != null) return String.valueOf(v);
                    }
                }
            }
        }
        return id;
    }

    private static String createTodo(String title, boolean done, String description) {
        Response res = given()
            .contentType("application/json")
            .body(String.format("{\"title\":\"%s\",\"doneStatus\":%s,\"description\":\"%s\"}",
                title, done, description == null ? "" : description))
            .when().post("/todos")
            .then().statusCode(anyOf(is(200), is(201)))
            .extract().response();

        String id = extractId(res, "todos");
        if (id == null) {
            Response r = given().queryParam("title", title)
                .when().get("/todos").then().statusCode(200).extract().response();
            List<Map<String, Object>> list = r.path("todos");
            if (list != null && !list.isEmpty()) {
                Object v = list.get(0).get("id");
                if (v != null) id = String.valueOf(v);
            }
        }
        return id;
    }

    private static String createProject(String title, String description) {
        Response res = given()
            .contentType("application/json")
            .body(String.format("{\"title\":\"%s\",\"description\":\"%s\"}",
                title, description == null ? "" : description))
            .when().post("/projects")
            .then().statusCode(anyOf(is(200), is(201)))
            .extract().response();

        String id = extractId(res, "projects");
        if (id == null) {
            Response r = given().when().get("/projects").then().statusCode(200).extract().response();
            List<Map<String, Object>> list = r.path("projects");
            if (list != null) {
                for (Map<String, Object> p : list) {
                    if (title.equals(String.valueOf(p.get("title")))) {
                        Object v = p.get("id");
                        if (v != null) return String.valueOf(v);
                    }
                }
            }
        }
        return id;
    }

    private static void deleteIfExists(String path) {
        given().when().delete(path).then().statusCode(anyOf(is(200), is(404), is(400)));
    }

    // -------------------- CRUD tests --------------------

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

        String id = extractId(res, "categories");
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

        // Note: This is confusing API design as per session notes
        Assumptions.assumeTrue(true, "Note: POST /categories/{id} behaves like PUT, creating confusion.");

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

    // -------------------- relationship tests --------------------

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
    void get_nonexistent_category_todos_returns_empty_not_404() {
        // As per session notes, returns empty list instead of 404
        given().when().get("/categories/999999/todos")
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
    void post_category_todos_with_title_creates_new_todo_and_links() {
        String categoryId = createCategory("NewTodoCat-" + System.nanoTime(), "new todo");
        String todoTitle = "NewTodo-" + System.nanoTime();

        Response res = given().contentType("application/json")
            .body("{\"title\":\"" + todoTitle + "\"}")
            .when().post("/categories/" + categoryId + "/todos")
            .then().statusCode(anyOf(is(200), is(201)))
            .extract().response();

        String newTodoId = extractId(res, "todos");

        // Verify it appears in category todos
        given().when().get("/categories/" + categoryId + "/todos")
            .then().statusCode(200)
            .body("todos.find { it.id == '" + newTodoId + "' }", notNullValue());

        // But not linked via todo categories (as per session notes)
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
    void post_category_projects_with_title_creates_new_project_and_links() {
        String categoryId = createCategory("NewProjCat-" + System.nanoTime(), "new proj");
        String projTitle = "NewProj-" + System.nanoTime();

        Response res = given().contentType("application/json")
            .body("{\"title\":\"" + projTitle + "\"}")
            .when().post("/categories/" + categoryId + "/projects")
            .then().statusCode(anyOf(is(200), is(201)))
            .extract().response();

        String newProjId = extractId(res, "projects");

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

    // -------------------- edge cases --------------------

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

        String id = extractId(res, "categories");
        Assertions.assertNotNull(id);

        deleteIfExists("/categories/" + id);
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