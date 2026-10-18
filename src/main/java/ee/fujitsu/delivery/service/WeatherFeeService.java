package ee.fujitsu.delivery.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import ee.fujitsu.delivery.domain.VehicleType;
import ee.fujitsu.delivery.domain.WeatherFeeRule;
import ee.fujitsu.delivery.domain.WeatherFeeRule.Condition;

@Service
public class WeatherFeeService {
    private final List<WeatherFeeRule> rules = new ArrayList<>(List.of(
        new WeatherFeeRule(VehicleType.SCOOTER, Condition.TEMPERATURE_BELOW_MINUS_10, new BigDecimal("1.00"), false),
        new WeatherFeeRule(VehicleType.SCOOTER, Condition.TEMPERATURE_MINUS_10_TO_0, new BigDecimal("0.50"), false),
        new WeatherFeeRule(VehicleType.BIKE, Condition.TEMPERATURE_BELOW_MINUS_10, new BigDecimal("1.00"), false),
        new WeatherFeeRule(VehicleType.BIKE, Condition.TEMPERATURE_MINUS_10_TO_0, new BigDecimal("0.50"), false),
        new WeatherFeeRule(VehicleType.BIKE, Condition.WIND_10_TO_20, new BigDecimal("0.50"), false),
        new WeatherFeeRule(VehicleType.BIKE, Condition.WIND_ABOVE_20, BigDecimal.ZERO, true),
        new WeatherFeeRule(VehicleType.SCOOTER, Condition.SNOW_OR_SLEET, new BigDecimal("1.00"), false),
        new WeatherFeeRule(VehicleType.BIKE, Condition.SNOW_OR_SLEET, new BigDecimal("1.00"), false),
        new WeatherFeeRule(VehicleType.SCOOTER, Condition.RAIN, new BigDecimal("0.50"), false),
        new WeatherFeeRule(VehicleType.BIKE, Condition.RAIN, new BigDecimal("0.50"), false),
        new WeatherFeeRule(VehicleType.SCOOTER, Condition.DANGEROUS_WEATHER, BigDecimal.ZERO, true),
        new WeatherFeeRule(VehicleType.BIKE, Condition.DANGEROUS_WEATHER, BigDecimal.ZERO, true)
    ));

    public synchronized List<WeatherFeeRule> findAll() {
        return List.copyOf(rules);
    }

    public synchronized WeatherFeeRule find(VehicleType vehicleType, Condition condition) {
        return rules.stream()
            .filter(rule -> rule.vehicleType() == vehicleType && rule.condition() == condition)
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Weather fee rule not found"));
    }

    public synchronized WeatherFeeRule create(WeatherFeeRule newRule) {
        boolean exists = rules.stream()
            .anyMatch(rule -> rule.vehicleType() == newRule.vehicleType()
                && rule.condition() == newRule.condition());

        if (exists) {
            throw new IllegalArgumentException("A rule already exists for that vehicle and condition");
        }

        rules.add(newRule);
        return newRule;
    }

    public synchronized WeatherFeeRule update(
            VehicleType vehicleType,
            Condition condition,
            BigDecimal amount,
            boolean forbidden) {

        for (int i = 0; i < rules.size(); i++) {
            WeatherFeeRule existing = rules.get(i);

            if (existing.vehicleType() == vehicleType && existing.condition() == condition) {
                WeatherFeeRule updated =
                    new WeatherFeeRule(vehicleType, condition, amount, forbidden);
                rules.set(i, updated);
                return updated;
            }
        }
        throw new IllegalArgumentException("Weather fee rule not found");
    }

    public synchronized void delete(VehicleType vehicleType, Condition condition) {
        boolean removed = rules.removeIf(
            rule -> rule.vehicleType() == vehicleType && rule.condition() == condition);

        if (!removed) {
            throw new IllegalArgumentException("Weather fee rule not found");
        }
    }
}
