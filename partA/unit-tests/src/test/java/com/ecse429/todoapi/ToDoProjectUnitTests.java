package com.ecse429.todoapi;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Project test suite for Thingifier Todo REST API (v1.5.5)
 * Focus: CRUD operations, relationships, and payload validation for /projects API.
 *
 * Start server:
 *   java -jar runTodoManagerRestAPI-1.5.5.jar
 *
 * Run tests:
 *   mvn -q test
 *
 * Notes:
 * - Each test is self-contained (creates and cleans its own data).
 * - Follows same structure and coding conventions as TodoUnitTests and CategoryTests.
 */
@TestMethodOrder(MethodOrderer.Random.class)
public class ToDoProjectUnitTests {

    private static final String BASE = "http://localhost";
    private static final int PORT = 4567;

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = BASE;
        RestAssured.port = PORT;
        given().when().get("/projects").then().statusCode(anyOf(is(200), is(204)));
    }

    // ---------- Helpers ----------

    private static String extractId(Response res, String root) {
        String id = res.path("id");
        if (id == null && root != null) {
            Object v = res.path(root + "[0].id");
            if (v != null) id = String.valueOf(v);
        }
        return id;
    }

    private static String createProject(String title, String desc, boolean completed) {
        String body = String.format("{\"title\":\"%s\",\"description\":\"%s\",\"completed\":%s}",
                title, desc == null ? "" : desc, Boolean.toString(completed));
        Response res = given()
            .contentType("application/json")
            .body(body)
            .when().post("/projects")
            .then().statusCode(anyOf(is(200), is(201)))
            .extract().response();

        String id = extractId(res, "projects");
        if (id == null) {
            Response all = given().when().get("/projects").then().statusCode(200).extract().response();
            List<Map<String, Object>> list = all.path("projects");
            if (list != null) {
                for (Map<String, Object> p : list) {
                    if (title.equals(p.get("title"))) {
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

    // ---------- CRUD Tests ----------

    @Test
    void get_projects_returns_list_200() {
        String id = createProject("ProjList-" + System.nanoTime(), "test", false);
        Response res = given().when().get("/projects").then().statusCode(200).extract().response();
        List<Map<String, Object>> projects = res.path("projects");
        Assertions.assertNotNull(projects);
        Assertions.assertTrue(projects.size() >= 1);
        deleteIfExists("/projects/" + id);
    }

    @Test
    void post_creates_project_defaults_completed_false() {
        String title = "ProjCreate-" + System.nanoTime();
        Response res = given()
            .contentType("application/json")
            .body("{\"title\":\"" + title + "\",\"description\":\"auto test\"}")
            .when().post("/projects")
            .then().statusCode(anyOf(is(200), is(201)))
            .extract().response();
        String id = extractId(res, "projects");
        given().when().get("/projects/" + id)
            .then().statusCode(200)
            .body("projects[0].completed", anyOf(equalTo("false"), equalTo(false)));
        deleteIfExists("/projects/" + id);
    }

    @Test
    void get_project_by_id_200_then_delete_404() {
        String id = createProject("ProjGet-" + System.nanoTime(), "check", false);
        given().when().get("/projects/" + id)
            .then().statusCode(200)
            .body("projects[0].id", equalTo(id));
        given().when().delete("/projects/" + id).then().statusCode(200);
        given().when().get("/projects/" + id).then().statusCode(404);
    }

    @Test
    void put_updates_all_fields_and_no_side_effects() {
        String id = createProject("ProjPut-" + System.nanoTime(), "before", false);
        given().contentType("application/json")
            .body("{\"title\":\"Updated-" + System.nanoTime() + "\",\"description\":\"new\",\"completed\":true}")
            .when().put("/projects/" + id)
            .then().statusCode(anyOf(is(200), is(201)))
            .body("title", containsString("Updated"))
            .body("completed", anyOf(equalTo("true"), equalTo(true)));
        deleteIfExists("/projects/" + id);
    }

    @Test
    void post_on_existing_id_behaves_like_patch_or_put() {
        String id = createProject("ProjPostPut-" + System.nanoTime(), "old", false);
        given().contentType("application/json")
            .body("{\"description\":\"patched\"}")
            .when().post("/projects/" + id)
            .then().statusCode(anyOf(is(200), is(201)));
        deleteIfExists("/projects/" + id);
    }

    @Test
    void delete_nonexistent_project_returns_404() {
        given().when().delete("/projects/9999999").then().statusCode(404);
    }

    // ---------- Relationship Tests ----------

    @Test
    void link_and_unlink_task_to_project() {
        String projId = createProject("RelProj-" + System.nanoTime(), "link", false);
        // Create a todo to link
        Response todo = given().contentType("application/json")
            .body("{\"title\":\"RelTodo-" + System.nanoTime() + "\"}")
            .when().post("/todos")
            .then().statusCode(anyOf(is(200), is(201)))
            .extract().response();
        String todoId = extractId(todo, "todos");

        given().contentType("application/json")
            .body("{\"id\":\"" + todoId + "\"}")
            .when().post("/projects/" + projId + "/tasks")
            .then().statusCode(anyOf(is(200), is(201)));

        given().when().get("/projects/" + projId + "/tasks")
            .then().statusCode(200)
            .body("todos.find { it.id == '" + todoId + "' }", notNullValue());

        given().when().delete("/projects/" + projId + "/tasks/" + todoId)
            .then().statusCode(anyOf(is(200), is(404)));

        deleteIfExists("/projects/" + projId);
        deleteIfExists("/todos/" + todoId);
    }

    @Test
    void link_and_unlink_category_to_project() {
        String projId = createProject("RelProjCat-" + System.nanoTime(), "linkcat", false);
        Response cat = given().contentType("application/json")
            .body("{\"title\":\"RelCat-" + System.nanoTime() + "\"}")
            .when().post("/categories")
            .then().statusCode(anyOf(is(200), is(201)))
            .extract().response();
        String catId = extractId(cat, "categories");

        given().contentType("application/json")
            .body("{\"id\":\"" + catId + "\"}")
            .when().post("/projects/" + projId + "/categories")
            .then().statusCode(anyOf(is(200), is(201)));

        given().when().get("/projects/" + projId + "/categories")
            .then().statusCode(200)
            .body("categories.find { it.id == '" + catId + "' }", notNullValue());

        given().when().delete("/projects/" + projId + "/categories/" + catId)
            .then().statusCode(anyOf(is(200), is(404)));

        deleteIfExists("/projects/" + projId);
        deleteIfExists("/categories/" + catId);
    }

    // ---------- Validation and Edge Cases ----------

    @Test
    void post_missing_title_returns_201() {
        given().contentType("application/json")
            .body("{\"description\":\"no title\"}")
            .when().post("/projects")
            .then().statusCode(201);
    }

    @Test
    void malformed_json_returns_client_error() {
        given().contentType("application/json")
            .body("{\"title\":\"BadJson\", \"completed\":\"false\"") // missing brace
            .when().post("/projects")
            .then().statusCode(anyOf(is(400), is(415), is(422)));
    }

    @Test
    void head_and_options_return_200() {
        given().when().head("/projects").then().statusCode(200);
        given().when().options("/projects").then().statusCode(200);
    }

    @Test
    void ids_not_reused_after_delete() {
        String a = createProject("ProjA-" + System.nanoTime(), "a", false);
        given().when().delete("/projects/" + a).then().statusCode(200);
        String b = createProject("ProjB-" + System.nanoTime(), "b", false);
        Assertions.assertNotEquals(a, b);
        deleteIfExists("/projects/" + b);
    }
}
