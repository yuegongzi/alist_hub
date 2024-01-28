package org.alist.hub.sql;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;

@Component
@Slf4j
@AllArgsConstructor
public class SqlScriptBatchExecutor {
    private final JdbcTemplate jdbcTemplate;

    public Connection getConnection() throws SQLException {
        // 使用 JdbcTemplate 获取 Connection
        return Objects.requireNonNull(jdbcTemplate.getDataSource()).getConnection();
    }


    /**
     * 执行SQL语句
     *
     * @param sql 待执行的SQL语句
     * @throws SQLException SQL异常
     */
    public void executeSQL(String sql) throws SQLException {
        Connection connection = getConnection(); // 获取数据库连接
        Statement statement = connection.createStatement(); // 创建语句对象
        statement.executeUpdate(sql);
    }


}
