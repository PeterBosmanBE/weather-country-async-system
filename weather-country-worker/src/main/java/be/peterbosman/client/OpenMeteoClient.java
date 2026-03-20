package be.peterbosman.client;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import java.util.Map;

@RegisterRestClient(configKey = "weather-api")
public interface OpenMeteoClient {
    @GET
    @Path("/forecast")
    Map<String, Object> getWeather(@QueryParam("latitude") double latitude, @QueryParam("longitude") double longitude, @QueryParam("current_weather") boolean currentWeather);
}
