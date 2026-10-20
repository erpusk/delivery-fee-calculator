package ee.fujitsu.delivery.domain;

import java.math.BigDecimal;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record RegionalBaseFeeRule(
        @NotNull City city,
        @NotNull VehicleType vehicleType,
        @NotNull @DecimalMin("0.00") BigDecimal amount) {
}
