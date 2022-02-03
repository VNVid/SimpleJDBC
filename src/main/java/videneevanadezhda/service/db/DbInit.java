package videneevanadezhda.service.db;

import videneevanadezhda.App;
import lombok.AllArgsConstructor;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.stream.Collectors;

/**
 * Initializes database
 */
@AllArgsConstructor
public class DbInit {
    private final SimpleJdbcTemplate source;

    private String getSQL(String name) throws IOException {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(
                        App.class.getResourceAsStream(name),
                        StandardCharsets.UTF_8))) {
            return br.lines().collect(Collectors.joining("\n"));
        }
    }

    /**
     * TODO
     *
     * @throws SQLException
     * @throws IOException
     */
    public void create() throws SQLException, IOException {
        String sql = getSQL("create.sql");
        source.statement(stmt -> {
            stmt.execute(sql);
        });
    }
}
