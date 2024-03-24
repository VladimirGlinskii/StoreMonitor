package ru.vglinskii.storemonitor.baseapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.vglinskii.storemonitor.baseapi.model.Store;

public interface StoreRepository extends JpaRepository<Store, Long> {
}