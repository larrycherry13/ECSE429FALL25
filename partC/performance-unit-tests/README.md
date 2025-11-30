# Performance Testing Suite

This project contains a comprehensive performance testing suite for the Todo REST API (v1.5.5) as part of ECSE-429 Project Part C. The test suite measures the time required to create, update, and delete objects (todos, categories, projects) and their relationships as the number of objects increases.

## Project Structure

```
performance-unit-tests/
├── README.md
├── pom.xml # Maven configuration
├── src/
    └── test/
        └── java/
            └── com/
                └── ecse429/
                    └── todoapi/
                        ├── PerformanceTestHelper.java # Performance testing utilities
                        ├── TodoPerformanceTests.java # Todo CRUD performance tests
                        ├── CategoryPerformanceTests.java # Category CRUD performance tests
                        ├── ProjectPerformanceTests.java # Project CRUD performance tests
                        ├── RelationshipPerformanceTests.java # Relationship performance tests
                        └── TestHelper.java # Helper methods common to all tests
```

## Performance Tests

The suite includes the following performance tests:

### Todo Performance Tests
- **Create Performance**: Measures time to create N todos (10, 50, 100, 250, 500, 1000)
- **Update Performance**: Measures time to update existing todos
- **Delete Performance**: Measures time to delete todos
- **Relationship Performance**: Measures time to create todo-category and todo-project relationships

### Category Performance Tests
- **Create Performance**: Measures time to create N categories
- **Update Performance**: Measures time to update existing categories
- **Delete Performance**: Measures time to delete categories

### Project Performance Tests
- **Create Performance**: Measures time to create N projects
- **Update Performance**: Measures time to update existing projects
- **Delete Performance**: Measures time to delete projects

### Relationship Performance Tests
- **Delete Relationships**: Measures time to delete todo-category and todo-project relationships
- **Complex Scenarios**: Measures time to create complete object graphs with all relationships
- **Multiple Relationships**: Measures time to create multiple relationships per todo

## How to Run

1. Install Java 11 or higher
 - https://www.oracle.com/ca-en/java/technologies/downloads/

2. Install Apache Maven 3.9
 - https://maven.apache.org/install.html

3. Run TodoManagerTestAPI-1.5.5.jar
   - The server must be running on `http://localhost:4567`

4. Run all performance tests
 - From this current directory, run `mvn test`

## Results

Performance results are automatically written to CSV files in `target/performance-results/`:
- `todo_performance_results.csv`
- `category_performance_results.csv`
- `project_performance_results.csv`
- `relationship_performance_results.csv`

Each CSV file contains:
- Test Name
- Object Count
- Operation Type (CREATE/UPDATE/DELETE)
- Average Time (ms)
- Min Time (ms)
- Max Time (ms)
- Median Time (ms)
- Total Time (ms)
- Sample Count
- Timestamp