package ru.vglinskii.storemonitor.baseapi.repository;

import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.vglinskii.storemonitor.baseapi.model.Sensor;

public interface SensorRepository extends JpaRepository<Sensor, Long> {
    @Query(value = """
            SELECT s FROM Sensor s
            LEFT JOIN FETCH s.values sv
            LEFT JOIN SensorValue sv2 ON sv.sensor.id = sv2.sensor.id AND sv.datetime < sv2.datetime
            WHERE sv2.id is null AND s.store.id = :storeId
            """)
    List<Sensor> findByStoreIdWithLastValue(long storeId);

    @Query(value = """
            SELECT s FROM Sensor s
            LEFT JOIN FETCH s.values sv
            WHERE s.store.id = :storeId AND sv.datetime >= :from AND sv.datetime <= :to
            ORDER BY sv.datetime ASC
            """)
    List<Sensor> findByStoreIdInInterval(long storeId, Instant from, Instant to);
}