package ru.vglinskii.storemonitor.baseapi.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.vglinskii.storemonitor.baseapi.model.CashRegisterSession;

public interface CashRegisterSessionRepository extends JpaRepository<CashRegisterSession, Long> {
    @Query("""
            SELECT s FROM CashRegisterSession s
            WHERE s.cashRegister.id = :cashRegisterId AND s.closedAt is null
            """)
    Optional<CashRegisterSession> findActiveByCashRegisterId(long cashRegisterId);

    @Query("""
            SELECT s FROM CashRegisterSession s
            WHERE s.cashRegister.store.id = :storeId
            AND (s.closedAt is null OR s.closedAt > :from)
            AND s.createdAt < :to
            """)
    List<CashRegisterSession> findByStoreIdThatIntersectInterval(long storeId,
                                                                 LocalDateTime from,
                                                                 LocalDateTime to);
}