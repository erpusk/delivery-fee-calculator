# Delivery Fee Service

This is my solution to the Fujitsu Java programming task. The service calculates a delivery fee from the city, vehicle type and, for bikes and scooters, the weather in that city.

## Run it

You need Java 17 or newer. From the project folder, run:

```powershell
.\mvnw.cmd spring-boot:run
```

The first run downloads Maven and the project dependencies. The API is then available at `http://localhost:8080`.

To run the tests:

```powershell
.\mvnw.cmd test
```

The tests are in src/test/java/ee/fujitsu/delivery/service/DeliveryFeeServiceTest.java and cover base fees, weather fees, forbidden conditions, and historical weather selection.

## Calculate a fee

The fee endpoint is `GET /api/delivery-fees`. It needs parameters city and vehicle type. The optional `dateTime` parameter is a local Tallinn date and time in `YYYY-MM-DDTHH:mm:ss` format. For example:

```text
http://localhost:8080/api/delivery-fees?city=Tallinn&vehicleType=CAR
http://localhost:8080/api/delivery-fees?city=Tallinn&vehicleType=BIKE&dateTime=2026-10-18T20:00:00
```

Cities are Tallinn, Tartu and Pärnu. Vehicle types are `CAR`, `SCOOTER` and `BIKE`. When a date is provided, the service uses the latest saved weather observation at or before that time. 
Cars are charged the base fee only.

If the weather makes a vehicle forbidden, the API returns an error. A fee cannot be calculated for a scooter or bike if there is no saved observation for the requested time.

## Manage fee rules

Regional base fees can be listed and managed at `/api/base-fees`:

- `GET /api/base-fees` lists the rules for base fees.
- `POST /api/base-fees` adds a rule.
- `PUT /api/base-fees/{city}/{vehicleType}` changes existing rule's amount.
- `DELETE /api/base-fees/{city}/{vehicleType}` removes existing rule.

Weather fees and restrictions can be managed at `/api/weather-fees`:

- `GET /api/weather-fees` lists the rules for weather fees.
- `POST /api/weather-fees` adds a rule.
- `PUT /api/weather-fees/{vehicleType}/{condition}` changes existing rule.
- `DELETE /api/weather-fees/{vehicleType}/{condition}` removes existing rule.

For JSON requests, base-fee amounts use a body like `{"amount": 3.25}`. A new base-fee rule also needs `city` and `vehicleType`, using enum names such as `TALLINN` and `BIKE`. Weather rules use `vehicleType`, `condition`, `amount` and `forbidden`; condition names include `RAIN`, `SNOW_OR_SLEET` and `DANGEROUS_WEATHER`.

The starting fee rules are kept in memory. Changes made through these endpoints reset when the application restarts.

## Weather data and database

The service imports observations from the [Estonian Environment Agency XML feed](https://www.ilmateenistus.ee/ilma_andmed/xml/observations.php). It checks the feed once an hour, at 15 minutes past the hour, using the `Europe/Tallinn` time zone. The first import happens at the next scheduled run after startup.

Weather observations are saved in a local H2 database under `data/weatherdb`, so the history is kept when the application restarts. The H2 console is available at `http://localhost:8080/h2-console`. Use the JDBC URL `jdbc:h2:file:./data/weatherdb;DB_CLOSE_ON_EXIT=FALSE`, username `sa`, and a blank password.

## API documentation

When the application is running, open `http://localhost:8080/swagger-ui.html` to see the API in Swagger UI.
