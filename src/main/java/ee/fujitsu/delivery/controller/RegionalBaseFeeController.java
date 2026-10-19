package ee.fujitsu.delivery.controller;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ee.fujitsu.delivery.domain.City;
import ee.fujitsu.delivery.domain.RegionalBaseFeeRule;
import ee.fujitsu.delivery.domain.VehicleType;
import ee.fujitsu.delivery.service.RegionalBaseFeeService;
import io.swagger.v3.oas.annotations.parameters.RequestBody;

@RestController
@RequestMapping("/api/base-fees")
public class RegionalBaseFeeController {
    private final RegionalBaseFeeService service;
    
    public RegionalBaseFeeController(RegionalBaseFeeService regionalBaseFeeService) {
        this.service = regionalBaseFeeService;
    }

    @GetMapping
    public List<RegionalBaseFeeRule> getAll() {
        return service.findAll();
    }

    @PostMapping
    public RegionalBaseFeeRule create(@RequestBody RegionalBaseFeeRule rule) {
        return service.create(rule);
    }

    @PutMapping("/{city}/{vehicleType}")
    public RegionalBaseFeeRule update(
            @PathVariable City city, 
            @PathVariable VehicleType vehicleType,
            @RequestBody UpdateRequest request) {
        return service.update(city, vehicleType, request.amount());
    }
    
    @DeleteMapping("/{city}/{vehicleType}")
    public ResponseEntity<Void> delete(
            @PathVariable City city,
            @PathVariable VehicleType vehicleType) {
        service.delete(city, vehicleType);
        return ResponseEntity.noContent().build();
    }
    
    public record UpdateRequest(BigDecimal amount) {}
}
