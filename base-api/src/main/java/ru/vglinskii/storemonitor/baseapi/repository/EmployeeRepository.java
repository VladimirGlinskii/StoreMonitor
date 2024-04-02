package ru.vglinskii.storemonitor.baseapi.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.vglinskii.storemonitor.baseapi.model.Employee;
import ru.vglinskii.storemonitor.common.enums.EmployeeType;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByIdAndStoreIdAndType(long id, long storeId, EmployeeType type);
}