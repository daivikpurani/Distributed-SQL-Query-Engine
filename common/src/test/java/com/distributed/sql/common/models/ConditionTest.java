package com.distributed.sql.common.models;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ConditionTest {

    @Test
    void testConditionCreation() {
        Condition condition = new Condition("age", Operator.GREATER_THAN, "25", DataType.INTEGER);

        assertEquals("age", condition.getColumn());
        assertEquals(Operator.GREATER_THAN, condition.getOperator());
        assertEquals("25", condition.getValue());
        assertEquals(DataType.INTEGER, condition.getDataType());
    }

    @Test
    void testConditionSetters() {
        Condition condition = new Condition("name", Operator.EQUALS, "John", DataType.STRING);

        condition.setColumn("email");
        condition.setOperator(Operator.LIKE);
        condition.setValue("%@gmail.com");
        condition.setDataType(DataType.STRING);

        assertEquals("email", condition.getColumn());
        assertEquals(Operator.LIKE, condition.getOperator());
        assertEquals("%@gmail.com", condition.getValue());
        assertEquals(DataType.STRING, condition.getDataType());
    }

    @Test
    void testOperatorEnum() {
        assertEquals(Operator.EQUALS, Operator.EQUALS);
        assertEquals(Operator.NOT_EQUALS, Operator.NOT_EQUALS);
        assertEquals(Operator.GREATER_THAN, Operator.GREATER_THAN);
        assertEquals(Operator.LESS_THAN, Operator.LESS_THAN);
        assertEquals(Operator.GREATER_THAN_EQUALS, Operator.GREATER_THAN_EQUALS);
        assertEquals(Operator.LESS_THAN_EQUALS, Operator.LESS_THAN_EQUALS);
        assertEquals(Operator.LIKE, Operator.LIKE);
        assertEquals(Operator.IN, Operator.IN);
    }
}
