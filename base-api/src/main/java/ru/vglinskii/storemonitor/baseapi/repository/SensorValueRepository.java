package ru.vglinskii.storemonitor.baseapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.vglinskii.storemonitor.baseapi.model.SensorValue;

public interface SensorValueRepository extends JpaRepository<SensorValue, Long> {
}