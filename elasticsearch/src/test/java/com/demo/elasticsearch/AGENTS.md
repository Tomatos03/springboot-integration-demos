# AGENTS.md

## Elasticsearch Module Tests

**Prerequisite**: Elasticsearch must be running before executing tests.

```bash
# Start ES (from module directory)
cd elasticsearch/docs/docker && docker-compose up -d

# Initialize test data (required)
curl -X POST "http://localhost:8083/api/es/client/products/init"
```

## Run Tests

```bash
# Run all tests in the module
mvn test -pl elasticsearch

# Run specific test class
mvn test -pl elasticsearch -Dtest=AutocompleteServiceTest
```

## Test Notes

- `@SpringBootTest` requires full application context (ES must be accessible)
- Tests may fail if sample data is not initialized
- Port 8083 must be available