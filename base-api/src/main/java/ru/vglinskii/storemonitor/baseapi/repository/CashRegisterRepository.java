package ru.vglinskii.storemonitor.baseapi.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import ru.vglinskii.storemonitor.baseapi.model.CashRegister;
import ru.vglinskii.storemonitor.baseapi.projection.CashRegisterStatusProjection;

public interface CashRegisterRepository extends JpaRepository<CashRegister, Long> {
    Optional<CashRegister> findByStoreIdAndInventoryNumber(long storeId, String inventoryNumber);

    Optional<CashRegister> findByIdAndStoreId(long id, long storeId);

    @Modifying
    @Transactional
    void deleteByIdAndStoreId(long id, long storeId);

    @Query(nativeQuery = true, value = """
            SELECT cr.id as id,
                cr.inventory_number as inventoryNumber,
                s.created_at as openedAt,
                s.closed_at as closedAt
            FROM cash_register cr
            LEFT JOIN (
                SELECT s1.* FROM cash_register_session s1
                LEFT JOIN cash_register_session s2
                ON s1.cash_register_id = s2.cash_register_id AND s1.created_at < s2.created_at
                WHERE s2.id is null
            ) s ON cr.id = s.cash_register_id
            WHERE cr.store_id = :storeId
            """)
    List<CashRegisterStatusProjection> findWithLastSessionByStoreId(long storeId);
}