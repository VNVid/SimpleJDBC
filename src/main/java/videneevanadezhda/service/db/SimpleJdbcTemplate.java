package videneevanadezhda.service.db;

import lombok.AllArgsConstructor;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.Objects;


@AllArgsConstructor
public class SimpleJdbcTemplate {
    @FunctionalInterface
    public interface SQLFunction<T, R> {
        R apply(T object) throws SQLException, ParseException;
    }

    @FunctionalInterface
    public interface SQLConsumer<T> {
        void accept(T object) throws SQLException;
    }

    private final DataSource connectionPool;

    /**
     * TODO
     *
     * @param consumer
     * @throws SQLException
     */
    public void connection(SQLConsumer<? super Connection> consumer) throws SQLException {
        Objects.requireNonNull(consumer);
        try (Connection conn = connectionPool.getConnection()) {
            consumer.accept(conn);
        }
    }

    /**
     * TODO
     *
     * @param function
     * @param <R>
     * @return
     * @throws SQLException
     */
    public <R> R connection(SQLFunction<? super Connection, ? extends R> function) throws SQLException, ParseException {
        Objects.requireNonNull(function);
        try (Connection conn = connectionPool.getConnection()) {
            return function.apply(conn);
        }
    }

    /**
     * TODO
     *
     * @param function
     * @param <R>
     * @return
     * @throws SQLException
     */
    public <R> R statement(SQLFunction<? super Statement, ? extends R> function) throws SQLException, ParseException {
        Objects.requireNonNull(function);
        return connection(conn -> {
            try (Statement stmt = conn.createStatement()) {
                return function.apply(stmt);
            }
        });
    }

    /**
     * TODO
     *
     * @param consumer
     * @throws SQLException
     */
    public void statement(SQLConsumer<? super Statement> consumer) throws SQLException {
        Objects.requireNonNull(consumer);
        connection(conn -> {
            try (Statement stmt = conn.createStatement()) {
                consumer.accept(stmt);
            }
        });
    }

    /**
     * TODO
     *
     * @param sql
     * @param function
     * @param <R>
     * @return
     * @throws SQLException
     */
    public <R> R preparedStatement(String sql, SQLFunction<? super PreparedStatement, ? extends R> function)
            throws SQLException, ParseException {
        Objects.requireNonNull(function);
        return connection(conn -> {
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                return function.apply(stmt);
            }
        });
    }

    /**
     * TODO
     *
     * @param sql
     * @param consumer
     * @throws SQLException
     */
    public void preparedStatement(String sql, SQLConsumer<? super PreparedStatement> consumer) throws SQLException {
        Objects.requireNonNull(consumer);
        connection(conn -> {
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                consumer.accept(stmt);
            }
        });
    }
}
