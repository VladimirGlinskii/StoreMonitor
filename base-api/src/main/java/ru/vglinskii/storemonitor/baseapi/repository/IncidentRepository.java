package ru.vglinskii.storemonitor.baseapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.vglinskii.storemonitor.baseapi.model.Incident;

public interface IncidentRepository extends JpaRepository<Incident, Long> {
}