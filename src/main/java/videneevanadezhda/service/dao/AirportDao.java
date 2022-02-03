package videneevanadezhda.service.dao;

import videneevanadezhda.service.db.SimpleJdbcTemplate;
import lombok.AllArgsConstructor;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

@AllArgsConstructor
public class AirportDao {
    private final SimpleJdbcTemplate source;

    /**
     * 1. Вывести города, в которых несколько аэропортов.
     *
     * @return
     * @throws SQLException
     */
    public Set<String> getCitiesWithSeveralAirports() throws SQLException {
        Set<String> cities = new HashSet<>();

        String sql = "select city from airports "
                + "group by city having count(airport_code)>1";
        source.statement(stmt -> {
            ResultSet resultSet = stmt.executeQuery(sql);
            while (resultSet.next()) {
                cities.add(resultSet.getString("city"));
            }
        });
        return cities;
    }

    /**
     * Для задачи В.1
     * @param city
     * @return
     * @throws SQLException
     */
    public Set<String> getAirportsByCity(String city) throws SQLException {
        Set<String> airports = new HashSet<>();

        String sql = "select airport_code from airports "
                + "where city=\'" + city + "\';";
        source.statement(stmt -> {
            ResultSet resultSet = stmt.executeQuery(sql);
            while (resultSet.next()) {
                airports.add(resultSet.getString("airport_code"));
            }
        });
        return airports;
    }


////////////////////////////////////////////////////////////////////////////////////////////////////////
// These functions are not used in task
// ////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * For testing purposes
     *
     * @return
     * @throws SQLException
     */
    public Set<String> getCities() throws SQLException {
        Set<String> cities = new HashSet<>();

        String sql = "select city from airports";
        source.statement(stmt -> {
            ResultSet resultSet = stmt.executeQuery(sql);
            while (resultSet.next()) {
                cities.add(resultSet.getString("city"));
            }
        });
        return cities;
    }
}
