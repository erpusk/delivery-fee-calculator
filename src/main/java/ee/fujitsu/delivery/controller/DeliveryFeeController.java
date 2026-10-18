package ee.fujitsu.delivery.controller;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ee.fujitsu.delivery.domain.City;
import ee.fujitsu.delivery.domain.VehicleType;
import ee.fujitsu.delivery.service.DeliveryFeeService;

@RestController 
@RequestMapping("/api/delivery-fees")
public class DeliveryFeeController {
    private final DeliveryFeeService deliveryFeeService;

    public DeliveryFeeController(DeliveryFeeService deliveryFeeService) {
        this.deliveryFeeService = deliveryFeeService;
    }

    @GetMapping
    public BigDecimal getDeliveryFee(
            @RequestParam String city,
            @RequestParam String vehicleType,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            OffsetDateTime time) {

        City selectedCity = City.fromDisplayName(city);
        VehicleType selectedVehicle;

        try {
            selectedVehicle = VehicleType.valueOf(vehicleType.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unsupported vehicle type: " + vehicleType);
        }

        BigDecimal totalFee = deliveryFeeService.calculate(selectedCity, selectedVehicle, time == null ? null : time.toInstant());

        return totalFee;
    }
}
