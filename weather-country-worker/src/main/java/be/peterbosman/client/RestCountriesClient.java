package be.peterbosman.client;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import java.util.List;
import java.util.Map;

@RegisterRestClient(configKey = "country-api")
public interface RestCountriesClient {
    @GET
    @Path("/name/{name}")
    List<Map<String, Object>> getCountryByName(@PathParam("name") String name);
}
