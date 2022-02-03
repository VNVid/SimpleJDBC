package videneevanadezhda.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Flight {
    private String  departureCity;
    private String arrivalAirportCode;
    private String time;
}
