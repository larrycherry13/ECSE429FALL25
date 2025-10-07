# Todo Manager 

This project contains a comprehensive test suite for the Todo REST API (v1.5.5) as part of ECSE-429 Project Part A. The test suite validates CRUD operations, relationships, and API behavior across todos, categories, and projects.

## Project Structure

```
unit-tests/
├── README.md
├── pom.xml # Maven configuration
├── src/
    └── test/
        └── java/
            └── com/
                └── ecse429/
                    └── todoapi/
                        ├── CategoryTests.java # Category CRUD & relationship tests
                        ├── InteroperabilityTests.java # Cross-entity relationship tests
                        ├── ProjectUnitTests.java # Project CRUD & relationship tests
                        ├── TestHelper.java # Helper methods common to all unit tests.
                        └── TodoUnitTests.java # Todo CRUD & relationship tests
```
## How to run

1. Install Java 25  
 - https://www.oracle.com/ca-en/java/technologies/downloads/

2. Install Apache Maven 3.9
 - https://maven.apache.org/install.html

3. Run TodoManagerTestAPI-1.5.5.jar
   - The server must be running on `http://localhost:4567`

4. Run all tests
 - From this current directory, run `mvn test`