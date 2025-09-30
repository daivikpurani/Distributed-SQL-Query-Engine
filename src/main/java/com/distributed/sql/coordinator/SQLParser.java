package com.distributed.sql.coordinator;

import com.distributed.sql.common.models.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Simple SQL parser for SELECT queries with WHERE and JOIN support
 */
public class SQLParser {
    private static final Pattern SELECT_PATTERN = Pattern.compile(
        "SELECT\\s+(.+?)\\s+FROM\\s+(.+?)(?:\\s+WHERE\\s+(.+?))?(?:\\s+JOIN\\s+(.+?)\\s+ON\\s+(.+?))?$",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern JOIN_PATTERN = Pattern.compile(
        "(.+?)\\s+JOIN\\s+(.+?)\\s+ON\\s+(.+?)\\s*=\\s*(.+?)$",
        Pattern.CASE_INSENSITIVE
    );

    /**
     * Parse a SQL SELECT query into a Query object
     */
    public static Query parse(String sql) throws SQLParseException {
        String queryId = "query_" + System.currentTimeMillis();
        Query query = new Query(queryId, sql);

        Matcher matcher = SELECT_PATTERN.matcher(sql.trim());
        if (!matcher.matches()) {
            throw new SQLParseException("Invalid SELECT query format: " + sql);
        }

        // Parse SELECT columns
        String selectClause = matcher.group(1).trim();
        List<String> columns = parseColumns(selectClause);
        query.setSelectColumns(columns);

        // Parse FROM tables
        String fromClause = matcher.group(2).trim();
        List<String> tables = parseTables(fromClause);
        query.setFromTables(tables);

        // Parse WHERE conditions
        String whereClause = matcher.group(3);
        if (whereClause != null && !whereClause.trim().isEmpty()) {
            List<Condition> conditions = parseConditions(whereClause.trim());
            query.setWhereConditions(conditions);
        }

        // Parse JOIN clauses
        String joinClause = matcher.group(4);
        if (joinClause != null && !joinClause.trim().isEmpty()) {
            List<Join> joins = parseJoins(joinClause.trim());
            query.setJoins(joins);
        }

        query.setType(Query.QueryType.SELECT);
        return query;
    }

    private static List<String> parseColumns(String selectClause) {
        List<String> columns = new ArrayList<>();
        if (selectClause.equals("*")) {
            // For simplicity, we'll handle * in the planner
            columns.add("*");
        } else {
            String[] parts = selectClause.split(",");
            for (String part : parts) {
                columns.add(part.trim());
            }
        }
        return columns;
    }

    private static List<String> parseTables(String fromClause) {
        List<String> tables = new ArrayList<>();
        String[] parts = fromClause.split(",");
        for (String part : parts) {
            tables.add(part.trim());
        }
        return tables;
    }

    private static List<Condition> parseConditions(String whereClause) {
        List<Condition> conditions = new ArrayList<>();
        
        // Simple condition parsing - supports basic operators
        String[] parts = whereClause.split("\\s+(AND|OR)\\s+");
        for (String part : parts) {
            part = part.trim();
            
            // Parse individual condition
            if (part.contains(">=")) {
                String[] tokens = part.split(">=");
                conditions.add(new Condition(tokens[0].trim(), Condition.Operator.GREATER_THAN_EQUALS, tokens[1].trim()));
            } else if (part.contains("<=")) {
                String[] tokens = part.split("<=");
                conditions.add(new Condition(tokens[0].trim(), Condition.Operator.LESS_THAN_EQUALS, tokens[1].trim()));
            } else if (part.contains("!=")) {
                String[] tokens = part.split("!=");
                conditions.add(new Condition(tokens[0].trim(), Condition.Operator.NOT_EQUALS, tokens[1].trim()));
            } else if (part.contains("=")) {
                String[] tokens = part.split("=");
                conditions.add(new Condition(tokens[0].trim(), Condition.Operator.EQUALS, tokens[1].trim()));
            } else if (part.contains(">")) {
                String[] tokens = part.split(">");
                conditions.add(new Condition(tokens[0].trim(), Condition.Operator.GREATER_THAN, tokens[1].trim()));
            } else if (part.contains("<")) {
                String[] tokens = part.split("<");
                conditions.add(new Condition(tokens[0].trim(), Condition.Operator.LESS_THAN, tokens[1].trim()));
            }
        }
        
        return conditions;
    }

    private static List<Join> parseJoins(String joinClause) {
        List<Join> joins = new ArrayList<>();
        
        Matcher matcher = JOIN_PATTERN.matcher(joinClause);
        if (matcher.matches()) {
            String leftTable = matcher.group(1).trim();
            String rightTable = matcher.group(2).trim();
            String leftColumn = matcher.group(3).trim();
            String rightColumn = matcher.group(4).trim();
            
            joins.add(new Join(leftTable, rightTable, leftColumn, rightColumn, Join.JoinType.INNER));
        }
        
        return joins;
    }

    public static class SQLParseException extends Exception {
        public SQLParseException(String message) {
            super(message);
        }
    }
}
