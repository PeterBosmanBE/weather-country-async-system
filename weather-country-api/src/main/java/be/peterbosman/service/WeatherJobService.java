package be.peterbosman.service;

import be.peterbosman.entity.WeatherJob;
import be.peterbosman.metrics.WeatherApiMetrics;
import be.peterbosman.repository.WeatherJobRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSContext;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.jboss.logging.Logger;

import java.util.UUID;

@ApplicationScoped
public class WeatherJobService {

    private static final Logger LOG = Logger.getLogger(WeatherJobService.class);

    @Inject
    WeatherJobRepository repository;

    @Inject
    ConnectionFactory connectionFactory;

    @Inject
    WeatherApiMetrics metrics;

    @ConfigProperty(name = "app.queue.request")
    String requestQueue;

    /**
     * Creates a new job in the DB (PENDING) and publishes it to the request queue.
     */
    @Transactional
    @Retry(maxRetries = 3, delay = 200)
    public WeatherJob createJob(String countryCode) {
        // Persist
        WeatherJob job = new WeatherJob();
        job.countryCode = countryCode.trim().toUpperCase();
        repository.persist(job);

        // Publish to request queue
        publishToQueue(requestQueue, job.id.toString() + ":" + job.countryCode);

        metrics.incrementJobsCreated();
        LOG.infof("Created job %s for country %s", job.id, job.countryCode);
        return job;
    }

    /**
     * Retrieves a job by ID (status only, no result).
     */
    public WeatherJob getJobStatus(UUID id) {
        return repository.findByIdOptional(id)
                .orElseThrow(() -> new jakarta.ws.rs.NotFoundException("Job not found: " + id));
    }

    /**
     * Retrieves a job by ID including the result.
     */
    public WeatherJob getJobResult(UUID id) {
        return repository.findByIdOptional(id)
                .orElseThrow(() -> new jakarta.ws.rs.NotFoundException("Job not found: " + id));
    }

    // ── Internal helpers ──────────────────────────────────────────────────────

    @Retry(maxRetries = 3, delay = 300)
    void publishToQueue(String queueName, String message) {
        try (JMSContext ctx = connectionFactory.createContext()) {
            ctx.createProducer()
               .send(ctx.createQueue(queueName), message);
            LOG.debugf("Published to %s: %s", queueName, message);
        } catch (Exception e) {
            LOG.errorf("Failed to publish to queue %s: %s", queueName, e.getMessage());
            throw e;
        }
    }
}
