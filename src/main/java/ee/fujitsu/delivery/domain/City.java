package ee.fujitsu.delivery.domain;

import java.util.Arrays;

public enum City {
    TALLINN("Tallinn", "Tallinn-Harku"),
    TARTU("Tartu", "Tartu-Tõravere"),
    PARNU("Pärnu", "Pärnu");

    private final String displayName;
    private final String stationName;

    City(String displayName, String stationName) {
        this.displayName = displayName;
        this.stationName = stationName;
    }

    public String displayName() {
        return displayName;
    }

    public String stationName() {
        return stationName;
    }

    public static City fromDisplayName(String displayName) {
        return Arrays.stream(values())
            .filter(city -> city.displayName.equalsIgnoreCase(displayName.trim()))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unsupported city: " + displayName));
    }

    public static City fromStationName(String stationName) {
        return Arrays.stream(values())
            .filter(city -> city.stationName.equalsIgnoreCase(stationName.trim()))
            .findFirst()
            .orElse(null);
    }
}