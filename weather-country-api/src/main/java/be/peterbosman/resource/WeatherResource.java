package be.peterbosman.resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import be.peterbosman.entity.WeatherMessage;
import be.peterbosman.entity.WeatherRequest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

import java.util.Map;

@Path("/weather")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class WeatherResource {

    @Channel("weather-requests-out")
    Emitter<String> emitter;

    @Inject
    ObjectMapper objectMapper;

    @POST
    @Transactional
    public Response requestWeather(String location) {
        String cleanLocation = location == null ? "" : location.trim();
        if (cleanLocation.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Location is required"))
                    .build();
        }

        WeatherRequest req = new WeatherRequest();
        req.location = cleanLocation;
        req.status = "PENDING";
        req.persistAndFlush();

        WeatherMessage message = new WeatherMessage();
        message.id = req.id;
        message.location = req.location;
        message.status = req.status;

        try {
            emitter.send(objectMapper.writeValueAsString(message));
        } catch (JsonProcessingException e) {
            req.status = "FAILED";
            req.result = "Failed to serialize outgoing message";
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Failed to queue weather request"))
                    .build();
        }

        return Response.accepted(req).build();
    }

    @GET
    @Path("/{id}/status")
    public Response getStatus(@PathParam("id") Long id) {
        WeatherRequest req = WeatherRequest.findById(id);
        if (req == null) {
            return Response.status(404).build();
        }

        return Response.ok(req.status).build();
    }

    @GET
    @Path("/{id}/result")
    public Response getResult(@PathParam("id") Long id) {
        WeatherRequest req = WeatherRequest.findById(id);
        if (req == null) {
            return Response.status(404).build();
        }

        return Response.ok(req.result == null ? "" : req.result).build();
    }
}
