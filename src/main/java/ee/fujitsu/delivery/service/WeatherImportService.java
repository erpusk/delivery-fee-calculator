package ee.fujitsu.delivery.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import ee.fujitsu.delivery.persistence.WeatherObservation;
import ee.fujitsu.delivery.persistence.WeatherObservationRepository;

@Service
public class WeatherImportService {
    private static final Logger log = LoggerFactory.getLogger(WeatherImportService.class);
    private final RestClient restClient;
    private final WeatherXmlParser parser;
    private final WeatherObservationRepository repository;
    private final String feedUrl;

    public WeatherImportService(
            RestClient.Builder restClientBuilder,
            WeatherXmlParser parser,
            WeatherObservationRepository repository,
            @Value("${weather.feed-url}") String feedUrl) {
        this.restClient = restClientBuilder.build();
        this.parser = parser;
        this.repository = repository;
        this.feedUrl = feedUrl;
    }

    @Scheduled(
        cron = "${weather.import.cron}",
        zone = "${weather.import.zone}")
    public void importOnSchedule() {
        try {
            int count = importLatest();
            log.info("Saved {} weather observations", count);
        } catch (Exception e) {
            log.error("Weather import failed: ", e);
        }
    }

    public int importLatest() {
        String xml = restClient.get().uri(feedUrl).retrieve().body(String.class);
        if (xml == null || xml.isBlank()) throw new IllegalStateException("No data was returned");

        List<WeatherObservation> observations = parser.parse(xml);
        if (observations.isEmpty()) throw new IllegalStateException("The weather feed contained none of the supported stations");

        repository.saveAll(observations);
        return observations.size();
    }
}
