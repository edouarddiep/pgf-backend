#!/bin/bash

# Script de test des endpoints PGF Backend
# Usage: chmod +x test-endpoints.sh && ./test-endpoints.sh

BASE_URL="http://localhost:8080"
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}=== Test des endpoints PGF Backend ===${NC}"
echo "Base URL: $BASE_URL"
echo ""

# Function to test GET endpoint
test_get() {
    local endpoint=$1
    local description=$2

    echo -e "${YELLOW}Testing GET $endpoint - $description${NC}"
    response=$(curl -s -w "\n%{http_code}" "$BASE_URL$endpoint")
    status_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | head -n -1)

    if [ "$status_code" -eq 200 ]; then
        echo -e "${GREEN}✓ SUCCESS (200)${NC}"
        echo "Response preview: $(echo "$body" | jq -r '. | if type == "array" then "Array with \(length) items" else .message // .title // .name // "Object" end' 2>/dev/null || echo "Response received")"
    else
        echo -e "${RED}✗ FAILED ($status_code)${NC}"
        echo "Response: $body"
    fi
    echo ""
}

# Function to test POST endpoint
test_post() {
    local endpoint=$1
    local description=$2
    local data=$3

    echo -e "${YELLOW}Testing POST $endpoint - $description${NC}"
    response=$(curl -s -w "\n%{http_code}" -X POST -H "Content-Type: application/json" -d "$data" "$BASE_URL$endpoint")
    status_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | head -n -1)

    if [ "$status_code" -eq 201 ] || [ "$status_code" -eq 200 ]; then
        echo -e "${GREEN}✓ SUCCESS ($status_code)${NC}"
        echo "Response preview: $(echo "$body" | jq -r '.id // .message // "Created successfully"' 2>/dev/null || echo "Created successfully")"
    else
        echo -e "${RED}✗ FAILED ($status_code)${NC}"
        echo "Response: $body"
    fi
    echo ""
}

# Check if server is running
echo -e "${YELLOW}Checking if server is running...${NC}"
if curl -s "$BASE_URL/actuator/health" > /dev/null 2>&1 || curl -s "$BASE_URL/api/categories" > /dev/null 2>&1; then
    echo -e "${GREEN}✓ Server is running${NC}"
else
    echo -e "${RED}✗ Server is not running at $BASE_URL${NC}"
    echo "Please start the server with: mvn spring-boot:run"
    exit 1
fi
echo ""

# Test Categories endpoints
echo -e "${YELLOW}=== CATEGORIES ENDPOINTS ===${NC}"
test_get "/api/categories" "Get all categories"
test_get "/api/categories/1" "Get category by ID"
test_get "/api/categories/slug/fils-de-fer" "Get category by slug"

# Test creating a category
test_post "/api/categories" "Create new category" '{
    "name": "Test Category API",
    "description": "Created via API test",
    "slug": "test-category-api",
    "displayOrder": 99
}'

# Test Artworks endpoints
echo -e "${YELLOW}=== ARTWORKS ENDPOINTS ===${NC}"
test_get "/api/artworks" "Get all artworks"
test_get "/api/artworks/category/1" "Get artworks by category ID"
test_get "/api/artworks/category/slug/fils-de-fer" "Get artworks by category slug"
test_get "/api/artworks/available" "Get available artworks"

# Test creating an artwork
test_post "/api/artworks" "Create new artwork" '{
    "title": "Test Artwork API",
    "description": "Created via API test",
    "dimensions": "50x70cm",
    "materials": "Test materials",
    "price": 750.00,
    "isAvailable": true,
    "categoryId": 1
}'

# Test Exhibitions endpoints
echo -e "${YELLOW}=== EXHIBITIONS ENDPOINTS ===${NC}"
test_get "/api/exhibitions" "Get all exhibitions"
test_get "/api/exhibitions/upcoming" "Get upcoming exhibitions"
test_get "/api/exhibitions/past" "Get past exhibitions"
test_get "/api/exhibitions/ongoing" "Get ongoing exhibitions"
test_get "/api/exhibitions/next-featured" "Get next featured exhibition"

# Test creating an exhibition
test_post "/api/exhibitions" "Create new exhibition" '{
    "title": "Test Exhibition API",
    "description": "Created via API test",
    "location": "Test Gallery",
    "startDate": "2025-10-01",
    "endDate": "2025-10-31",
    "isFeatured": true,
    "status": "UPCOMING"
}'

# Test Contact endpoints
echo -e "${YELLOW}=== CONTACT ENDPOINTS ===${NC}"
test_get "/api/contact/messages" "Get all messages"
test_get "/api/contact/messages/unread" "Get unread messages"
test_get "/api/contact/messages/count-unread" "Get unread count"

# Test sending a contact message
test_post "/api/contact" "Send contact message" '{
    "name": "Test User API",
    "email": "test@example.com",
    "phone": "0123456789",
    "subject": "API Test Message",
    "message": "This is a test message sent via API testing script."
}'

# Test Images upload simulation (without actual file)
echo -e "${YELLOW}=== IMAGES ENDPOINTS ===${NC}"
echo -e "${YELLOW}Testing image endpoints info (file upload requires multipart)${NC}"
echo "Available endpoints:"
echo "- POST /api/upload/image (requires multipart file)"
echo "- POST /api/upload/image-with-thumbnail (requires multipart file)"
echo "- GET /api/images/{category}/{filename}"
echo "- DELETE /api/images?imageUrl={url}"
echo "- GET /api/images/exists?imageUrl={url}"
echo ""

# Test API Documentation
echo -e "${YELLOW}=== API DOCUMENTATION ===${NC}"
test_get "/v3/api-docs" "OpenAPI documentation JSON"
echo "Swagger UI available at: $BASE_URL/swagger-ui.html"
echo ""

# Summary
echo -e "${GREEN}=== TEST COMPLETED ===${NC}"
echo "All endpoints have been tested. Check results above."
echo "For detailed API documentation, visit: $BASE_URL/swagger-ui.html"
echo ""
echo "To test file uploads manually:"
echo "curl -X POST -F 'file=@/path/to/image.jpg' -F 'category=test' $BASE_URL/api/upload/image"