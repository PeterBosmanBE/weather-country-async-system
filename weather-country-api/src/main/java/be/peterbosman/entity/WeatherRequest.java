package be.peterbosman.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

@Entity
public class WeatherRequest extends PanacheEntity {
    public String location;
    public String status;

    @Column(columnDefinition = "TEXT")
    public String result;
}
