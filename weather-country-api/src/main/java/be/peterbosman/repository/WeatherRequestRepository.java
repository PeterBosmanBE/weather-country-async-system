package be.peterbosman.repository;

import be.peterbosman.entity.WeatherRequest;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class WeatherRequestRepository implements PanacheRepository<WeatherRequest> {
}
