package be.peterbosman.resource;

import be.peterbosman.entity.WeatherJob;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.UUID;

public class JobDtos {

    // ── Inbound ──────────────────────────────────────────────
    public record CreateJobRequest(String countryCode) {}

    // ── Outbound ─────────────────────────────────────────────
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record JobStatusResponse(
            UUID id,
            String countryCode,
            String status,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        public static JobStatusResponse from(WeatherJob job) {
            return new JobStatusResponse(
                    job.id,
                    job.countryCode,
                    job.status.name(),
                    job.createdAt,
                    job.updatedAt
            );
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record JobResultResponse(
            UUID id,
            String countryCode,
            String status,
            String result,
            String error,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        public static JobResultResponse from(WeatherJob job) {
            return new JobResultResponse(
                    job.id,
                    job.countryCode,
                    job.status.name(),
                    job.result,
                    job.error,
                    job.createdAt,
                    job.updatedAt
            );
        }
    }
}
