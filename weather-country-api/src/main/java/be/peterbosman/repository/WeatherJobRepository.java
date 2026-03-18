package be.peterbosman.repository;

import be.peterbosman.entity.WeatherJob;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class WeatherJobRepository implements PanacheRepositoryBase<WeatherJob, UUID> {

    public Optional<WeatherJob> findByIdOptional(UUID id) {
        return find("id", id).firstResultOptional();
    }
}
