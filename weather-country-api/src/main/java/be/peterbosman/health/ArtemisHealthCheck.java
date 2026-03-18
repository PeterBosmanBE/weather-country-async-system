package be.peterbosman.health;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSContext;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;

/**
 * Custom health check: verifies that we can open a JMS connection to ActiveMQ Artemis.
 * Exposed at /q/health/live
 */
@Liveness
@ApplicationScoped
public class ArtemisHealthCheck implements HealthCheck {

    @Inject
    ConnectionFactory connectionFactory;

    @Override
    public HealthCheckResponse call() {
        try (JMSContext ctx = connectionFactory.createContext()) {
            // If this doesn't throw, the broker is reachable
            ctx.getSessionMode(); // lightweight no-op call
            return HealthCheckResponse.named("artemis-connectivity")
                    .up()
                    .withData("broker", "reachable")
                    .build();
        } catch (Exception e) {
            return HealthCheckResponse.named("artemis-connectivity")
                    .down()
                    .withData("error", e.getMessage())
                    .build();
        }
    }
}
