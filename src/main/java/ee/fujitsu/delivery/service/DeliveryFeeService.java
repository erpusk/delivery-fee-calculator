package ee.fujitsu.delivery.service;

import java.math.BigDecimal;
import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import ee.fujitsu.delivery.domain.City;
import ee.fujitsu.delivery.domain.VehicleType;
import ee.fujitsu.delivery.domain.WeatherFeeRule;
import ee.fujitsu.delivery.domain.WeatherFeeRule.Condition;
import ee.fujitsu.delivery.persistence.WeatherObservation;
import ee.fujitsu.delivery.persistence.WeatherObservationRepository;

@Service
public class DeliveryFeeService {

    private final RegionalBaseFeeService regionalBaseFeeService;
    private final WeatherFeeService weatherFeeService;
    private final WeatherObservationRepository weatherObservationRepository;

    public DeliveryFeeService(RegionalBaseFeeService regionalBaseFeeService, WeatherFeeService weatherFeeService, WeatherObservationRepository weatherObservationRepository) {
        this.regionalBaseFeeService = regionalBaseFeeService;
        this.weatherFeeService = weatherFeeService;
        this.weatherObservationRepository = weatherObservationRepository;
    }

    public BigDecimal calculate(City city, VehicleType vehicleType, Instant requestedAt) {
        BigDecimal total = regionalBaseFeeService.find(city, vehicleType).amount();

        if (vehicleType == VehicleType.CAR) {
            return total;
        }

        Instant observationTime = requestedAt == null ? Instant.now() : requestedAt;

        WeatherObservation observation = weatherObservationRepository
            .findFirstByCityAndObservedAtLessThanEqualOrderByObservedAtDesc(city, observationTime)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No weather observation is available for " + city.displayName()));

        Double temperature = observation.getAirTemperature();
        if (temperature == null) throw new IllegalStateException("Air temperature is unavailable");

        if (temperature < -10.0) {
            total = addRuleFee(total, vehicleType, Condition.TEMPERATURE_BELOW_MINUS_10);
        } else if (temperature <= 0.0) {
            total = addRuleFee(total, vehicleType, Condition.TEMPERATURE_MINUS_10_TO_0);
        }

        if (vehicleType == VehicleType.BIKE) {
            Double windspeed = observation.getWindSpeed();
            if (windspeed == null) throw new IllegalStateException("Wind speed is unavailable");
            
            if (windspeed > 20.0) {
                total = addRuleFee(total, vehicleType, Condition.WIND_ABOVE_20);
            } else if (windspeed >= 10.0) {
                total = addRuleFee(total, vehicleType, Condition.WIND_10_TO_20);
            }
        }

        String phenomenon = observation.getPhenomenon();
        if (phenomenon == null || phenomenon.isBlank()) throw new IllegalStateException("Weather phenomenon is unavailable");

        phenomenon = phenomenon.toLowerCase();
        
        if (phenomenon.contains("glaze") || phenomenon.contains("hail") || phenomenon.contains("thunder")) {
            total = addRuleFee(total, vehicleType, Condition.DANGEROUS_WEATHER);
        } else if (phenomenon.contains("rain")) {
            total = addRuleFee(total, vehicleType, Condition.RAIN);
        } else if (phenomenon.contains("snow") || phenomenon.contains("sleet")) {
            total = addRuleFee(total, vehicleType, Condition.SNOW_OR_SLEET);
        }

        return total;
    }

    private BigDecimal addRuleFee(BigDecimal total, VehicleType vehicleType, Condition condition) {
        WeatherFeeRule rule = weatherFeeService.find(vehicleType, condition);

        if (rule == null) return total;

        if (rule.forbidden()) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Usage of selected vehicle type is forbidden"
            );
        }

        return total.add(rule.amount());
    }
}
