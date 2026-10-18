# Fujitsu Delivery Fee Service

Starter project for the Java Programming Trial Task. It uses Java 17, Spring Boot, Maven, and H2.

## Requirements

- JDK 17 or newer
- Internet access the first time Maven Wrapper is run, so it can download Maven and dependencies

## Run

On Windows PowerShell, from this folder:

```powershell
.\mvnw.cmd spring-boot:run
```

The first launch downloads the Maven distribution and project dependencies. The application listens on `http://localhost:8080`.

## Project setup

- `pom.xml` declares Java and Spring dependencies.
- `src/main/java/ee/fujitsu/delivery/DeliveryFeeApplication.java` starts Spring Boot and enables scheduled tasks.
- `src/main/resources/application.properties` configures the local file-backed H2 database and weather-import settings.
- Feature code (database entities, API, importer, and fee calculation) is intentionally left for the next step.

The weather source is the [Estonian Environment Agency observation XML feed](https://www.ilmateenistus.ee/ilma_andmed/xml/observations.php). When displaying or documenting its data, cite the Estonian Environment Agency as the source and link to [ilmateenistus.ee](https://www.ilmateenistus.ee/).
