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
import ee.fujitsu.delivery.domain.City;
import ee.fujitsu.delivery.domain.RegionalBaseFeeRule;
import ee.fujitsu.delivery.domain.VehicleType;
import ee.fujitsu.delivery.service.RegionalBaseFeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Regional base fees")
@RestController
@RequestMapping("/api/base-fees")
public class RegionalBaseFeeController {
    private final RegionalBaseFeeService service;
    
    public RegionalBaseFeeController(RegionalBaseFeeService regionalBaseFeeService) {
        this.service = regionalBaseFeeService;
    }

    @Operation(summary = "List all base fees")
    @GetMapping
    public List<RegionalBaseFeeRule> getAll() {
        return service.findAll();
    }

    @Operation(summary = "Create a base fee")
    @PostMapping
    public RegionalBaseFeeRule create(@Valid @RequestBody RegionalBaseFeeRule rule) {
        return service.create(rule);
    }

    @Operation(summary = "Update a base fee")
    @PutMapping("/{city}/{vehicleType}")
    public RegionalBaseFeeRule update(
            @PathVariable City city, 
            @PathVariable VehicleType vehicleType,
            @Valid @RequestBody BaseFeeUpdateRequest request) {
        return service.update(city, vehicleType, request.amount());
    }
    
    @Operation(summary = "Delete a base fee")
    @DeleteMapping("/{city}/{vehicleType}")
    public ResponseEntity<Void> delete(
            @PathVariable City city,
            @PathVariable VehicleType vehicleType) {
        service.delete(city, vehicleType);
        return ResponseEntity.noContent().build();
    }
    
    public record BaseFeeUpdateRequest(
            @NotNull @DecimalMin("0.00") BigDecimal amount) {}
}
