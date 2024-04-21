package ru.vglinskii.storemonitor.baseapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.vglinskii.storemonitor.baseapi.model.Sensor;

public interface SensorRepository extends JpaRepository<Sensor, Long> {
}