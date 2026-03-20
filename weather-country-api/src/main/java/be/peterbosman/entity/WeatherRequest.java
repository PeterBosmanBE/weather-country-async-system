package be.peterbosman.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;

@Entity
public class WeatherRequest extends PanacheEntity {

    public String city;
    public String country;
    public Status status;
    public String weatherInfo;

    public enum Status {
        PENDING,
        PROCESSING,
        COMPLETE,
        FAILED
    }
}
