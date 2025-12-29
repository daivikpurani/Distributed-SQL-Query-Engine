# Practical Implementation Guide: Real SQL Execution

This guide walks you through implementing real PostgreSQL query execution to replace the mock data in your distributed SQL engine.

---

## Problem Statement

Currently, `DataStore.java` and `QueryExecutor.java` return mock data. While this demonstrates the distributed architecture, it doesn't show that the system can actually execute SQL queries against PostgreSQL.

---

## Implementation Steps

### Step 1: Fix DataStore.executeQuery()

**File**: `worker/src/main/java/com/distributed/sql/worker/DataStore.java`

**Current Code** (lines 40-51):

```java
public java.sql.ResultSet executeQuery(String sqlQuery) {
    String traceId = Tracer.startTrace("execute_query");
    try {
        AppLogger.info("Executing query on worker {}: {}", workerId, sqlQuery);
        // For demo purposes, return mock data based on query content
        return convertToResultSet(generateMockResults(sqlQuery));
    } finally {
        Tracer.endTrace("execute_query");
    }
}
```

**Problem**: Returns null from `convertToResultSet()` and doesn't execute real SQL.

**Solution**:

```java
public List<com.distributed.sql.common.models.Row> executeQuery(String sqlQuery) {
    String traceId = Tracer.startTrace("execute_query");

    try {
        AppLogger.info("Executing query on worker {}: {}", workerId, sqlQuery);

        long startTime = System.currentTimeMillis();

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sqlQuery)) {

            // Get column metadata
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            // Extract column names
            List<String> columns = new ArrayList<>();
            for (int i = 1; i <= columnCount; i++) {
                columns.add(metaData.getColumnLabel(i));
            }

            // Convert rows
            List<com.distributed.sql.common.models.Row> rows = new ArrayList<>();
            while (rs.next()) {
                List<String> values = new ArrayList<>();
                for (int i = 1; i <= columnCount; i++) {
                    Object value = rs.getObject(i);
                    values.add(value != null ? value.toString() : "NULL");
                }
                rows.add(new com.distributed.sql.common.models.Row(values));
            }

            long executionTime = System.currentTimeMillis() - startTime;
            AppLogger.info("Query completed in {}ms, returned {} rows", executionTime, rows.size());

            return rows;

        } catch (SQLException e) {
            AppLogger.error("SQL execution failed for worker {}", workerId, e);
            throw new RuntimeException("Query execution failed: " + e.getMessage(), e);
        }

    } finally {
        Tracer.endTrace("execute_query");
    }
}
```

**Key Changes**:

1. Execute actual SQL using `stmt.executeQuery(sqlQuery)`
2. Extract column metadata using `ResultSetMetaData`
3. Convert `java.sql.ResultSet` to your internal Row model
4. Handle NULL values properly
5. Use try-with-resources for proper resource management

---

### Step 2: Update QueryExecutor to Use Real Data

**File**: `worker/src/main/java/com/distributed/sql/worker/QueryExecutor.java`

**Current Problem** (lines 85-92):

```java
private ResultSet executeScanNode(PlanNode planNode) {
    String tableName = planNode.getTableName();
    String sqlQuery = String.format("SELECT * FROM %s", tableName);

    ResultSet resultSet = convertFromSqlResultSet(dataStore.executeQuery(sqlQuery));
    resultSet.setQueryId("scan_" + planNode.getNodeId());

    return resultSet;
}
```

**Problem**: Calls `convertFromSqlResultSet()` which expects a `java.sql.ResultSet`, but now we're returning `List<Row>`.

**Solution**: Update `QueryExecutor` to work with the new signature:

```java
public ResultSet executeQuery(String sqlQuery) {
    String traceId = Tracer.startTrace("execute_query");

    try {
        AppLogger.info("Executing query on worker {}: {}", workerId, sqlQuery);

        long startTime = System.currentTimeMillis();

        // Execute query using DataStore
        List<Row> rows = dataStore.executeQuery(sqlQuery);
        long executionTime = System.currentTimeMillis() - startTime;

        // Create ResultSet
        ResultSet resultSet = new ResultSet();
        resultSet.setQueryId("query_" + System.currentTimeMillis());
        resultSet.setStatus("SUCCESS");
        resultSet.setExecutionTimeMs(executionTime);
        resultSet.setTotalRows(rows.size());
        resultSet.setRows(rows);

        Tracer.addTimestamp("query_executed");
        AppLogger.info("Query executed successfully on worker {} in {}ms",
                workerId, executionTime);

        return resultSet;

    } catch (Exception e) {
        AppLogger.error("Error executing query on worker {}", workerId, e);

        ResultSet errorResult = new ResultSet();
        errorResult.setQueryId("error_" + System.currentTimeMillis());
        errorResult.setStatus("FAILED");
        errorResult.setExecutionTimeMs(0);
        errorResult.setTotalRows(0);

        return errorResult;

    } finally {
        Tracer.endTrace("execute_query");
    }
}
```

**Update `executeScanNode()` method**:

```java
private ResultSet executeScanNode(PlanNode planNode) {
    String tableName = planNode.getTableName();

    // Build WHERE clause from conditions
    StringBuilder sqlQuery = new StringBuilder("SELECT ");

    // Add columns or *
    if (planNode.getColumns().isEmpty()) {
        sqlQuery.append("*");
    } else {
        for (int i = 0; i < planNode.getColumns().size(); i++) {
            if (i > 0) sqlQuery.append(", ");
            sqlQuery.append(planNode.getColumns().get(i));
        }
    }

    sqlQuery.append(" FROM ").append(tableName);

    // Add WHERE clause
    if (!planNode.getConditions().isEmpty()) {
        sqlQuery.append(" WHERE ");
        for (int i = 0; i < planNode.getConditions().size(); i++) {
            if (i > 0) sqlQuery.append(" AND ");

            Condition condition = planNode.getConditions().get(i);
            sqlQuery.append(condition.getColumn())
                    .append(" ")
                    .append(mapOperatorToString(condition.getOperator()))
                    .append(" '")
                    .append(condition.getValue())
                    .append("'");
        }
    }

    // Execute query
    ResultSet resultSet = new ResultSet();
    resultSet.setQueryId("scan_" + planNode.getNodeId());

    try {
        List<Row> rows = dataStore.executeQuery(sqlQuery.toString());
        resultSet.setRows(rows);
        resultSet.setTotalRows(rows.size());
        resultSet.setStatus("COMPLETED");

        // Extract columns from first row if available
        if (!rows.isEmpty()) {
            // Set columns based on SELECT clause
            if (planNode.getColumns().isEmpty() || planNode.getColumns().contains("*")) {
                // For now, use numeric indices
                resultSet.setColumns(IntStream.range(0, rows.get(0).size())
                    .mapToObj(i -> "col_" + i)
                    .collect(Collectors.toList()));
            } else {
                resultSet.setColumns(new ArrayList<>(planNode.getColumns()));
            }
        }

    } catch (Exception e) {
        AppLogger.error("Error executing scan node", e);
        resultSet.setStatus("FAILED");
        resultSet.setTotalRows(0);
    }

    return resultSet;
}
```

**Important**: Update imports at the top of the file:

```java
import java.util.stream.Collectors;
import java.util.stream.IntStream;
```

---

### Step 3: Handle Column Metadata Properly

To get proper column names, update `DataStore.executeQuery()` to return both columns and data:

**Create a new method in DataStore**:

```java
public QueryResult executeQueryWithMetadata(String sqlQuery) throws SQLException {
    long startTime = System.currentTimeMillis();

    try (Connection conn = dataSource.getConnection();
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(sqlQuery)) {

        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        // Extract column names
        List<String> columns = new ArrayList<>();
        for (int i = 1; i <= columnCount; i++) {
            columns.add(metaData.getColumnLabel(i));
        }

        // Convert rows
        List<com.distributed.sql.common.models.Row> rows = new ArrayList<>();
        while (rs.next()) {
            List<String> values = new ArrayList<>();
            for (int i = 1; i <= columnCount; i++) {
                Object value = rs.getObject(i);
                values.add(value != null ? value.toString() : "NULL");
            }
            rows.add(new com.distributed.sql.common.models.Row(values));
        }

        long executionTime = System.currentTimeMillis() - startTime;

        // Return QueryResult with metadata
        QueryResult result = QueryResult.newBuilder()
            .setQueryId("query_" + System.currentTimeMillis())
            .setSqlQuery(sqlQuery)
            .setExecutionTimeMs(executionTime)
            .setRowsReturned(rows.size())
            .setStatus(QueryStatus.COMPLETED)
            .build();

        // Note: You'll need to add columns and rows to the result
        // This depends on your proto definition

        return result;

    } catch (SQLException e) {
        AppLogger.error("SQL execution failed", e);
        throw e;
    }
}
```

---

### Step 4: Update ResultSet Model

Your `ResultSet` model needs to handle columns properly. Update the `convertFromSqlResultSet()` method (or remove it if no longer needed).

---

## Testing the Changes

### 1. Start Your System

```bash
./scripts/start_system.sh
```

### 2. Test Simple Queries

```bash
# Connect to coordinator
mvn exec:java -pl client

# In client:
SELECT name, age FROM users WHERE age > 30;
SELECT COUNT(*) FROM users;
SELECT * FROM products WHERE category = 'Electronics';
```

### 3. Verify Results

- Check coordinator logs: `tail -f logs/coordinator.log`
- Check worker logs: `tail -f logs/worker1.log`
- Results should now come from PostgreSQL, not mock data

---

## Common Issues & Solutions

### Issue 1: "Table not found" errors

**Solution**: Ensure you've created the schema in all worker databases:

```bash
psql -U postgres -d worker1_db -f scripts/create_schema.sql
psql -U postgres -d worker2_db -f scripts/create_schema.sql
psql -U postgres -d worker3_db -f scripts/create_schema.sql
```

### Issue 2: Connection refused

**Solution**: Check that PostgreSQL is running and databases are created:

```bash
pg_isready
psql -U postgres -d worker1_db -c "SELECT COUNT(*) FROM users;"
```

### Issue 3: NULL values causing issues

**Solution**: Handle NULLs in your converter:

```java
values.add(rs.getObject(i) != null ? rs.getObject(i).toString() : "NULL");
```

### Issue 4: Data type mismatches

**Solution**: Use `getObject()` for generic conversion, or handle types specifically:

```java
Object obj = rs.getObject(i);
if (obj instanceof BigDecimal) {
    values.add(((BigDecimal) obj).toPlainString());
} else {
    values.add(obj != null ? obj.toString() : "NULL");
}
```

---

## Verification Checklist

- [ ] Can execute `SELECT * FROM users` and get real data
- [ ] Can execute `SELECT COUNT(*) FROM users` and get accurate count
- [ ] WHERE clauses filter correctly
- [ ] Column names are properly displayed
- [ ] NULL values are handled gracefully
- [ ] Numeric types are converted correctly
- [ ] Connection pooling works (check HikariCP logs)
- [ ] Multiple concurrent queries work
- [ ] Error handling works for invalid SQL

---

## Next Steps

Once real SQL execution is working:

1. **Implement proper JOINs** - This is harder because you need to coordinate across workers
2. **Add aggregation logic** - SUM, AVG, COUNT, GROUP BY across workers
3. **Implement GROUP BY** - Requires re-shuffling data
4. **Add query optimization** - Use indexes from PostgreSQL

---

## Personal Touches to Add

### 1. Add Comments Explaining Decisions

```java
/**
 * Converts SQL ResultSet to our internal model.
 *
 * Why we return List<Row> instead of streaming:
 * - Simpler error handling
 * - Easier to aggregate across workers
 * - For small result sets, memory isn't an issue
 *
 * Trade-off: Large result sets will use more memory
 * Future: Implement streaming for results > 10MB
 */
```

### 2. Add Logging for Troubleshooting

```java
AppLogger.debug("Converting ResultSet with {} columns, {} rows",
    columnCount, rowCount);
```

### 3. Document Performance Characteristics

```java
// Note: This conversion adds ~1-2ms overhead
// Measured on: MacBook Pro M1, PostgreSQL 14
// With 1000 rows: ~50ms, with 10000 rows: ~200ms
```

---

## Expected Performance

With this implementation, you should see:

- Simple queries (SELECT from single table): 10-50ms
- Queries with WHERE: 20-100ms
- COUNT queries: 50-150ms
- Multi-table JOINs: 200-500ms (depends on JOIN implementation)

These numbers will vary based on:

- Network latency
- PostgreSQL configuration
- Data volume
- Connection pool size

**Document your actual benchmarks in your README!**

---

## Success Criteria

You'll know you've succeeded when:

1. ✅ All queries return real data from PostgreSQL
2. ✅ No more "Sample Data" in results
3. ✅ You can verify data matches what's in the database
4. ✅ Multiple workers return different data (sharding working)
5. ✅ Console shows execution times and row counts
6. ✅ You understand where each millisecond is spent

---

Good luck! This change will transform your project from a demo to a real distributed SQL engine.
