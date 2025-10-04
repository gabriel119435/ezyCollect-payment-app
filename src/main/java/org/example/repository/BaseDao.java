package org.example.repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BaseDao {

    protected final DataSource dataSource;

    protected BaseDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    protected interface SqlConsumer<T> {
        void accept(T t) throws SQLException;
    }

    protected interface SqlFunction<T, R> {
        R apply(T t) throws SQLException;
    }

    protected int executeUpdate(String sql, SqlConsumer<PreparedStatement> binder) {
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            binder.accept(stmt);
            return stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    protected <T> List<T> executeQuery(String sql, SqlConsumer<PreparedStatement> binder, SqlFunction<ResultSet, T> mapper) {
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            binder.accept(stmt);
            List<T> result = new ArrayList<>();
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) result.add(mapper.apply(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
