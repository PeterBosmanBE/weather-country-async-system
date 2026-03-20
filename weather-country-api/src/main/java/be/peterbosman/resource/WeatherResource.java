package be.peterbosman.resource;

import be.peterbosman.entity.WeatherRequest;
import be.peterbosman.repository.WeatherRequestRepository;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Optional;

@Path("/weather")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class WeatherResource {

    @Inject
    WeatherRequestRepository weatherRequestRepository;

    // Here you would inject a service to send messages to the request queue
    // For example:
    // @Inject
    // RequestQueueService requestQueueService;

    @POST
    @Transactional
    public Response requestWeather(WeatherRequest request) {
        request.status = WeatherRequest.Status.PENDING;
        weatherRequestRepository.persist(request);
        // requestQueueService.send(request);
        return Response.status(Response.Status.ACCEPTED).entity(request).build();
    }

    @GET
    @Path("/{id}/status")
    public Response getStatus(@PathParam("id") Long id) {
        Optional<WeatherRequest> weatherRequest = weatherRequestRepository.findByIdOptional(id);
        return weatherRequest.map(req -> Response.ok(req.status).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @GET
    @Path("/{id}/result")
    public Response getResult(@PathParam("id") Long id) {
        Optional<WeatherRequest> weatherRequest = weatherRequestRepository.findByIdOptional(id);
        return weatherRequest.map(req -> {
            if (req.status == WeatherRequest.Status.COMPLETE) {
                return Response.ok(req.weatherInfo).build();
            } else {
                return Response.status(Response.Status.NO_CONTENT).entity("Result not yet available.").build();
            }
        }).orElse(Response.status(Response.Status.NOT_FOUND).build());
    }
}
