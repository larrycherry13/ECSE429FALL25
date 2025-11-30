package com.ecse429.todoapi;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TodoPerformanceTests {

    private static final String BASE = "http://localhost";
    private static final int PORT = 4567;
    private static final int[] OBJECT_COUNTS = { 10, 25, 50, 100, 250 };
    private static final List<PerformanceTestHelper.PerformanceResult> allResults = new ArrayList<>();

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = BASE;
        RestAssured.port = PORT;

        // Verify service is running
        given()
                .when().get("/todos")
                .then().statusCode(anyOf(is(200), is(204)));

        System.out.println("\n========================================");
        System.out.println("Starting Todo Performance Tests");
        System.out.println("========================================\n");
    }

    @AfterEach
    void tearDown() {
        // Clean up after each test
        PerformanceTestHelper.cleanupAllData();
    }

    @AfterAll
    static void writeResults() {
        // Write all results to CSV
        PerformanceTestHelper.writeResultsToCSV("todo_performance_results.csv", allResults);

        System.out.println("\n========================================");
        System.out.println("Todo Performance Tests Complete");
        System.out.println("========================================\n");
    }

    @Test
    @Order(1)
    void testCreateTodoPerformance() {
        System.out.println("\n--- Testing Todo CREATE Performance ---");

        for (int count : OBJECT_COUNTS) {
            PerformanceTestHelper.PerformanceResult result = new PerformanceTestHelper.PerformanceResult("TodoCreate",
                    count, "CREATE");

            System.out.println("Creating " + count + " todos...");

            // Warmup
            String warmupId = PerformanceTestHelper.createRandomTodo();
            PerformanceTestHelper.safeDeleteTodo(warmupId);

            // Measure individual create operations
            List<String> createdIds = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                long timing = PerformanceTestHelper.measureOperation(() -> {
                    String id = PerformanceTestHelper.createRandomTodo();
                    createdIds.add(id);
                });
                result.addTiming(timing);
            }

            PerformanceTestHelper.printResults(result);
            allResults.add(result);

            // Cleanup
            for (String id : createdIds) {
                PerformanceTestHelper.safeDeleteTodo(id);
            }
        }
    }

    @Test
    @Order(2)
    void testUpdateTodoPerformance() {
        System.out.println("\n--- Testing Todo UPDATE Performance ---");

        for (int count : OBJECT_COUNTS) {
            PerformanceTestHelper.PerformanceResult result = new PerformanceTestHelper.PerformanceResult("TodoUpdate",
                    count, "UPDATE");

            System.out.println("Updating " + count + " todos...");

            // Pre-populate with todos
            List<String> todoIds = PerformanceTestHelper.createRandomTodos(count);

            // Warmup
            given()
                    .contentType("application/json")
                    .body("{\"title\":\"Warmup\",\"doneStatus\":true}")
                    .when().post("/todos/" + todoIds.get(0))
                    .then().statusCode(200);

            // Measure update operations
            for (String id : todoIds) {
                long timing = PerformanceTestHelper.measureOperation(() -> {
                    String newTitle = "Updated-" + PerformanceTestHelper.generateRandomString(10);
                    String newDesc = "UpdatedDesc-" + PerformanceTestHelper.generateRandomString(15);
                    boolean newStatus = PerformanceTestHelper.generateRandomBoolean();

                    given()
                            .contentType("application/json")
                            .body(String.format("{\"title\":\"%s\",\"description\":\"%s\",\"doneStatus\":%s}",
                                    newTitle, newDesc, newStatus))
                            .when().post("/todos/" + id)
                            .then().statusCode(200);
                });
                result.addTiming(timing);
            }

            PerformanceTestHelper.printResults(result);
            allResults.add(result);

            // Cleanup
            for (String id : todoIds) {
                PerformanceTestHelper.safeDeleteTodo(id);
            }
        }
    }

    @Test
    @Order(3)
    void testDeleteTodoPerformance() {
        System.out.println("\n--- Testing Todo DELETE Performance ---");

        for (int count : OBJECT_COUNTS) {
            PerformanceTestHelper.PerformanceResult result = new PerformanceTestHelper.PerformanceResult("TodoDelete",
                    count, "DELETE");

            System.out.println("Deleting " + count + " todos...");

            // Pre-populate with todos
            List<String> todoIds = PerformanceTestHelper.createRandomTodos(count);

            // Warmup
            String warmupId = PerformanceTestHelper.createRandomTodo();
            given().when().delete("/todos/" + warmupId).then().statusCode(200);

            // Measure delete operations
            for (String id : todoIds) {
                long timing = PerformanceTestHelper.measureOperation(() -> {
                    given()
                            .when().delete("/todos/" + id)
                            .then().statusCode(200);
                });
                result.addTiming(timing);
            }

            PerformanceTestHelper.printResults(result);
            allResults.add(result);
        }
    }

    @Test
    @Order(4)
    void testTodoCategoryRelationshipPerformance() {
        System.out.println("\n--- Testing Todo-Category Relationship Performance ---");

        for (int count : OBJECT_COUNTS) {
            PerformanceTestHelper.PerformanceResult result = new PerformanceTestHelper.PerformanceResult(
                    "TodoCategoryRelationship", count, "CREATE_RELATIONSHIP");

            System.out.println("Creating " + count + " todo-category relationships...");

            // Pre-populate with todos and categories
            List<String> todoIds = PerformanceTestHelper.createRandomTodos(count);
            List<String> categoryIds = PerformanceTestHelper.createRandomCategories(count);

            // Warmup
            PerformanceTestHelper.createTodoCategoryRelationship(todoIds.get(0), categoryIds.get(0));

            // Measure relationship creation
            for (int i = 0; i < count; i++) {
                String todoId = todoIds.get(i);
                String categoryId = categoryIds.get(i);

                long timing = PerformanceTestHelper.measureOperation(() -> {
                    PerformanceTestHelper.createTodoCategoryRelationship(todoId, categoryId);
                });
                result.addTiming(timing);
            }

            PerformanceTestHelper.printResults(result);
            allResults.add(result);

            // Cleanup
            for (String id : todoIds) {
                PerformanceTestHelper.safeDeleteTodo(id);
            }
            for (String id : categoryIds) {
                PerformanceTestHelper.safeDeleteCategory(id);
            }
        }
    }

    @Test
    @Order(5)
    void testTodoProjectRelationshipPerformance() {
        System.out.println("\n--- Testing Todo-Project Relationship Performance ---");

        for (int count : OBJECT_COUNTS) {
            PerformanceTestHelper.PerformanceResult result = new PerformanceTestHelper.PerformanceResult(
                    "TodoProjectRelationship", count, "CREATE_RELATIONSHIP");

            System.out.println("Creating " + count + " todo-project relationships...");

            // Pre-populate with todos and projects
            List<String> todoIds = PerformanceTestHelper.createRandomTodos(count);
            List<String> projectIds = PerformanceTestHelper.createRandomProjects(count);

            // Warmup
            PerformanceTestHelper.createTodoProjectRelationship(todoIds.get(0), projectIds.get(0));

            // Measure relationship creation
            for (int i = 0; i < count; i++) {
                String todoId = todoIds.get(i);
                String projectId = projectIds.get(i);

                long timing = PerformanceTestHelper.measureOperation(() -> {
                    PerformanceTestHelper.createTodoProjectRelationship(todoId, projectId);
                });
                result.addTiming(timing);
            }

            PerformanceTestHelper.printResults(result);
            allResults.add(result);

            // Cleanup
            for (String id : todoIds) {
                PerformanceTestHelper.safeDeleteTodo(id);
            }
            for (String id : projectIds) {
                PerformanceTestHelper.safeDeleteProject(id);
            }
        }
    }
}
