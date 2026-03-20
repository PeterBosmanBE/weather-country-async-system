package be.peterbosman.messaging;

import be.peterbosman.entity.WeatherMessage;
import be.peterbosman.entity.WeatherRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import io.smallrye.common.annotation.Blocking;

@ApplicationScoped
public class WeatherConsumer {

    private static final Logger LOG = Logger.getLogger(WeatherConsumer.class);

    @Inject
    ObjectMapper objectMapper;

    @Incoming("weather-responses-in")
    @Blocking
    @Transactional
    public void processResponse(String payload) {
        WeatherMessage responseMsg;

        try {
            responseMsg = objectMapper.readValue(payload, WeatherMessage.class);
        } catch (JsonProcessingException e) {
            LOG.errorf(e, "Failed to parse worker response payload: %s", payload);
            return;
        }

        LOG.infov("Received weather response message for id={0}, status={1}", responseMsg.id, responseMsg.status);

        WeatherRequest req = WeatherRequest.findById(responseMsg.id);

        if (req != null) {
            req.status = responseMsg.status;
            req.result = responseMsg.result;
            req.persistAndFlush();
            LOG.infov("Updated WeatherRequest id={0} to status={1}", req.id, req.status);
        } else {
            LOG.warnv("No WeatherRequest row found for message id={0}", responseMsg.id);
        }
    }
}
