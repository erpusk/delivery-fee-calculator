package ee.fujitsu.delivery.controller;

import java.math.BigDecimal;
import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ee.fujitsu.delivery.domain.VehicleType;
import ee.fujitsu.delivery.domain.WeatherFeeRule;
import ee.fujitsu.delivery.domain.WeatherFeeRule.Condition;
import ee.fujitsu.delivery.service.WeatherFeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Weather fees")
@RestController 
@RequestMapping("/api/weather-fees")
public class WeatherFeeController {
    
    private final WeatherFeeService service;

    public WeatherFeeController(WeatherFeeService weatherFeeService) {
        this.service = weatherFeeService;
    }

    @Operation(summary = "List all weather fee rules")
    @GetMapping
    public List<WeatherFeeRule> getAll() {
        return service.findAll();
    }

    @Operation(summary = "Create a weather fee rule")
    @PostMapping
    public WeatherFeeRule create(@Valid @RequestBody WeatherFeeRule rule) {
        return service.create(rule);
    }

    @Operation(summary = "Update a weather fee rule")
    @PutMapping("/{vehicleType}/{condition}")
    public WeatherFeeRule update(
            @PathVariable VehicleType vehicleType,
            @PathVariable Condition condition,
            @Valid @RequestBody WeatherFeeUpdateRequest request) {
        return service.update(vehicleType, condition, request.amount(), request.forbidden());
    }

    @Operation(summary = "Delete a weather fee rule")
    @DeleteMapping("/{vehicleType}/{condition}")
    public ResponseEntity<Void> delete(
            @PathVariable VehicleType vehicleType,
            @PathVariable Condition condition) {
        service.delete(vehicleType, condition);
        return ResponseEntity.noContent().build();
    }

    private record WeatherFeeUpdateRequest(
            @NotNull @DecimalMin("0.00") BigDecimal amount,
            boolean forbidden) {}
}
