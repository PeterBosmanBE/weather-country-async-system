package be.peterbosman.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Custom application metrics exposed via Prometheus (/q/metrics).
 *
 * Metrics:
 *  - weather_api_jobs_created_total   : number of jobs submitted
 *  - weather_api_jobs_completed_total : number of jobs finished with DONE
 *  - weather_api_jobs_failed_total    : number of jobs finished with FAILED
 */
@ApplicationScoped
public class WeatherApiMetrics {

    private final Counter jobsCreated;
    private final Counter jobsCompleted;
    private final Counter jobsFailed;

    @Inject
    public WeatherApiMetrics(MeterRegistry registry) {
        jobsCreated = Counter.builder("weather_api_jobs_created")
                .description("Total number of weather jobs submitted")
                .register(registry);

        jobsCompleted = Counter.builder("weather_api_jobs_completed")
                .description("Total number of weather jobs completed successfully")
                .register(registry);

        jobsFailed = Counter.builder("weather_api_jobs_failed")
                .description("Total number of weather jobs that failed")
                .register(registry);
    }

    public void incrementJobsCreated()   { jobsCreated.increment(); }
    public void incrementJobsCompleted() { jobsCompleted.increment(); }
    public void incrementJobsFailed()    { jobsFailed.increment(); }
}
