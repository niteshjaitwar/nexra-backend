package com.nexra.hrms.nexra.modules.hrms.leave.repository;

import com.nexra.hrms.nexra.modules.hrms.leave.entity.HolidayEntity;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HolidayRepository extends JpaRepository<HolidayEntity, String> {
    List<HolidayEntity> findByTenantCodeIgnoreCaseAndHolidayDateBetweenOrderByHolidayDateAsc(
        String tenantCode, LocalDate start, LocalDate end
    );
    Optional<HolidayEntity> findByTenantCodeIgnoreCaseAndHolidayDateAndLocationCode(
        String tenantCode, LocalDate holidayDate, String locationCode
    );
    Optional<HolidayEntity> findByIdAndTenantCodeIgnoreCase(String id, String tenantCode);
}

