package org.alist.hub.sql;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;
import java.util.stream.Stream;

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


    /**
     * 执行分批的 SQL 脚本
     *
     * @param scriptFilePath SQL 脚本文件的路径
     * @param batchSize      批次大小
     * @throws Exception
     */
    public void executeBatchedSQL(String scriptFilePath, int batchSize) throws Exception {
        try (Stream<String> lines = Files.lines(Path.of(scriptFilePath))) {
            // 获取数据库连接
            Connection connection = getConnection();
            // 设置自动提交模式为 false，启用事务处理
            connection.setAutoCommit(false);
            final int[] count = {0};
            // 创建语句对象
            Statement statement = connection.createStatement();
            // 逐行处理脚本文件中的内容
            lines.forEach(line -> {
                if (!(line.equals("BEGIN TRANSACTION;") || line.equals("COMMIT;"))) {
                    try {
                        // 将 SQL 语句添加到批量处理中
                        statement.addBatch(line);
                        count[0]++;
                        // 当达到批量大小时执行批量处理并提交事务，然后清空批量语句
                        if (count[0] % batchSize == 0) {
                            statement.executeBatch();
                            connection.commit();
                            statement.clearBatch();
                        }
                    } catch (SQLException e) {
                        log.error(e.getMessage(), e);
                    }
                }

            });
            // 如果剩余语句数不是批量大小的倍数，则执行剩余的批量处理并提交事务
            if (count[0] % batchSize != 0) {
                statement.executeBatch();
                connection.commit();
            }
            // 恢复自动提交模式
            connection.setAutoCommit(true);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

}
