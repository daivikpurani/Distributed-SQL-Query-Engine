package com.distributed.sql.common.models;

/**
 * Represents a WHERE condition in a SQL query
 */
public class Condition {
    private String column;
    private Operator operator;
    private String value;
    private DataType dataType;

    public Condition() {}

    public Condition(String column, Operator operator, String value) {
        this.column = column;
        this.operator = operator;
        this.value = value;
        this.dataType = DataType.STRING; // Default to string
    }

    public Condition(String column, Operator operator, String value, DataType dataType) {
        this.column = column;
        this.operator = operator;
        this.value = value;
        this.dataType = dataType;
    }

    // Getters and setters
    public String getColumn() { return column; }
    public void setColumn(String column) { this.column = column; }

    public Operator getOperator() { return operator; }
    public void setOperator(Operator operator) { this.operator = operator; }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }

    public DataType getDataType() { return dataType; }
    public void setDataType(DataType dataType) { this.dataType = dataType; }

    @Override
    public String toString() {
        return String.format("%s %s %s", column, operator, value);
    }

    public enum Operator {
        EQUALS("="),
        NOT_EQUALS("!="),
        GREATER_THAN(">"),
        LESS_THAN("<"),
        GREATER_THAN_EQUALS(">="),
        LESS_THAN_EQUALS("<="),
        LIKE("LIKE"),
        IN("IN");

        private final String symbol;

        Operator(String symbol) {
            this.symbol = symbol;
        }

        public String getSymbol() {
            return symbol;
        }
    }

    public enum DataType {
        STRING, INTEGER, DOUBLE, BOOLEAN, DATE
    }
}
