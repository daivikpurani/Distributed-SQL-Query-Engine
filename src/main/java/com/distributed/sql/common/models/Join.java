package com.distributed.sql.common.models;

/**
 * Represents a JOIN operation in a SQL query
 */
public class Join {
    private String leftTable;
    private String rightTable;
    private String leftColumn;
    private String rightColumn;
    private JoinType type;

    public Join() {}

    public Join(String leftTable, String rightTable, String leftColumn, String rightColumn, JoinType type) {
        this.leftTable = leftTable;
        this.rightTable = rightTable;
        this.leftColumn = leftColumn;
        this.rightColumn = rightColumn;
        this.type = type;
    }

    // Getters and setters
    public String getLeftTable() { return leftTable; }
    public void setLeftTable(String leftTable) { this.leftTable = leftTable; }

    public String getRightTable() { return rightTable; }
    public void setRightTable(String rightTable) { this.rightTable = rightTable; }

    public String getLeftColumn() { return leftColumn; }
    public void setLeftColumn(String leftColumn) { this.leftColumn = leftColumn; }

    public String getRightColumn() { return rightColumn; }
    public void setRightColumn(String rightColumn) { this.rightColumn = rightColumn; }

    public JoinType getType() { return type; }
    public void setType(JoinType type) { this.type = type; }

    @Override
    public String toString() {
        return String.format("%s JOIN %s ON %s.%s = %s.%s", 
                           type, rightTable, leftTable, leftColumn, rightTable, rightColumn);
    }

    public enum JoinType {
        INNER, LEFT, RIGHT, FULL
    }
}
