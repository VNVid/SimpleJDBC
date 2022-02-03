package videneevanadezhda.service.db;

import com.opencsv.exceptions.CsvValidationException;
import lombok.AllArgsConstructor;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.opencsv.CSVReader;

/**
 * Fills database with data from csv files
 */
@AllArgsConstructor
public class DbFill {
    private final SimpleJdbcTemplate source;

    static final String INSERT_AIRCRAFT = "INSERT INTO aircrafts(aircraft_code, model, range) VALUES (?, ?, ?)";
    static final String INSERT_AIRPORT = "INSERT INTO airports(airport_code, airport_name,"
            + " city, coordinates, timezone) VALUES (?, ?, ?, ?, ?)";
    static final String INSERT_BOARDING_PASS = "INSERT INTO boarding_passes(ticket_no, flight_id,"
            + " boarding_no, seat_no) VALUES (?, ?, ?, ?)";
    static final String INSERT_BOOKING = "INSERT INTO bookings(book_ref, book_date, total_amount) VALUES (?, ?, ?)";
    static final String INSERT_FLIGHT = "INSERT INTO flights(flight_id, flight_no, scheduled_departure,"
            + " scheduled_arrival, departure_airport, arrival_airport, status, aircraft_code, "
            + "actual_departure, actual_arrival) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    static final String INSERT_SEAT = "INSERT INTO seats(aircraft_code, seat_no, fare_conditions) VALUES (?, ?, ?)";
    static final String INSERT_TICKET_FLIGHTS = "INSERT INTO ticket_flights(ticket_no, flight_id,"
            + " fare_conditions, amount) VALUES (?, ?, ?, ?)";
    static final String INSERT_TICKETS = "INSERT INTO tickets(ticket_no, book_ref,"
            + " passenger_id, passenger_name, contact_data) VALUES (?, ?, ?, ?, ?)";


    /**
     * Fills aircrafts table with data
     *
     * @throws SQLException
     * @throws IOException
     * @throws CsvValidationException
     */
    public void fillAircrafts() throws SQLException, IOException, CsvValidationException {
        try (CSVReader reader = new CSVReader(
                new FileReader("src/main/resources/videneevanadezhda/aircrafts_data.csv"))) {
            while (true) {
                final String[] rec = reader.readNext();
                if (rec == null) {
                    break;
                }
                final int prefsize = 8;
                final int sufsize = 3;
                source.preparedStatement(INSERT_AIRCRAFT, insertAircraft -> {
                    String regexp = "\\{\"en\": \".*\", ";
                    Pattern pattern = Pattern.compile(regexp);
                    Matcher matcher = pattern.matcher(rec[1]);
                    matcher.find();

                    insertAircraft.setString(1, rec[0]);
                    insertAircraft.setString(2, rec[1].substring(matcher.start() + prefsize,
                            matcher.end() - sufsize));
                    insertAircraft.setInt(3, Integer.valueOf(rec[2]));
                    insertAircraft.execute();
                });
            }
        }
    }

    /**
     * Fills airports table with data
     *
     * @throws SQLException
     * @throws IOException
     * @throws CsvValidationException
     */
    public void fillAirports() throws SQLException, IOException, CsvValidationException {
        try (CSVReader reader = new CSVReader(
                new FileReader("src/main/resources/videneevanadezhda/airports_data.csv"))) {
            while (true) {
                final String[] rec = reader.readNext();
                if (rec == null) {
                    break;
                }
                final int prefsize = 8;
                final int sufsize = 3;
                source.preparedStatement(INSERT_AIRPORT, insertAirport -> {
                    String regexp = "\\{\"en\": \".*\", ";
                    Pattern pattern = Pattern.compile(regexp);
                    Matcher matcher = pattern.matcher(rec[1]);
                    matcher.find();

                    insertAirport.setString(1, rec[0]);
                    insertAirport.setString(2, rec[1].substring(matcher.start() + prefsize,
                            matcher.end() - sufsize));

                    matcher = pattern.matcher(rec[2]);
                    matcher.find();
                    insertAirport.setString(3, rec[2].substring(matcher.start() + prefsize,
                            matcher.end() - sufsize));
                    insertAirport.setString(4, rec[3]);
                    insertAirport.setString(5, rec[4]);
                    insertAirport.execute();
                });
            }
        }
    }

    /**
     * Fills boarding_passes table with data
     *
     * @throws SQLException
     * @throws IOException
     * @throws CsvValidationException
     */
    public void fillBoardingPasses() throws SQLException, IOException, CsvValidationException {
        try (CSVReader reader = new CSVReader(
                new FileReader("src/main/resources/videneevanadezhda/boarding_passes.csv"))) {
            while (true) {
                final String[] rec = reader.readNext();
                if (rec == null) {
                    break;
                }

                source.preparedStatement(INSERT_BOARDING_PASS, insertBoardingPass -> {
                    insertBoardingPass.setString(1, rec[0]);
                    insertBoardingPass.setInt(2, Integer.valueOf(rec[1]));
                    insertBoardingPass.setInt(3, Integer.valueOf(rec[2]));
                    insertBoardingPass.setString(4, rec[3]);
                    insertBoardingPass.execute();
                });
            }
        }
    }

    /**
     * Fills bookings table with data
     *
     * @throws SQLException
     * @throws IOException
     * @throws CsvValidationException
     */
    public void fillBookings() throws SQLException, IOException, CsvValidationException {
        try (CSVReader reader = new CSVReader(
                new FileReader("src/main/resources/videneevanadezhda/bookings.csv"))) {
            while (true) {
                final String[] rec = reader.readNext();
                if (rec == null) {
                    break;
                }

                final int timeZoneLen = 3;

                source.preparedStatement(INSERT_BOOKING, insertAircraft -> {
                    insertAircraft.setString(1, rec[0]);
                    insertAircraft.setTimestamp(2,
                            Timestamp.valueOf(rec[1].substring(0, rec[1].length() - timeZoneLen)));
                    insertAircraft.setDouble(3, Double.valueOf(rec[2]));
                    insertAircraft.execute();
                });
            }
        }
    }

    /**
     * Fills flights table with data
     *
     * @throws SQLException
     * @throws IOException
     * @throws CsvValidationException
     */
    public void fillFlights() throws SQLException, IOException, CsvValidationException {
        try (CSVReader reader = new CSVReader(
                new FileReader("src/main/resources/videneevanadezhda/flights.csv"))) {
            while (true) {
                final String[] rec = reader.readNext();
                if (rec == null) {
                    break;
                }

                // Just to avoid checkstyle violations
                final int index8 = 8;
                final int index9 = 9;
                final int index10 = 10;
                final int timeZoneLen = 3;

                source.preparedStatement(INSERT_FLIGHT, insertFlight -> {
                    insertFlight.setInt(1, Integer.valueOf(rec[0]));
                    insertFlight.setString(2, rec[1]);
                    insertFlight.setTimestamp(3, Timestamp.valueOf(rec[2].substring(0, rec[2].length() - timeZoneLen)));
                    insertFlight.setTimestamp(4, Timestamp.valueOf(rec[3].substring(0, rec[3].length() - timeZoneLen)));
                    insertFlight.setString(5, rec[4]);
                    insertFlight.setString(6, rec[5]);
                    insertFlight.setString(7, rec[6]);
                    insertFlight.setString(index8, rec[7]);
                    if (rec[index8].length() > 0) {
                        insertFlight.setTimestamp(index9,
                                Timestamp.valueOf(rec[index8].substring(0, rec[index8].length() - timeZoneLen)));
                    } else {
                        insertFlight.setTimestamp(index9, null);
                    }
                    if (rec[index9].length() > 0) {
                        insertFlight.setTimestamp(index10,
                                Timestamp.valueOf(rec[index9].substring(0, rec[index9].length() - timeZoneLen)));
                    } else {
                        insertFlight.setTimestamp(index10, null);
                    }
                    insertFlight.execute();
                });
            }
        }
    }

    /**
     * Fills seats table with data
     *
     * @throws SQLException
     * @throws IOException
     * @throws CsvValidationException
     */
    public void fillSeats() throws SQLException, IOException, CsvValidationException {
        try (CSVReader reader = new CSVReader(
                new FileReader("src/main/resources/videneevanadezhda/seats.csv"))) {
            while (true) {
                final String[] rec = reader.readNext();
                if (rec == null) {
                    break;
                }

                source.preparedStatement(INSERT_SEAT, insertSeat -> {
                    insertSeat.setString(1, rec[0]);
                    insertSeat.setString(2, rec[1]);
                    insertSeat.setString(3, rec[2]);
                    insertSeat.execute();
                });
            }
        }
    }

    /**
     * Fills ticket_flights table with data
     *
     * @throws SQLException
     * @throws IOException
     * @throws CsvValidationException
     */
    public void fillTicketFlights() throws SQLException, IOException, CsvValidationException {
        try (CSVReader reader = new CSVReader(
                new FileReader("src/main/resources/videneevanadezhda/ticket_flights.csv"))) {
            while (true) {
                final String[] rec = reader.readNext();
                if (rec == null) {
                    break;
                }

                source.preparedStatement(INSERT_TICKET_FLIGHTS, insertTicketFlights -> {
                    insertTicketFlights.setString(1, rec[0]);
                    insertTicketFlights.setInt(2, Integer.parseInt(rec[1]));
                    insertTicketFlights.setString(3, rec[2]);
                    insertTicketFlights.setDouble(4, Double.parseDouble(rec[3]));
                    insertTicketFlights.execute();
                });
            }
        }
    }

    /**
     * Fills tickets table with data
     *
     * @throws SQLException
     * @throws IOException
     * @throws CsvValidationException
     */
    public void fillTickets() throws SQLException, IOException, CsvValidationException {
        try (CSVReader reader = new CSVReader(
                new FileReader("src/main/resources/videneevanadezhda/tickets.csv"))) {
            while (true) {
                final String[] rec = reader.readNext();
                if (rec == null) {
                    break;
                }

                source.preparedStatement(INSERT_TICKETS, insertTicket -> {
                    insertTicket.setString(1, rec[0]);
                    insertTicket.setString(2, rec[1]);
                    insertTicket.setString(3, rec[2]);
                    insertTicket.setString(4, rec[3]);
                    insertTicket.setString(5, rec[4]);
                    insertTicket.execute();
                });
            }
        }
    }
}
