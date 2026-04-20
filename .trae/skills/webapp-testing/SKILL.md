---
name: "webapp-testing"
description: "Performs comprehensive web application testing including API testing, UI testing, and deployment validation. Invoke when user needs to test web applications, validate deployments, or troubleshoot issues."
---

# Web Application Testing Skill

This skill provides comprehensive testing capabilities for web applications, with a focus on full-stack applications like your campus marketplace platform.

## When to Use This Skill

Invoke this skill when:
- Testing API endpoints and validating responses
- Performing UI/UX testing on frontend components
- Validating deployment configurations
- Troubleshooting deployment issues
- Testing database connections and migrations
- Performing end-to-end user flow testing
- Checking application health and performance

## Testing Capabilities

### 1. API Testing
- **REST API Testing**: Test all REST endpoints with proper authentication
- **Response Validation**: Validate JSON responses, status codes, and error handling
- **Authentication Testing**: Test JWT token flows, login/logout functionality
- **Data Validation**: Test CRUD operations for all entities (users, products, orders, etc.)

### 2. Frontend Testing
- **Component Testing**: Test Vue.js components for proper rendering and interaction
- **Routing Testing**: Validate Vue Router navigation and route guards
- **State Management**: Test Vuex/Pinia state updates and reactivity
- **UI/UX Validation**: Check responsive design, accessibility, and user interactions

### 3. Database Testing
- **Connection Testing**: Validate MySQL database connections
- **Migration Testing**: Test database schema migrations and data integrity
- **Query Performance**: Analyze and optimize database queries
- **Transaction Testing**: Test ACID properties and rollback scenarios

### 4. Deployment Testing
- **Environment Validation**: Test environment variables and configuration
- **Health Check Testing**: Validate /actuator/health endpoint responses
- **CORS Testing**: Test cross-origin resource sharing configurations
- **Security Testing**: Validate security headers and authentication mechanisms

### 5. Integration Testing
- **End-to-End Flows**: Test complete user journeys (registration → login → product listing → purchase)
- **Third-Party Integration**: Test integrations with external services
- **File Upload Testing**: Test image upload and storage functionality
- **Real-time Features**: Test WebSocket or real-time communication if applicable

## Testing Tools and Commands

### Backend Testing (Spring Boot)
```bash
# Run backend tests
cd env-setup/backend
./mvnw test

# Run specific test class
./mvnw test -Dtest=UserServiceTest

# Run tests with coverage
./mvnw test jacoco:report

# Integration tests
./mvnw verify
```

### Frontend Testing (Vue.js)
```bash
# Run frontend tests
cd env-setup/frontend
npm test

# Run tests in watch mode
npm run test:watch

# Run specific test file
npm test -- --testPathPattern=ProductList.test.js

# Run e2e tests (if configured)
npm run test:e2e
```

### Database Testing
```bash
# Test database connection
mysql -h localhost -u root -p -e "SELECT 1"

# Check database schema
mysql -h localhost -u root -p campus_market -e "SHOW TABLES"

# Test specific queries
mysql -h localhost -u root -p campus_market -e "SELECT COUNT(*) FROM users"
```

### Deployment Testing
```bash
# Test application startup
cd env-setup/backend
./mvnw spring-boot:run

# Test health endpoint
curl http://localhost:8080/actuator/health

# Test API endpoints
curl -H "Authorization: Bearer <token>" http://localhost:8080/api/users/me

# Test frontend build
cd env-setup/frontend
npm run build
```

## Common Test Scenarios

### Scenario 1: User Registration Flow
1. Test registration endpoint with valid data
2. Verify user is created in database
3. Test duplicate email/username handling
4. Verify email confirmation (if implemented)
5. Test login with newly created credentials

### Scenario 2: Product Listing and Purchase
1. Test product listing API
2. Test product search and filtering
3. Test product detail view
4. Test add to cart functionality
5. Test checkout process
6. Test order creation and status updates

### Scenario 3: Deployment Validation
1. Test environment variable loading
2. Test database connection on startup
3. Verify health check endpoint returns UP
4. Test API endpoints with authentication
5. Verify static assets are served correctly

## Troubleshooting Guide

### Common Issues and Solutions

#### Issue 1: Database Connection Failed
**Symptoms**: Application fails to start, "Cannot connect to database" errors
**Solutions**:
- Check database URL and credentials
- Verify database service is running
- Check network connectivity and firewall rules
- Test connection manually with mysql command

#### Issue 2: API Endpoints Return 404
**Symptoms**: Frontend cannot connect to backend APIs
**Solutions**:
- Verify backend is running on correct port
- Check CORS configuration
- Verify API route mappings
- Check authentication requirements

#### Issue 3: Frontend Build Errors
**Symptoms**: npm run build fails with compilation errors
**Solutions**:
- Check for syntax errors in Vue components
- Verify all dependencies are installed
- Check import statements and file paths
- Review webpack/vite configuration

#### Issue 4: Health Check Fails
**Symptoms**: /actuator/health returns DOWN status
**Solutions**:
- Check database connectivity
- Verify Redis connection (if configured)
- Check disk space and memory usage
- Review application logs for errors

## Test Data Management

### Sample Test Data
```json
{
  "user": {
    "username": "testuser",
    "email": "test@example.com",
    "password": "Test123!",
    "phone": "13800138000"
  },
  "product": {
    "title": "Test Product",
    "description": "This is a test product",
    "price": 99.99,
    "category": "electronics",
    "condition": "new"
  },
  "order": {
    "productId": 1,
    "quantity": 1,
    "shippingAddress": "Test Address"
  }
}
```

### Test Data Cleanup
Always clean up test data after testing to maintain database integrity:
```sql
-- Clean test data
DELETE FROM orders WHERE user_id IN (SELECT id FROM users WHERE email LIKE '%test%');
DELETE FROM products WHERE user_id IN (SELECT id FROM users WHERE email LIKE '%test%');
DELETE FROM users WHERE email LIKE '%test%';
```

## Best Practices

1. **Isolate Tests**: Each test should be independent and not rely on other tests
2. **Clean Up**: Always clean up test data after tests complete
3. **Mock External Services**: Use mocks for external APIs and services
4. **Test Edge Cases**: Test boundary conditions and error scenarios
5. **Performance Testing**: Include performance tests for critical paths
6. **Security Testing**: Test authentication, authorization, and input validation
7. **Documentation**: Document test cases and expected behaviors

## Integration with CI/CD

### GitHub Actions Example
```yaml
name: Web Application Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    
    services:
      mysql:
        image: mysql:8.0
        env:
          MYSQL_ROOT_PASSWORD: root
          MYSQL_DATABASE: campus_market_test
        ports:
          - 3306:3306
        options: >-
          --health-cmd="mysqladmin ping"
          --health-interval=10s
          --health-timeout=5s
          --health-retries=3
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up Java
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
    
    - name: Set up Node.js
      uses: actions/setup-node@v3
      with:
        node-version: '18'
    
    - name: Run backend tests
      run: |
        cd env-setup/backend
        ./mvnw test
    
    - name: Run frontend tests
      run: |
        cd env-setup/frontend
        npm ci
        npm test
```

This skill will help ensure your web application is thoroughly tested and ready for production deployment.