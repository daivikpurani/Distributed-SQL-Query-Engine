package com.distributed.sql.common.models;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

public class ResultSetTest {

    private ResultSet resultSet;

    @BeforeEach
    void setUp() {
        resultSet = new ResultSet();
    }

    @Test
    void testResultSetCreation() {
        assertNotNull(resultSet);
        assertNotNull(resultSet.getRows());
        assertTrue(resultSet.getRows().isEmpty());
    }

    @Test
    void testSetAndGetColumns() {
        List<String> columns = Arrays.asList("id", "name", "age");
        resultSet.setColumns(columns);
        assertEquals(columns, resultSet.getColumns());
    }

    @Test
    void testAddRow() {
        Row row1 = new Row(Arrays.asList("1", "John", "30"));
        Row row2 = new Row(Arrays.asList("2", "Jane", "25"));

        resultSet.addRow(row1);
        resultSet.addRow(row2);

        assertEquals(2, resultSet.getRows().size());
        assertEquals(row1, resultSet.getRows().get(0));
        assertEquals(row2, resultSet.getRows().get(1));
    }

    @Test
    void testSetAndGetTotalRows() {
        resultSet.setTotalRows(100);
        assertEquals(100, resultSet.getTotalRows());
    }

    @Test
    void testSetAndGetExecutionTime() {
        resultSet.setExecutionTimeMs(150);
        assertEquals(150, resultSet.getExecutionTimeMs());
    }

    @Test
    void testSetAndGetStatus() {
        resultSet.setStatus("SUCCESS");
        assertEquals("SUCCESS", resultSet.getStatus());
    }

    @Test
    void testSetAndGetQueryId() {
        resultSet.setQueryId("query_123");
        assertEquals("query_123", resultSet.getQueryId());
    }
}
