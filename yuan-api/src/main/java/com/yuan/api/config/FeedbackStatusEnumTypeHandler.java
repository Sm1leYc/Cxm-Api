package com.yuan.api.config;

import com.yuan.api.model.enums.FeedbackStatusEnum;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 映射枚举类
 */
public class FeedbackStatusEnumTypeHandler extends BaseTypeHandler<FeedbackStatusEnum> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, FeedbackStatusEnum parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, parameter.getStatus());
    }

    @Override
    public FeedbackStatusEnum getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String status = rs.getString(columnName);
        return FeedbackStatusEnum.valueOfStatus(status);
    }

    @Override
    public FeedbackStatusEnum getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String status = rs.getString(columnIndex);
        return FeedbackStatusEnum.valueOfStatus(status);
    }

    @Override
    public FeedbackStatusEnum getNullableResult(java.sql.CallableStatement cs, int columnIndex) throws SQLException {
        String status = cs.getString(columnIndex);
        return FeedbackStatusEnum.valueOfStatus(status);
    }
}

