package ee.fujitsu.delivery.domain;

import java.math.BigDecimal;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record WeatherFeeRule (
        @NotNull VehicleType vehicleType,
        @NotNull Condition condition,
        @NotNull @DecimalMin("0.00") BigDecimal amount,
        boolean forbidden) {
    public enum Condition {
        TEMPERATURE_BELOW_MINUS_10,
        TEMPERATURE_MINUS_10_TO_0,
        WIND_10_TO_20,
        WIND_ABOVE_20,
        SNOW_OR_SLEET,
        RAIN,
        DANGEROUS_WEATHER
    }
}
