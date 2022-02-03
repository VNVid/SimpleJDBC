package videneevanadezhda.service.dao;

import org.javatuples.Pair;
import videneevanadezhda.domain.CityInfo;
import videneevanadezhda.domain.Flight;
import videneevanadezhda.service.db.SimpleJdbcTemplate;
import lombok.AllArgsConstructor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;
import java.util.HashSet;


@AllArgsConstructor
public class FlightDao {
    private final SimpleJdbcTemplate source;

    /**
     * 2. Вывести города, из которых чаще всего отменяли рейсы.
     *
     * @return
     * @throws SQLException
     */
    public ArrayList<Pair<String, Integer>> getCitiesMostFrequentCancelledFlights() throws SQLException {
        ArrayList<Pair<String, Integer>> cities = new ArrayList<>();

        String sql = "select airports.city, count(flights.status) as cancelled_number "
                + "from flights "
                + "inner join airports on flights.departure_airport=airports.airport_code "
                + "where flights.status='Cancelled' "
                + "group by airports.city";
        source.statement(stmt -> {
            ResultSet resultSet = stmt.executeQuery(sql);
            while (resultSet.next()) {
                cities.add(new Pair<>(resultSet.getString("city"), resultSet.getInt("cancelled_number")));
            }
        });

        Collections.sort(cities, Comparator.comparing(p -> -p.getValue1()));
        return cities;
    }


    /**
     * 3. В каждом городе вылета найти самый короткий маршрут. Отсортировать по продолжительности.
     *
     * @return
     * @throws SQLException
     */
    public Set<Flight> getShortestTimeForCity() throws SQLException {
        Set<Flight> flights = new HashSet<>();

        String sqlInner = "create table table_mean as (select flights.arrival_airport as aa, "
                + "flights.departure_airport as da, "
                + "avg(extract (day    from (flights.scheduled_arrival-flights.scheduled_departure))*24*60*60+ "
                + "extract (hour   from (flights.scheduled_arrival-flights.scheduled_departure))*60*60+"
                + "extract (minute from (flights.scheduled_arrival-flights.scheduled_departure))*60+"
                + "extract (second from (flights.scheduled_arrival-flights.scheduled_departure))) as mean_time, "
                + "airports.city as city "
                + "from flights inner join airports on airports.airport_code=flights.departure_airport "
                + "where (flights.scheduled_arrival is not null) and (flights.scheduled_departure is not null) "
                + "group by flights.flight_no);";
        String sql = "select airports.city as city, "
                + "min(table_mean.mean_time) as time "
                + "from airports "
                + "inner join table_mean "
                + "on table_mean.da=airports.airport_code "
                + "group by airports.city";
        String sqlOuter = "select distinct table_final.city, table_mean.aa, table_final.time from "
                + "(" + sql + ") as table_final "
                + "inner join table_mean on table_mean.city=table_final.city "
                + "and table_mean.mean_time=table_final.time;";

        source.statement(stmt -> {
            stmt.execute(sqlInner);
            ResultSet resultSet = stmt.executeQuery(sqlOuter);
            while (resultSet.next()) {
                flights.add(new Flight(resultSet.getString(1),
                        resultSet.getString(2), resultSet.getString(3)));
            }
        });

        return flights;
    }

    /**
     * 4. Найти кол-во отмен рейсов по месяцам.
     *
     * @return
     * @throws SQLException
     */
    public ArrayList<Pair<String, Integer>> getNumberCancelled() throws SQLException {
        ArrayList<Pair<String, Integer>> month = new ArrayList<>();

        String sql = "select monthname(flights.scheduled_departure) as month, count(status) as num "
                + "from flights where status=\'Cancelled\' "
                + "group by month;";
        source.statement(stmt -> {
            ResultSet resultSet = stmt.executeQuery(sql);
            while (resultSet.next()) {
                month.add(new Pair<String, Integer>(resultSet.getString("month"), resultSet.getInt("num")));
            }
        });
        return month;
    }

    /**
     * 5. Выведите кол-во рейсов в Москву и из Москвы по дням недели за весь наблюдаемый период.
     *
     * @return
     * @throws SQLException
     */
    public ArrayList<CityInfo> getInfoForMoscow() throws SQLException {
        ArrayList<CityInfo> ans = new ArrayList<>();

        String sql1 = "create table arrived as (select dayname(flights.scheduled_arrival) as day, "
                + "count(flights.flight_id) as num "
                + "from flights "
                + "inner join airports on airports.airport_code=flights.arrival_airport "
                + "where status=\'Arrived\' and airports.city=\'Moscow\' "
                + "group by day);";
        String sql2 = "select departure.day as day, departure.num as num_dep, arrived.num as num_arr "
                + "from (select dayname(flights.scheduled_departure) as day, "
                + "count(flights.flight_id) as num "
                + "from flights "
                + "inner join airports on airports.airport_code=flights.departure_airport "
                + "where status=\'Arrived\' and airports.city=\'Moscow\' "
                + "group by day) as departure "
                + "inner join arrived on arrived.day=departure.day;";
        source.statement(stmt -> {
            stmt.execute(sql1);
            ResultSet resultSet = stmt.executeQuery(sql2);
            while (resultSet.next()) {
                ans.add(new CityInfo("Moscow", resultSet.getString("day"),
                        resultSet.getInt("num_arr"), resultSet.getInt("num_dep")));
            }
        });
        return ans;
    }


    /**
     * 6. Отменить все рейсы самолета, заданной модели
     *
     * @param model
     * @throws SQLException
     */
    public void updateStatusByAircraftModel(String model, TicketDao ticketDaoInstance) throws SQLException {
        String sql = "update flights set status = 'Cancelled' "
                + "where flights.status in (\'Scheduled\', \'On Time\', \'Delayed\') and "
                + "aircraft_code in (select aircraft_code "
                + "from aircrafts where model = ?);";
        source.preparedStatement(sql, stmt -> {
            stmt.setString(1, model);
            stmt.executeUpdate();
        });
        ticketDaoInstance.deleteCancelledTickets();
    }


    /**
     * 7. В связи с пандемией COVID-2017 все рейсы, прибывающие в Москву и отбывающие из неё,
     * запланированные на даты в интервале, заданном параметром (например,  [01.08.17, 15.08.17]), были отменены.
     * Перевести соответствующие рейсы в Cancelled, а также посчитать убыток,
     * который теряют компании-перевозчики по дням.
     *
     * @param begin
     * @param end
     * @throws SQLException
     */
    public ArrayList<Pair<String, Integer>> updateStatusBetweenDatesAndCountLosses(String begin, String end)
            throws SQLException, ParseException {
        String findID = "select flight_id "
                + "from flights inner join airports as arr "
                + "on arr.airport_code=flights.arrival_airport "
                + "inner join airports as dep "
                + "on dep.airport_code=flights.departure_airport "
                + "where (dep.city=\'Moscow\' or arr.city=\'Moscow\') and "
                + "((scheduled_departure between ? and ?) or (scheduled_arrival between ? and ?));";
        String flightIDs = source.preparedStatement(findID, stmt -> {
            stmt.setString(1, begin);
            stmt.setString(2, end);
            stmt.setString(3, begin);
            stmt.setString(4, end);
            ResultSet resultSet = stmt.executeQuery();
            String res = "";
            while (resultSet.next()) {
                res += resultSet.getString("flight_id") + ", ";
            }
            return res.substring(0, res.length() - 2);
        });

        String sql = "update flights set status = 'Cancelled' "
                + "where flight_id in (" + flightIDs + ");";
        source.statement(stmt -> {
            stmt.execute(sql);
        });

        String countLoss = "select day(scheduled_arrival) as day, sum(ticket_flights.amount) as sum "
                + "from ticket_flights inner join flights on ticket_flights.flight_id = flights.flight_id "
                + "where ticket_flights.flight_id in (" + flightIDs + ") "
                + "group by day(scheduled_arrival);";
        return source.statement(stmt -> {
            ResultSet resultSet = stmt.executeQuery(countLoss);
            ArrayList<Pair<String, Integer>> res = new ArrayList<>();
            while (resultSet.next()) {
                res.add(new Pair<>(resultSet.getString("day"), resultSet.getInt("sum")));
            }
            return res;
        });
    }

////////////////////////////////////////////////////////////////////////////////////////////////////////
// These functions are not used in task
// ////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * For testing purposes.  SKIP WHEN CHECKING
     *
     * @return
     * @throws SQLException
     */
    public ArrayList<Timestamp> getTime() throws SQLException {
        ArrayList<Timestamp> cities = new ArrayList<>();

        String sql = "select scheduled_departure from flights;";
        source.statement(stmt -> {
            ResultSet resultSet = stmt.executeQuery(sql);
            while (resultSet.next()) {
                cities.add(resultSet.getTimestamp("scheduled_departure"));
            }
        });
        return cities;
    }

    /**
     * For testing purposes.  SKIP WHEN CHECKING
     *
     * @return
     * @throws SQLException
     */
    public ArrayList<Pair<String, Integer>> getFLightNo() throws SQLException {
        ArrayList<Pair<String, Integer>> cities = new ArrayList<>();

        String sql = "select flight_no, count(flight_id) as num "
                + "from flights group by flight_no;";
        source.statement(stmt -> {
            ResultSet resultSet = stmt.executeQuery(sql);
            while (resultSet.next()) {
                cities.add(new Pair<String, Integer>(resultSet.getString("flight_no"), resultSet.getInt("num")));
            }
        });
        return cities;
    }
}
