package com.distributed.sql.common.models;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

public class QueryTest {

    private Query query;

    @BeforeEach
    void setUp() {
        query = new Query();
        query.setSql("SELECT * FROM users WHERE age > 25");
        query.setQueryType(QueryType.SELECT);
    }

    @Test
    void testQueryCreation() {
        assertNotNull(query);
        assertEquals("SELECT * FROM users WHERE age > 25", query.getSql());
        assertEquals(QueryType.SELECT, query.getQueryType());
    }

    @Test
    void testSetAndGetColumns() {
        List<String> columns = new ArrayList<>();
        columns.add("id");
        columns.add("name");
        columns.add("age");
        query.setSelectColumns(columns);
        assertEquals(columns, query.getSelectColumns());
    }

    @Test
    void testSetAndGetTables() {
        List<String> tables = new ArrayList<>();
        tables.add("users");
        tables.add("orders");
        query.setFromTables(tables);
        assertEquals(tables, query.getFromTables());
    }

    @Test
    void testSetAndGetConditions() {
        Condition condition = new Condition("age", Operator.GREATER_THAN, "25", DataType.INTEGER);
        List<Condition> conditions = new ArrayList<>();
        conditions.add(condition);
        query.setWhereConditions(conditions);
        assertEquals(conditions, query.getWhereConditions());
    }

    @Test
    void testSetAndGetJoins() {
        Join join = new Join("users", "orders", "id", "user_id", JoinType.INNER);
        List<Join> joins = new ArrayList<>();
        joins.add(join);
        query.setJoins(joins);
        assertEquals(joins, query.getJoins());
    }

    @Test
    void testQueryTypeDetection() {
        Query selectQuery = new Query();
        selectQuery.setSql("SELECT * FROM users");
        selectQuery.setQueryType(QueryType.SELECT);
        assertEquals(QueryType.SELECT, selectQuery.getQueryType());

        Query insertQuery = new Query();
        insertQuery.setSql("INSERT INTO users VALUES (1, 'John')");
        insertQuery.setQueryType(QueryType.INSERT);
        assertEquals(QueryType.INSERT, insertQuery.getQueryType());

        Query updateQuery = new Query();
        updateQuery.setSql("UPDATE users SET age = 30 WHERE id = 1");
        updateQuery.setQueryType(QueryType.UPDATE);
        assertEquals(QueryType.UPDATE, updateQuery.getQueryType());

        Query deleteQuery = new Query();
        deleteQuery.setSql("DELETE FROM users WHERE id = 1");
        deleteQuery.setQueryType(QueryType.DELETE);
        assertEquals(QueryType.DELETE, deleteQuery.getQueryType());
    }
}
