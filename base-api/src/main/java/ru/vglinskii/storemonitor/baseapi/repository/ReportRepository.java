package ru.vglinskii.storemonitor.baseapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.vglinskii.storemonitor.baseapi.model.Report;

public interface ReportRepository extends JpaRepository<Report, Long> {
}