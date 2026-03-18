package be.peterbosman.resource;

import be.peterbosman.entity.WeatherJob;
import be.peterbosman.service.WeatherJobService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.net.URI;
import java.util.UUID;

@Path("/api/jobs")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Weather Jobs", description = "Submit and track async weather lookup jobs")
public class WeatherJobResource {

    @Inject
    WeatherJobService service;

    /**
     * POST /api/jobs
     * Submit a new country weather lookup job.
     * Body: { "countryCode": "BE" }
     */
    @POST
    @Operation(summary = "Submit a new weather job", description = "Places a job on the request queue and returns the job ID")
    public Response createJob(JobDtos.CreateJobRequest request) {
        if (request == null || request.countryCode() == null || request.countryCode().isBlank()) {
            throw new BadRequestException("countryCode is required");
        }

        WeatherJob job = service.createJob(request.countryCode());
        return Response
                .created(URI.create("/api/jobs/" + job.id))
                .entity(JobDtos.JobStatusResponse.from(job))
                .build();
    }

    /**
     * GET /api/jobs/{id}
     * Check the status of a job.
     */
    @GET
    @Path("/{id}")
    @Operation(summary = "Get job status", description = "Returns the current status of a job (PENDING, PROCESSING, DONE, FAILED)")
    public Response getStatus(@PathParam("id") UUID id) {
        WeatherJob job = service.getJobStatus(id);
        return Response.ok(JobDtos.JobStatusResponse.from(job)).build();
    }

    /**
     * GET /api/jobs/{id}/result
     * Retrieve the full result of a completed job.
     */
    @GET
    @Path("/{id}/result")
    @Operation(summary = "Get job result", description = "Returns the full weather result once the job is DONE")
    public Response getResult(@PathParam("id") UUID id) {
        WeatherJob job = service.getJobResult(id);

        if (job.status == WeatherJob.JobStatus.PENDING || job.status == WeatherJob.JobStatus.PROCESSING) {
            return Response.status(Response.Status.ACCEPTED)
                    .entity(JobDtos.JobStatusResponse.from(job))
                    .build();
        }

        return Response.ok(JobDtos.JobResultResponse.from(job)).build();
    }
}
