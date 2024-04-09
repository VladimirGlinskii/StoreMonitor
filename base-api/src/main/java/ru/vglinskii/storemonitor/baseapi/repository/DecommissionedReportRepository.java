package ru.vglinskii.storemonitor.baseapi.repository;

import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.vglinskii.storemonitor.baseapi.model.DecommissionedReport;

public interface DecommissionedReportRepository extends JpaRepository<DecommissionedReport, Long> {
    @Query(value = """
            SELECT r FROM DecommissionedReport r
            WHERE r.store.id = :storeId AND r.createdAt >= :from AND r.createdAt <= :to
            ORDER BY r.createdAt ASC
            """)
    List<DecommissionedReport> findByStoreIdInInterval(long storeId, Instant from, Instant to);
}