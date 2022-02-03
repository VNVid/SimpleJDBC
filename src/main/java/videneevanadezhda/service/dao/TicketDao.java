package videneevanadezhda.service.dao;

import videneevanadezhda.service.db.SimpleJdbcTemplate;
import lombok.AllArgsConstructor;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;


@AllArgsConstructor
public class TicketDao {
    private final SimpleJdbcTemplate source;

    /**
     * 6. Все билеты, относящиеся к удаленным рейсам - удалить.
     *
     * @throws SQLException
     */
    public void deleteCancelledTickets() throws SQLException {
        String sql1 = "delete tickets where ticket_no in "
                + "(select ticket_no from ticket_flights "
                + "where flight_id in (select flight_id from flights "
                + "where status=\'Cancelled\'));";
        source.statement(stmt -> {
            stmt.execute(sql1);
        });

        String sql2 = "delete ticket_flights "
                + "where flight_id in (select flight_id from flights "
                + "where status=\'Cancelled\');";
        source.statement(stmt -> {
            stmt.execute(sql2);
        });
    }

    /**
     * 8. Написать запрос на добавление нового билета.
     * При указании рейса и места в самолёте делать проверку, что соответствующие рейс и место существуют.
     *
     * @param ticketNo
     * @param flightId
     * @param boardingNo
     * @param seatNo
     * @throws SQLException
     * @throws ParseException
     * @throws IOException
     */
    public void addNewBoardingPass(Integer ticketNo, Integer flightId, Integer boardingNo, String seatNo)
            throws SQLException, ParseException, IOException {
        String checkFLightId = "select exists(select *  from flights where flight_id = ? );";
        boolean existsFlightId = source.preparedStatement(checkFLightId, stmt -> {
            stmt.setInt(1, flightId);
            ResultSet resultSet = stmt.executeQuery();
            resultSet.next();
            return resultSet.getInt(1) == 1;
        });
        if (!existsFlightId) {
            System.out.println("ERROR: flight_id not found");
            return;
        }

        String sql = "select aircraft_code from flights where flight_id = ?;";
        String aircraftCode = source.preparedStatement(sql, stmt -> {
            stmt.setInt(1, flightId);
            ResultSet resultSet = stmt.executeQuery();
            resultSet.next();
            return resultSet.getString(1);
        });

        String checkSeatNo = "select exists(select *  from seats where aircraft_code = ?);";
        boolean existsSeatNo = source.preparedStatement(checkSeatNo, stmt -> {
            stmt.setString(1, aircraftCode);
            ResultSet resultSet = stmt.executeQuery();
            resultSet.next();
            return resultSet.getInt(1) == 1;
        });
        if (!existsSeatNo) {
            System.out.println("ERROR: seat_no not found");
            return;
        }

        String sql1 = "insert into boarding_passes values (?, ?, ?, ?);";
        source.preparedStatement(sql1, stmt -> {
            stmt.setInt(1, ticketNo);
            stmt.setInt(2, flightId);
            stmt.setInt(3, boardingNo);
            stmt.setString(4, seatNo);
            stmt.execute();
        });
    }
}
