package ee.fujitsu.delivery.domain;

import java.math.BigDecimal;

public record RegionalBaseFeeRule (City city, VehicleType vehicleType, BigDecimal amount) {
    }
