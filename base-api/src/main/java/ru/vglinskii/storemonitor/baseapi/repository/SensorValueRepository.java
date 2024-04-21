package ru.vglinskii.storemonitor.baseapi.repository;

import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.vglinskii.storemonitor.baseapi.model.SensorValue;

public interface SensorValueRepository extends JpaRepository<SensorValue, Long> {
    @Query(value = """
            SELECT sv FROM SensorValue sv
            LEFT JOIN SensorValue sv2 ON sv.sensor.id = sv2.sensor.id AND sv.datetime < sv2.datetime
            WHERE sv2.id is null AND sv.sensor.store.id = :storeId
            """)
    @EntityGraph(value = "SensorValue.sensor")
    List<SensorValue> findLastForSensorsByStoreId(long storeId);

    @Query(value = """
            SELECT sv FROM SensorValue sv
            WHERE sv.sensor.store.id = :storeId AND sv.datetime >= :from AND sv.datetime <= :to
            ORDER BY sv.datetime ASC
            """)
    @EntityGraph(value = "SensorValue.sensor")
    List<SensorValue> findByStoreIdInInterval(long storeId, Instant from, Instant to);
}