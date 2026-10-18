package ee.fujitsu.delivery.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import ee.fujitsu.delivery.domain.RegionalBaseFeeRule;
import ee.fujitsu.delivery.domain.City;
import ee.fujitsu.delivery.domain.VehicleType;

@Service
public class RegionalBaseFeeService {
    
    private final List<RegionalBaseFeeRule> rules = new ArrayList<>(List.of(
        new RegionalBaseFeeRule(City.TALLINN, VehicleType.CAR, new BigDecimal("4.00")),
        new RegionalBaseFeeRule(City.TALLINN, VehicleType.SCOOTER, new BigDecimal("3.50")),
        new RegionalBaseFeeRule(City.TALLINN, VehicleType.BIKE, new BigDecimal("3.00")),
        new RegionalBaseFeeRule(City.TARTU, VehicleType.CAR, new BigDecimal("3.50")),
        new RegionalBaseFeeRule(City.TARTU, VehicleType.SCOOTER, new BigDecimal("3.00")),
        new RegionalBaseFeeRule(City.TARTU, VehicleType.BIKE, new BigDecimal("2.50")),
        new RegionalBaseFeeRule(City.PARNU, VehicleType.CAR, new BigDecimal("3.00")),
        new RegionalBaseFeeRule(City.PARNU, VehicleType.SCOOTER, new BigDecimal("2.50")),
        new RegionalBaseFeeRule(City.PARNU, VehicleType.BIKE, new BigDecimal("2.00"))
    ));

    public synchronized List<RegionalBaseFeeRule> findAll() {
        return List.copyOf(rules);
    }

    public synchronized RegionalBaseFeeRule find(City city, VehicleType vehicleType) {
        return rules.stream()
            .filter(rule -> rule.city() == city && rule.vehicleType() == vehicleType)
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Regional base fee rule not found"));
    }

    public synchronized RegionalBaseFeeRule create(RegionalBaseFeeRule newRule) {
        boolean exists = rules.stream()
            .anyMatch(rule -> rule.city() == newRule.city() && rule.vehicleType() == newRule.vehicleType());

        if (exists) throw new IllegalArgumentException("A base fee already exists for that city and vehicle");

        rules.add(newRule);
        return newRule;
    }

    public synchronized RegionalBaseFeeRule update(
            City city,
            VehicleType vehicleType,
            BigDecimal amount) {

        for (int i = 0; i < rules.size(); i++) {
            RegionalBaseFeeRule existing = rules.get(i);

            if (existing.city() == city && existing.vehicleType() == vehicleType) {
                RegionalBaseFeeRule updated = new RegionalBaseFeeRule(city, vehicleType, amount);
                rules.set(i, updated);
                return updated;
            }
        }

        throw new IllegalArgumentException("Regional base fee rule not found");
    }

    public synchronized void delete(City city, VehicleType vehicleType) {
        boolean removed = rules.removeIf(
            rule -> rule.city() == city && rule.vehicleType() == vehicleType);

        if (!removed) {
            throw new IllegalArgumentException("Regional base fee rule not found");
        }
    }
}