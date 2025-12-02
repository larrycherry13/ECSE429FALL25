package com.ecse429.todoapi;

import io.restassured.RestAssured;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RelationshipPerformanceTests {

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
        System.out.println("Starting Relationship Performance Tests");
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
        PerformanceTestHelper.writeResultsToCSV("relationship_performance_results.csv", allResults);

        System.out.println("\n========================================");
        System.out.println("Relationship Performance Tests Complete");
        System.out.println("========================================\n");
    }

    @Test
    @Order(1)
    void testDeleteTodoCategoryRelationshipPerformance() {
        System.out.println("\n--- Testing Todo-Category Relationship DELETE Performance ---");

        for (int count : OBJECT_COUNTS) {
            PerformanceTestHelper.PerformanceResult result = new PerformanceTestHelper.PerformanceResult(
                    "TodoCategoryRelationshipDelete", count, "DELETE_RELATIONSHIP");

            System.out.println("Deleting " + count + " todo-category relationships...");

            // Pre-populate with todos, categories, and relationships
            List<String> todoIds = PerformanceTestHelper.createRandomTodos(count);
            List<String> categoryIds = PerformanceTestHelper.createRandomCategories(count);

            // Create relationships
            for (int i = 0; i < count; i++) {
                PerformanceTestHelper.createTodoCategoryRelationship(todoIds.get(i), categoryIds.get(i));
            }

            // Warmup
            String warmupTodo = PerformanceTestHelper.createRandomTodo();
            String warmupCat = PerformanceTestHelper.createRandomCategory();
            PerformanceTestHelper.createTodoCategoryRelationship(warmupTodo, warmupCat);
            given().when().delete("/todos/" + warmupTodo + "/categories/" + warmupCat)
                    .then().statusCode(anyOf(is(200), is(404)));
            PerformanceTestHelper.safeDeleteTodo(warmupTodo);
            PerformanceTestHelper.safeDeleteCategory(warmupCat);

            // Measure relationship deletion
            for (int i = 0; i < count; i++) {
                String todoId = todoIds.get(i);
                String categoryId = categoryIds.get(i);

                PerformanceTestHelper.OperationMetrics metrics = PerformanceTestHelper.measureOperationWithMetrics(() -> {
                    given()
                            .when().delete("/todos/" + todoId + "/categories/" + categoryId)
                            .then().statusCode(anyOf(is(200), is(404)));
                });
                result.addTiming(metrics.durationNanos, metrics.cpuPercent, metrics.memoryMB);
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
    @Order(2)
    void testDeleteTodoProjectRelationshipPerformance() {
        System.out.println("\n--- Testing Todo-Project Relationship DELETE Performance ---");

        for (int count : OBJECT_COUNTS) {
            PerformanceTestHelper.PerformanceResult result = new PerformanceTestHelper.PerformanceResult(
                    "TodoProjectRelationshipDelete", count, "DELETE_RELATIONSHIP");

            System.out.println("Deleting " + count + " todo-project relationships...");

            // Pre-populate with todos, projects, and relationships
            List<String> todoIds = PerformanceTestHelper.createRandomTodos(count);
            List<String> projectIds = PerformanceTestHelper.createRandomProjects(count);

            // Create relationships
            for (int i = 0; i < count; i++) {
                PerformanceTestHelper.createTodoProjectRelationship(todoIds.get(i), projectIds.get(i));
            }

            // Warmup
            String warmupTodo = PerformanceTestHelper.createRandomTodo();
            String warmupProj = PerformanceTestHelper.createRandomProject();
            PerformanceTestHelper.createTodoProjectRelationship(warmupTodo, warmupProj);
            given().when().delete("/todos/" + warmupTodo + "/tasksof/" + warmupProj)
                    .then().statusCode(anyOf(is(200), is(404)));
            PerformanceTestHelper.safeDeleteTodo(warmupTodo);
            PerformanceTestHelper.safeDeleteProject(warmupProj);

            // Measure relationship deletion
            for (int i = 0; i < count; i++) {
                String todoId = todoIds.get(i);
                String projectId = projectIds.get(i);

                PerformanceTestHelper.OperationMetrics metrics = PerformanceTestHelper.measureOperationWithMetrics(() -> {
                    given()
                            .when().delete("/todos/" + todoId + "/tasksof/" + projectId)
                            .then().statusCode(anyOf(is(200), is(404)));
                });
                result.addTiming(metrics.durationNanos, metrics.cpuPercent, metrics.memoryMB);
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

    @Test
    @Order(3)
    void testComplexScenarioPerformance() {
        System.out.println("\n--- Testing Complex Scenario Performance ---");
        System.out.println("Creating todos, categories, projects, and all relationships");

        for (int count : OBJECT_COUNTS) {
            PerformanceTestHelper.PerformanceResult result = new PerformanceTestHelper.PerformanceResult(
                    "ComplexScenario", count, "COMPLEX");

            System.out.println("Creating complex scenario with " + count + " objects...");

            // Warmup
            String wTodo = PerformanceTestHelper.createRandomTodo();
            String wCat = PerformanceTestHelper.createRandomCategory();
            String wProj = PerformanceTestHelper.createRandomProject();
            PerformanceTestHelper.createTodoCategoryRelationship(wTodo, wCat);
            PerformanceTestHelper.createTodoProjectRelationship(wTodo, wProj);
            PerformanceTestHelper.safeDeleteTodo(wTodo);
            PerformanceTestHelper.safeDeleteCategory(wCat);
            PerformanceTestHelper.safeDeleteProject(wProj);

            // Measure entire complex scenario
            PerformanceTestHelper.OperationMetrics metrics = PerformanceTestHelper.measureOperationWithMetrics(() -> {
                // Create all objects
                List<String> todoIds = PerformanceTestHelper.createRandomTodos(count);
                List<String> categoryIds = PerformanceTestHelper.createRandomCategories(count);
                List<String> projectIds = PerformanceTestHelper.createRandomProjects(count);

                // Create all relationships
                for (int i = 0; i < count; i++) {
                    PerformanceTestHelper.createTodoCategoryRelationship(todoIds.get(i), categoryIds.get(i));
                    PerformanceTestHelper.createTodoProjectRelationship(todoIds.get(i), projectIds.get(i));
                }
            });

            result.addTiming(metrics.durationNanos, metrics.cpuPercent, metrics.memoryMB);

            PerformanceTestHelper.printResults(result);
            allResults.add(result);

            // Cleanup is handled by tearDown
        }
    }

    @Test
    @Order(4)
    void testMultipleRelationshipsPerTodoPerformance() {
        System.out.println("\n--- Testing Multiple Relationships Per Todo Performance ---");
        System.out.println("Each todo will be linked to multiple categories and projects");

        int[] relationshipCounts = { 5, 10, 25, 50 };

        for (int relCount : relationshipCounts) {
            PerformanceTestHelper.PerformanceResult result = new PerformanceTestHelper.PerformanceResult(
                    "MultipleRelationshipsPerTodo", relCount, "CREATE_RELATIONSHIP");

            System.out.println("Creating " + relCount + " relationships per todo...");

            // Create one todo and multiple categories/projects
            String todoId = PerformanceTestHelper.createRandomTodo();
            List<String> categoryIds = PerformanceTestHelper.createRandomCategories(relCount);
            List<String> projectIds = PerformanceTestHelper.createRandomProjects(relCount);

            // Warmup
            String warmupCat = PerformanceTestHelper.createRandomCategory();
            PerformanceTestHelper.createTodoCategoryRelationship(todoId, warmupCat);
            PerformanceTestHelper.safeDeleteCategory(warmupCat);

            // Measure creating multiple relationships to the same todo
            for (int i = 0; i < relCount; i++) {
                final int index = i; // Make effectively final for lambda
                PerformanceTestHelper.OperationMetrics catMetrics = PerformanceTestHelper.measureOperationWithMetrics(() -> {
                    PerformanceTestHelper.createTodoCategoryRelationship(todoId, categoryIds.get(index));
                });
                result.addTiming(catMetrics.durationNanos, catMetrics.cpuPercent, catMetrics.memoryMB);

                PerformanceTestHelper.OperationMetrics projMetrics = PerformanceTestHelper.measureOperationWithMetrics(() -> {
                    PerformanceTestHelper.createTodoProjectRelationship(todoId, projectIds.get(index));
                });
                result.addTiming(projMetrics.durationNanos, projMetrics.cpuPercent, projMetrics.memoryMB);
            }

            PerformanceTestHelper.printResults(result);
            allResults.add(result);

            // Cleanup
            PerformanceTestHelper.safeDeleteTodo(todoId);
            for (String id : categoryIds) {
                PerformanceTestHelper.safeDeleteCategory(id);
            }
            for (String id : projectIds) {
                PerformanceTestHelper.safeDeleteProject(id);
            }
        }
    }
}
