package com.paper.reviewer.common.persistence;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@MappedTypes(JsonNode.class)
@MappedJdbcTypes(value = {JdbcType.VARCHAR, JdbcType.LONGVARCHAR, JdbcType.OTHER}, includeNullJdbcType = true)
public class JsonNodeTypeHandler extends BaseTypeHandler<JsonNode> {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, JsonNode parameter, JdbcType jdbcType)
            throws SQLException {
        ps.setString(i, parameter.toString());
    }

    @Override
    public JsonNode getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return parse(rs.getString(columnName));
    }

    @Override
    public JsonNode getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return parse(rs.getString(columnIndex));
    }

    @Override
    public JsonNode getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return parse(cs.getString(columnIndex));
    }

    private JsonNode parse(String json) throws SQLException {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            JsonNode parsed = OBJECT_MAPPER.readTree(json);
            // H2's JSON type can expose JSON text as a quoted JSON string.
            return parsed.isTextual() ? OBJECT_MAPPER.readTree(parsed.textValue()) : parsed;
        } catch (JacksonException e) {
            throw new SQLException("Cannot deserialize JSON column", e);
        }
    }
}
