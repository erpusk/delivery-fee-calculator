package ee.fujitsu.delivery.controller;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ee.fujitsu.delivery.domain.City;
import ee.fujitsu.delivery.domain.VehicleType;
import ee.fujitsu.delivery.service.DeliveryFeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Delivery fees", description = "Delivery fee calculation")
@RestController 
@RequestMapping("/api/delivery-fees")
public class DeliveryFeeController {
    private final DeliveryFeeService deliveryFeeService;

    public DeliveryFeeController(DeliveryFeeService deliveryFeeService) {
        this.deliveryFeeService = deliveryFeeService;
    }

    @Operation(
        summary = "Calculate a delivery fee",
        description = "Calculates the fee using the latest weather observation or the latest observation before the optionally given time")
    @GetMapping
    public BigDecimal getDeliveryFee(
            @Parameter(description = "City name: Tallinn, Tartu or Pärnu")
            @RequestParam String city,

            @Parameter(description = "Vehicle type: car, scooter or bike")
            @RequestParam String vehicleType,

            @Parameter(description = "Optional datetime", example = "2026-10-18T20:00:00")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime dateTime) {

        City selectedCity = City.fromDisplayName(city);
        VehicleType selectedVehicle;

        try {
            selectedVehicle = VehicleType.valueOf(vehicleType.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unsupported vehicle type: " + vehicleType);
        }

        Instant requestedAt = dateTime == null ? null : dateTime.atZone(ZoneId.of("Europe/Tallinn")).toInstant();

        BigDecimal totalFee = deliveryFeeService.calculate(selectedCity, selectedVehicle, requestedAt);

        return totalFee;
    }
}
