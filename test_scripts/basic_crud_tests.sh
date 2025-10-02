#!/bin/bash

# Basic CRUD Tests for Todo Manager REST API
# ECSE-429 Project - Exploratory Testing Scripts

BASE_URL="http://localhost:4567"
echo "=== Basic CRUD Tests for Todo Manager API ==="
echo "Base URL: $BASE_URL"
echo ""

# Test 1: Create a new todo
echo "1. Creating a new todo..."
TODO_RESPONSE=$(curl -s -X POST $BASE_URL/todos \
  -H "Content-Type: application/json" \
  -d '{"title":"Test Todo","doneStatus":false,"description":"Test description"}')

echo "Response: $TODO_RESPONSE"
TODO_ID=$(echo $TODO_RESPONSE | grep -o '"id":"[^"]*"' | cut -d'"' -f4)
echo "Created Todo ID: $TODO_ID"
echo ""

# Test 2: Get all todos
echo "2. Getting all todos..."
curl -s -X GET $BASE_URL/todos | jq '.' 2>/dev/null || curl -s -X GET $BASE_URL/todos
echo ""
echo ""

# Test 3: Get specific todo
echo "3. Getting specific todo (ID: $TODO_ID)..."
curl -s -X GET $BASE_URL/todos/$TODO_ID | jq '.' 2>/dev/null || curl -s -X GET $BASE_URL/todos/$TODO_ID
echo ""
echo ""

# Test 4: Update todo (PUT - replace)
echo "4. Updating todo (PUT - replace)..."
curl -s -X PUT $BASE_URL/todos/$TODO_ID \
  -H "Content-Type: application/json" \
  -d '{"title":"Updated Todo","doneStatus":true,"description":"Updated description"}' | jq '.' 2>/dev/null || curl -s -X PUT $BASE_URL/todos/$TODO_ID \
  -H "Content-Type: application/json" \
  -d '{"title":"Updated Todo","doneStatus":true,"description":"Updated description"}'
echo ""
echo ""

# Test 5: Partial update (POST - amend)
echo "5. Partial update (POST - amend)..."
curl -s -X POST $BASE_URL/todos/$TODO_ID \
  -H "Content-Type: application/json" \
  -d '{"description":"Amended description"}' | jq '.' 2>/dev/null || curl -s -X POST $BASE_URL/todos/$TODO_ID \
  -H "Content-Type: application/json" \
  -d '{"description":"Amended description"}'
echo ""
echo ""

# Test 6: Get todo after updates
echo "6. Getting todo after updates..."
curl -s -X GET $BASE_URL/todos/$TODO_ID | jq '.' 2>/dev/null || curl -s -X GET $BASE_URL/todos/$TODO_ID
echo ""
echo ""

# Test 7: Test HEAD request
echo "7. Testing HEAD request..."
curl -s -I $BASE_URL/todos/$TODO_ID
echo ""
echo ""

# Test 8: Test OPTIONS request
echo "8. Testing OPTIONS request..."
curl -s -X OPTIONS $BASE_URL/todos/$TODO_ID
echo ""
echo ""

# Test 9: Delete todo
echo "9. Deleting todo..."
curl -s -X DELETE $BASE_URL/todos/$TODO_ID
echo ""
echo ""

# Test 10: Verify deletion
echo "10. Verifying deletion (should return 404)..."
curl -s -X GET $BASE_URL/todos/$TODO_ID
echo ""
echo ""

echo "=== Basic CRUD Tests Completed ==="