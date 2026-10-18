package ee.fujitsu.delivery.service;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.springframework.stereotype.Component;
import ee.fujitsu.delivery.domain.City;
import ee.fujitsu.delivery.persistence.WeatherObservation;

@Component
public class WeatherXmlParser {

    public List<WeatherObservation> parse(String xml) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);

            Element root = factory.newDocumentBuilder()
                .parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)))
                .getDocumentElement();
            
            Instant observedAt = parseTimestamp(root.getAttribute("timestamp"));
            NodeList stations = root.getElementsByTagName("station");
            List<WeatherObservation> observations = new ArrayList<>();

            for (int i = 0; i < stations.getLength(); i++) {
                Node node = stations.item(i);
                if (!(node instanceof Element station)) {
                    continue;
                }

                String stationName = text(station, "name");
                if (stationName == null || stationName.isBlank()) continue;

                City city = City.fromStationName(stationName);
                if (city == null) continue; // this ignores all stations that are not mapped to Tallinn, Tartu or Pärnu

                String wmoCode = text(station, "wmocode");
                if (wmoCode == null || wmoCode.isBlank()) continue;

                observations.add(new WeatherObservation(
                    city, 
                    stationName, 
                    wmoCode, 
                    decimal(text(station, "airtemperature")), 
                    decimal(text(station, "windspeed")), 
                    text(station, "phenomenon"), 
                    observedAt
                ));
            }
            return observations;
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not parse weather XML, exception: ", e);
        }
    }

    private String text(Element parent, String tagName) {
        NodeList elements = parent.getElementsByTagName(tagName);
        if (elements.getLength() == 0) {
            return null;
        }

        String value = elements.item(0).getTextContent().trim();
        return value.isEmpty() ? null : value;
    }

    private Double decimal(String value) {
        if (value == null || value.isBlank() || value.equals("-")) {
            return null;
        }

        return Double.valueOf(value.replace(',', '.'));
    }

    private Instant parseTimestamp(String value) {
        return Instant.ofEpochSecond(Long.parseLong(value.trim()));
    }
}
