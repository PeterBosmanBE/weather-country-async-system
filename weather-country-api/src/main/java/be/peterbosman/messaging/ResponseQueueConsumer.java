package be.peterbosman.messaging;

import be.peterbosman.entity.WeatherJob;
import be.peterbosman.metrics.WeatherApiMetrics;
import be.peterbosman.repository.WeatherJobRepository;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSConsumer;
import jakarta.jms.JMSContext;
import jakarta.jms.Message;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.jboss.logging.Logger;

import java.util.UUID;

/**
 * Polls the response queue every 2 seconds and persists results to the DB.
 */
@ApplicationScoped
public class ResponseQueueConsumer {

    private static final Logger LOG = Logger.getLogger(ResponseQueueConsumer.class);

    // Message format: "UUID:STATUS:JSON_RESULT_OR_ERROR_MESSAGE"
    private static final String DELIMITER = "\\|";

    @Inject
    ConnectionFactory connectionFactory;

    @Inject
    WeatherJobRepository repository;

    @Inject
    WeatherApiMetrics metrics;

    @ConfigProperty(name = "app.queue.response")
    String responseQueue;

    /**
     * Runs every 2 seconds to drain any pending response messages.
     */
    @Scheduled(every = "2s")
    @Transactional
    @Retry(maxRetries = 2, delay = 500)
    public void pollResponseQueue() {
        try (JMSContext ctx = connectionFactory.createContext()) {
            JMSConsumer consumer = ctx.createConsumer(ctx.createQueue(responseQueue));
            Message msg;
            // Drain all available messages in this poll cycle
            while ((msg = consumer.receiveNoWait()) != null) {
                String body = msg.getBody(String.class);
                processMessage(body);
            }
        } catch (Exception e) {
            LOG.errorf("Error polling response queue: %s", e.getMessage());
        }
    }

    private void processMessage(String body) {
        // Format: "<UUID>|<STATUS>|<JSON_or_error>"
        String[] parts = body.split(DELIMITER, 3);
        if (parts.length < 3) {
            LOG.warnf("Unexpected message format, skipping: %s", body);
            return;
        }

        UUID id;
        try {
            id = UUID.fromString(parts[0]);
        } catch (IllegalArgumentException e) {
            LOG.warnf("Invalid UUID in response message: %s", parts[0]);
            return;
        }

        String statusStr = parts[1];
        String payload = parts[2];

        repository.findByIdOptional(id).ifPresentOrElse(job -> {
            try {
                job.status = WeatherJob.JobStatus.valueOf(statusStr);
            } catch (IllegalArgumentException ex) {
                job.status = WeatherJob.JobStatus.FAILED;
            }

            if (job.status == WeatherJob.JobStatus.DONE) {
                job.result = payload;
                metrics.incrementJobsCompleted();
                LOG.infof("Job %s completed successfully", id);
            } else {
                job.error = payload;
                metrics.incrementJobsFailed();
                LOG.warnf("Job %s failed: %s", id, payload);
            }

            repository.persist(job);

        }, () -> LOG.warnf("Received response for unknown job ID: %s", id));
    }
}
