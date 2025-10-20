package com.distributed.sql.coordinator;

import com.distributed.sql.common.models.*;
import com.distributed.sql.common.utils.AppLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Simple SQL parser for basic query parsing
 */
public class SQLParser {

    private static final Pattern SELECT_PATTERN = Pattern.compile(
            "SELECT\\s+(.*?)\\s+FROM\\s+(.*?)(?:\\s+WHERE\\s+(.*?))?(?:\\s+ORDER\\s+BY\\s+(.*?))?(?:\\s+LIMIT\\s+(\\d+))?",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private static final Pattern WHERE_CONDITION_PATTERN = Pattern.compile(
            "(\\w+)\\s*([<>=!]+|LIKE|IN)\\s*('?\"?.*?\"?'?)(?:\\s+(AND|OR)\\s+)?",
            Pattern.CASE_INSENSITIVE);

    public Query parse(String sql) {
        AppLogger.info("Parsing SQL query: {}", sql);

        Query query = new Query(sql, QueryType.SELECT);

        Matcher matcher = SELECT_PATTERN.matcher(sql.trim());
        if (matcher.find()) {
            String selectClause = matcher.group(1);
            String fromClause = matcher.group(2);
            String whereClause = matcher.group(3);

            // Parse SELECT columns
            parseSelectColumns(selectClause, query);

            // Parse FROM tables
            parseFromTables(fromClause, query);

            // Parse WHERE conditions
            if (whereClause != null && !whereClause.trim().isEmpty()) {
                parseWhereConditions(whereClause, query);
            }
        } else {
            AppLogger.warn("Could not parse SQL query: {}", sql);
        }

        return query;
    }

    private void parseSelectColumns(String selectClause, Query query) {
        if (selectClause == null || selectClause.trim().isEmpty()) {
            return;
        }

        String trimmed = selectClause.trim();
        if ("*".equals(trimmed)) {
            query.setSelectColumns(List.of("*"));
        } else {
            String[] columns = trimmed.split(",");
            List<String> columnList = new ArrayList<>();
            for (String column : columns) {
                columnList.add(column.trim());
            }
            query.setSelectColumns(columnList);
        }
    }

    private void parseFromTables(String fromClause, Query query) {
        if (fromClause == null || fromClause.trim().isEmpty()) {
            return;
        }

        String[] tables = fromClause.split(",");
        List<String> tableList = new ArrayList<>();
        for (String table : tables) {
            tableList.add(table.trim());
        }
        query.setFromTables(tableList);
    }

    private void parseWhereConditions(String whereClause, Query query) {
        Matcher matcher = WHERE_CONDITION_PATTERN.matcher(whereClause);
        while (matcher.find()) {
            String column = matcher.group(1);
            String operatorStr = matcher.group(2);
            String value = matcher.group(3).replaceAll("['\"]", ""); // Remove quotes

            Operator operator = parseOperator(operatorStr);
            DataType dataType = inferDataType(value);

            Condition condition = new Condition(column, operator, value, dataType);
            query.addCondition(condition);
        }
    }

    private Operator parseOperator(String opStr) {
        return switch (opStr.toUpperCase()) {
            case "=" -> Operator.EQUALS;
            case "!=" -> Operator.NOT_EQUALS;
            case ">" -> Operator.GREATER_THAN;
            case "<" -> Operator.LESS_THAN;
            case ">=" -> Operator.GREATER_THAN_EQUALS;
            case "<=" -> Operator.LESS_THAN_EQUALS;
            case "LIKE" -> Operator.LIKE;
            case "IN" -> Operator.IN;
            default -> throw new IllegalArgumentException("Unknown operator: " + opStr);
        };
    }

    private DataType inferDataType(String value) {
        if (value.matches("-?\\d+")) {
            return DataType.INTEGER;
        } else if (value.matches("-?\\d+\\.\\d+")) {
            return DataType.DOUBLE;
        } else if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
            return DataType.BOOLEAN;
        } else if (value.matches("\\d{4}-\\d{2}-\\d{2}")) { // YYYY-MM-DD
            return DataType.DATE;
        }
        return DataType.STRING;
    }
}