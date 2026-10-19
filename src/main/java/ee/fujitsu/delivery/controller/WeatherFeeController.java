package ee.fujitsu.delivery.controller;

import java.math.BigDecimal;
import java.util.List;
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

@RestController 
@RequestMapping("/api/weather-fees")
public class WeatherFeeController {
    
    private final WeatherFeeService service;

    public WeatherFeeController(WeatherFeeService weatherFeeService) {
        this.service = weatherFeeService;
    }

    @GetMapping
    public List<WeatherFeeRule> getAll() {
        return service.findAll();
    }

    @PostMapping
    public WeatherFeeRule create(@RequestBody WeatherFeeRule rule) {
        return service.create(rule);
    }

    @PutMapping("/{vehicleType}/{condition}")
    public WeatherFeeRule update(
            @PathVariable VehicleType vehicleType,
            @PathVariable Condition condition,
            @RequestBody UpdateRequest request) {
        return service.update(vehicleType, condition, request.amount(), request.forbidden());
    }

    @DeleteMapping("/{vehicleType}/{condition}")
    public ResponseEntity<Void> delete(
            @PathVariable VehicleType vehicleType,
            @PathVariable Condition condition) {
        service.delete(vehicleType, condition);
        return ResponseEntity.noContent().build();
    }

    private record UpdateRequest(BigDecimal amount, boolean forbidden) {}
}
