package ee.fujitsu.delivery.persistence;

import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import ee.fujitsu.delivery.domain.City;

public interface WeatherObservationRepository 
    extends JpaRepository<WeatherObservation, Long> {
    Optional<WeatherObservation> findFirstByCityAndObservedAtLessThanEqualOrderByObservedAtDesc(
        City city, Instant time
    );
}
