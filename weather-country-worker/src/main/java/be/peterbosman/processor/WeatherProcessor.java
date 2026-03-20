package be.peterbosman.processor;

import be.peterbosman.client.RestCountriesClient;
import be.peterbosman.client.OpenMeteoClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import io.smallrye.common.annotation.Blocking;
import org.jboss.logging.Logger;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Fallback;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class WeatherProcessor {

    private static final Logger LOG = Logger.getLogger(WeatherProcessor.class);

    @RestClient
    RestCountriesClient countriesClient;

    @RestClient
    OpenMeteoClient meteoClient;

    @Inject
    ObjectMapper objectMapper;

    @Incoming("weather-requests-in")
    @Outgoing("weather-responses-out")
    @Blocking
    @Retry(maxRetries = 3)
    @Fallback(fallbackMethod = "fallbackWeather")
    public String process(String payload) {
        WeatherMessage msg;

        try {
            msg = objectMapper.readValue(payload, WeatherMessage.class);
        } catch (JsonProcessingException e) {
            LOG.errorf(e, "Failed to parse request payload: %s", payload);
            WeatherMessage failed = new WeatherMessage();
            failed.status = "FAILED";
            failed.result = "Invalid request payload";
            return toJson(failed);
        }

        try {
            String normalizedLocation = normalizeLocation(msg.location);
            if (normalizedLocation.isBlank()) {
                msg.status = "FAILED";
                msg.result = "Location is empty";
                return toJson(msg);
            }

            msg.location = normalizedLocation;
            List<Map<String, Object>> countryData = countriesClient.getCountryByName(normalizedLocation);

            if (countryData != null && !countryData.isEmpty()) {
                List<Double> latlng = extractCoordinates(countryData.get(0));

                if (latlng == null || latlng.size() < 2) {
                    msg.status = "FAILED";
                    msg.result = "Could not resolve coordinates for location";
                    return toJson(msg);
                }

                Map<String, Object> weather = meteoClient.getWeather(latlng.get(0), latlng.get(1), true);
                msg.status = "SUCCESS";
                msg.result = objectMapper.writeValueAsString(weather);
            } else {
                msg.status = "FAILED";
                msg.result = "Country not found";
            }
        } catch (WebApplicationException e) {
            if (e.getResponse() != null && e.getResponse().getStatus() == 404) {
                msg.status = "FAILED";
                msg.result = "Location not found in RestCountries: " + (msg.location == null ? "" : msg.location);
            } else {
                msg.status = "FAILED";
                msg.result = e.getMessage();
            }
        } catch (Exception e) {
            msg.status = "FAILED";
            msg.result = e.getMessage();
        }

        return toJson(msg);
    }

    public String fallbackWeather(String payload) {
        WeatherMessage msg;

        try {
            msg = objectMapper.readValue(payload, WeatherMessage.class);
        } catch (JsonProcessingException e) {
            msg = new WeatherMessage();
        }

        msg.status = "FAILED";
        msg.result = "Fallback executed due to failure";
        return toJson(msg);
    }

    private String toJson(WeatherMessage message) {
        try {
            return objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            LOG.error("Failed to serialize response payload", e);
            return "{\"status\":\"FAILED\",\"result\":\"Serialization failure\"}";
        }
    }

    @SuppressWarnings("unchecked")
    private List<Double> extractCoordinates(Map<String, Object> countryEntry) {
        Object capitalInfoObj = countryEntry.get("capitalInfo");
        if (capitalInfoObj instanceof Map<?, ?> capitalInfoMap) {
            Object latlng = capitalInfoMap.get("latlng");
            if (latlng instanceof List<?> list && list.size() >= 2) {
                return (List<Double>) list;
            }
        }

        Object fallbackLatLng = countryEntry.get("latlng");
        if (fallbackLatLng instanceof List<?> list && list.size() >= 2) {
            return (List<Double>) list;
        }

        return null;
    }

    private String normalizeLocation(String rawLocation) {
        if (rawLocation == null) {
            return "";
        }

        String value = rawLocation.trim();
        if (value.isBlank()) {
            return "";
        }

        if (value.startsWith("{") && value.endsWith("}")) {
            try {
                JsonNode node = objectMapper.readTree(value);
                JsonNode locationNode = node.get("location");
                if (locationNode != null && !locationNode.isNull()) {
                    value = locationNode.asText("").trim();
                }
            } catch (JsonProcessingException e) {
                LOG.warnf("Invalid JSON location payload, applying fallback normalization: %s", value);
                String compact = value.substring(1, value.length() - 1).trim();
                if (compact.startsWith("\"location\":") || compact.startsWith("location:")) {
                    String[] parts = compact.split(":", 2);
                    if (parts.length == 2) {
                        value = parts[1].trim();
                    }
                }
            }
        }

        if (value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
            value = value.substring(1, value.length() - 1).trim();
        }

        return value;
    }
}
