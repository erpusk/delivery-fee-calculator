package ee.fujitsu.delivery.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import ee.fujitsu.delivery.domain.City;
import ee.fujitsu.delivery.domain.VehicleType;
import ee.fujitsu.delivery.persistence.WeatherObservation;
import ee.fujitsu.delivery.persistence.WeatherObservationRepository;

@ExtendWith(MockitoExtension.class)
class DeliveryFeeServiceTest {

    @Mock
    private WeatherObservationRepository observationRepository;
    private DeliveryFeeService service;

    @BeforeEach 
    void setUp() {
        service = new DeliveryFeeService(
            new RegionalBaseFeeService(), 
            new WeatherFeeService(), 
            observationRepository);
    }

    @ParameterizedTest(name = "{0}'s RBF is {1}")
    @CsvSource({
        "TALLINN, 4.00",
        "TARTU, 3.50",
        "PARNU, 3.00"
    })
    void carGetsOnlyBaseFeeNotWeather(City city, String amount) {
        BigDecimal expectedFee = new BigDecimal(amount);
        BigDecimal actualFee = service.calculate(city, VehicleType.CAR, null);

        assertEquals(expectedFee, actualFee);
        verifyNoInteractions(observationRepository);
    }

    @ParameterizedTest(name = "{0} degrees adds {1}€")
    @CsvSource({
        "-11.0, 1.00",
        "-10.0, 0.50",
        "0.0, 0.50",
        "1.0, 0.00"
    })
    void calculateTemperatureFee(double temperature, String fee) {
        givenWeather(temperature, 0.0, "Clear");

        BigDecimal tallinnScooterBaseFee = new BigDecimal("3.50");
        BigDecimal expectedFee = tallinnScooterBaseFee.add(new BigDecimal(fee));
        BigDecimal actualFee = service.calculate(City.TALLINN, VehicleType.SCOOTER, null);

        assertEquals(expectedFee, actualFee);
    }

    @ParameterizedTest(name = "Bike wind {0} m/s adds {1}€")
    @CsvSource({
        "9.0, 0.00",
        "10.0, 0.50",
        "20.0, 0.50"
    })
    void calculateBikeWindFee(double windSpeed, String fee) {
        givenWeather(5.0, windSpeed, "Clear");

        BigDecimal tallinnBikeBaseFee = new BigDecimal("3.00");
        BigDecimal expectedFee = tallinnBikeBaseFee.add(new BigDecimal(fee));
        BigDecimal actualFee = service.calculate(City.TALLINN, VehicleType.BIKE, null);

        assertEquals(expectedFee, actualFee);
    }

    @ParameterizedTest(name = "{0} in phenomenon {1} adds {3}€")
    @CsvSource({
        "SCOOTER, Snow, 3.50, 1.00",
        "SCOOTER, Sleet, 3.50, 1.00",
        "SCOOTER, Rain, 3.50, 0.50",
        "BIKE, Snow, 3.00, 1.00",
        "BIKE, Sleet, 3.00, 1.00",
        "BIKE, Rain, 3.00, 0.50"
    })
    void calculatePhenomenonFee(String vehicleName, String phenomenon, String baseFee, String fee) {
        VehicleType vehicleType = VehicleType.valueOf(vehicleName);
        givenWeather(5.0, 2.0, phenomenon);

        BigDecimal expectedFee = new BigDecimal(baseFee).add(new BigDecimal(fee));
        BigDecimal actualFee = service.calculate(City.TALLINN, vehicleType, null);

        assertEquals(expectedFee, actualFee);
    }

    @ParameterizedTest(name = "{0} is forbidden with wind {1} and phenomenon {2}")
    @CsvSource({
        "BIKE, 21.0, Clear",
        "BIKE, 2.0, Glaze",
        "BIKE, 2.0, Hail",
        "BIKE, 2.0, Thunderstorm",
        "SCOOTER, 2.0, Glaze",
        "SCOOTER, 2.0, Hail",
        "SCOOTER, 2.0, Thunderstorm"
    })
    void forbiddenWeatherThrows(String vehicleName, double windSpeed, String phenomenon) {
        givenWeather(5.0, windSpeed, phenomenon);
        VehicleType vehicleType = VehicleType.valueOf(vehicleName);

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> service.calculate(City.TALLINN, vehicleType, null));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Usage of selected vehicle type is forbidden", exception.getReason());
    }


    @Test 
    void usesWeatherObservationFromBeforeRequestedTime() {
        Instant now = Instant.now();
        Instant requestedAt = now.minus(Duration.ofMinutes(15));
        Instant previousObservedAt = now.minus(Duration.ofMinutes(30));

        givenWeather(5.0, 1.0, "Clear"); // add an observation with +0 EUR fee

        WeatherObservation previousObservation = new WeatherObservation( // add an observation with +2 EUR fee
            City.TALLINN, 
            "Tallinn-Harku",
            "26038", 
            -2.1, 
            14.7, 
            "Light snow shower",
            previousObservedAt);

        when(observationRepository
            .findFirstByCityAndObservedAtLessThanEqualOrderByObservedAtDesc(
                City.TALLINN, requestedAt))
            .thenReturn(Optional.of(previousObservation));

        BigDecimal fee = service.calculate(City.TALLINN, VehicleType.BIKE, requestedAt);

        // Tallinn bike base 3 EUR + cold-weather 0.50 EUR + heavy winds 0.50 EUR + snow 1 EUR = 5EUR
        assertEquals(new BigDecimal("5.00"), fee);
    }

    private void givenWeather(double temperature, double windSpeed, String phenomenon) {
        WeatherObservation observation = new WeatherObservation(
            City.TALLINN, 
            "Tallinn-Harku", 
            "26038", 
            temperature, 
            windSpeed, 
            phenomenon,
            Instant.now());

        when(observationRepository
            .findFirstByCityAndObservedAtLessThanEqualOrderByObservedAtDesc(
                eq(City.TALLINN), any(Instant.class)))
            .thenReturn(Optional.of(observation));
    }
}
