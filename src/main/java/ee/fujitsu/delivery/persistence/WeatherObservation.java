package ee.fujitsu.delivery.persistence;

import java.math.BigDecimal;
import java.time.Instant;

import ee.fujitsu.delivery.domain.City;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "weather_observations")
public class WeatherObservation {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private City city;

    @Column(nullable = false)
    private String stationName;

    @Column(nullable = false)
    private String wmoCode;

    private BigDecimal airTemperature;
    private BigDecimal windSpeed;
    private String phenomenon;

    @Column(nullable = false)
    private Instant observedAt;

    protected WeatherObservation() {
    }

    public WeatherObservation(City city, String stationName, String wmoCode, BigDecimal airTemperature, BigDecimal windSpeed, String phenomenon, Instant observedAt) {
        this.city = city;
        this.stationName = stationName;
        this.wmoCode = wmoCode;
        this.airTemperature = airTemperature;
        this.windSpeed = windSpeed;
        this.phenomenon = phenomenon;
        this.observedAt = observedAt;
    }

    public Long getId() {
        return id;
    }

    public City getCity() {
        return city;
    }

    public String getStationName() {
        return stationName;
    }

    public String getWmoCode() {
        return wmoCode;
    }

    public BigDecimal getAirTemperature() {
        return airTemperature;
    }

    public BigDecimal getWindSpeed() {
        return windSpeed;
    }

    public String getPhenomenon() {
        return phenomenon;
    }

    public Instant getObservedAt() {
        return observedAt;
    }
}
