package be.peterbosman.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "weather_job")
public class WeatherJob extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    public UUID id;

    @Column(name = "country_code", nullable = false, length = 10)
    public String countryCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    public JobStatus status = JobStatus.PENDING;

    @Column(name = "result", columnDefinition = "TEXT")
    public String result;

    @Column(name = "error", columnDefinition = "TEXT")
    public String error;

    @Column(name = "created_at", nullable = false, updatable = false)
    public LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    public LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public enum JobStatus {
        PENDING, PROCESSING, DONE, FAILED
    }
}
