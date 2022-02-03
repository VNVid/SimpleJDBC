package videneevanadezhda;

import org.javatuples.Pair;
import videneevanadezhda.domain.CityInfo;
import videneevanadezhda.domain.Flight;
import videneevanadezhda.service.dao.*;
import videneevanadezhda.service.db.DbFill;
import videneevanadezhda.service.db.DbInit;
import videneevanadezhda.service.db.SimpleJdbcTemplate;
import com.opencsv.exceptions.CsvValidationException;
import org.h2.jdbcx.JdbcConnectionPool;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

public class TestAll {
    private SimpleJdbcTemplate source = new SimpleJdbcTemplate(
            JdbcConnectionPool.create("jdbc:h2:mem:database;DB_CLOSE_DELAY=-1", "", ""));

    @BeforeEach
    void setupDB() throws IOException, SQLException {
        new DbInit(source).create();
        DbFill fill = new DbFill(source);
        try {
            fill.fillAircrafts();
            fill.fillFlights();
            fill.fillAirports();
            fill.fillTicketFlights();
            fill.fillSeats();
            fill.fillBookings();
            fill.fillBoardingPasses();
            fill.fillTickets();
        } catch (CsvValidationException e) {
            e.printStackTrace();
        }
    }

    @AfterEach
    void tearDownDB() throws SQLException {
        source.statement(stmt -> {
            stmt.execute("drop all objects;");
        });
    }

    @Test
    public void testAll() throws SQLException, IOException, ParseException {

        // задача В.1
        AirportDao a = new AirportDao(source);
        Set<String> cities = a.getCitiesWithSeveralAirports();
        ArrayList<Pair<String, String>> cityAirports = new ArrayList<>();
        for (String i : cities) {
            Set<String> airports = a.getAirportsByCity(i);
            String airportsString = String.join(", ", airports);
            cityAirports.add(new Pair<>(i, airportsString));
        }
        ArrayList<String> columnNamesCitiesWithSeveralAirports
                = new ArrayList<>(Arrays.asList("City", "Airports"));
        new Tables().createTableFromPair(cityAirports,
                "CitiesWithSeveralAirports", columnNamesCitiesWithSeveralAirports);


        //Задача В.2
        FlightDao f = new FlightDao(source);
        ArrayList<Pair<String, Integer>> citiesMostFrequentCancelledFlights
                = f.getCitiesMostFrequentCancelledFlights();
        ArrayList<String> columnNamesCitiesMostFrequentCancelledFlights
                = new ArrayList<>(Arrays.asList("City", "Number of cancelled flights"));
        new Tables().createTableFromPair(citiesMostFrequentCancelledFlights,
                "CitiesMostFrequentCancelledFlights", columnNamesCitiesMostFrequentCancelledFlights);


        // Задача В.3
        Set<Flight> shortestFlights = f.getShortestTimeForCity();
        ArrayList<ArrayList<String>> flights = new ArrayList<>();
        for (Flight i : shortestFlights) {
            flights.add(new ArrayList<>(Arrays.asList(i.getDepartureCity(), i.getArrivalAirportCode(), i.getTime())));
        }
        ArrayList<String> columnNamesShortestFlights
                = new ArrayList<>(Arrays.asList("City", "Arrival airport", "Time (in mins)"));
        new Tables().createTableFromArray(flights,
                "ShortestFlights", columnNamesShortestFlights);


        //Задача В.4
        ArrayList<Pair<String, Integer>> monthCancelled = f.getNumberCancelled();
        ArrayList<String> columnNamesMonthCancelled
                = new ArrayList<>(Arrays.asList("Month", "Number of cancelled flights"));
        new Tables().createTableFromPair(monthCancelled,
                "NumberOfCancelledFlights", columnNamesMonthCancelled);
        ArrayList<String> rowKey = new ArrayList<>();
        rowKey.add("month");
        rowKey.add("");
        new Charts().createChart(monthCancelled, new ArrayList<>(), "Number of cancelled flights", "Month", "Number",
                "NumberOfCancelledFlights", rowKey);


        //Задача В.5
        ArrayList<CityInfo> moscowStats = f.getInfoForMoscow();
        ArrayList<Pair<String, Integer>> flightsNumberArrivedToMoscow = new ArrayList<>();
        ArrayList<Pair<String, Integer>> flightsNumberDepartedFromMoscow = new ArrayList<>();
        for (CityInfo i : moscowStats) {
            flightsNumberArrivedToMoscow.add(new Pair<>(i.getDayName(), i.getFlightNumberArrived()));
            flightsNumberDepartedFromMoscow.add(new Pair<>(i.getDayName(), i.getFlightNumberDeparture()));
        }
        ArrayList<String> columnNamesMoscowStats
                = new ArrayList<>(Arrays.asList("Day", "Number of flights"));
        new Tables().createTableFromPair(flightsNumberArrivedToMoscow,
                "flightsNumberArrivedToMoscow", columnNamesMoscowStats);
        new Tables().createTableFromPair(flightsNumberDepartedFromMoscow,
                "flightsNumberDepartedFromMoscow", columnNamesMoscowStats);
        rowKey.clear();
        rowKey.add("arrived");
        rowKey.add("departed");
        new Charts().createChart(flightsNumberArrivedToMoscow, flightsNumberDepartedFromMoscow, "Flights from and to Moscow",
                "Day", "Number of flights", "moscowStats", rowKey);

        //Задача В.6
        TicketDao t = new TicketDao(source);
        f.updateStatusByAircraftModel("Cessna 208 Caravan", t);


        //Задача В.7
        String begin = "2017-08-01 00:00:00";
        String end = "2017-08-15 23:59:59";
        rowKey.clear();
        rowKey.add("Day");
        rowKey.add("");
        ArrayList<Pair<String, Integer>> lossesByDay = f.updateStatusBetweenDatesAndCountLosses(begin, end);
        new Charts().createChart(lossesByDay, new ArrayList<>(), "Losses", "Day", "Amount",
                "losses", rowKey);

        //Задача В.8
        t.addNewBoardingPass(0005435212351, 30625, 1, "2D");

    }
}
