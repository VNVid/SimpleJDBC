package videneevanadezhda.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CityInfo {
    private String cityName;
    private String dayName;
    private int flightNumberArrived;
    private int flightNumberDeparture;
}
