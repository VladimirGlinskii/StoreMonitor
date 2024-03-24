package ru.vglinskii.storemonitor.baseapi.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.vglinskii.storemonitor.baseapi.enums.EmployeeType;
import ru.vglinskii.storemonitor.baseapi.model.Employee;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByIdAndStoreIdAndType(long id, long storeId, EmployeeType type);
}