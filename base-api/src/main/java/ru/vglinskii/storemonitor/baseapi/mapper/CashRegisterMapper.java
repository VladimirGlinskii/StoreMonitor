package ru.vglinskii.storemonitor.baseapi.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.vglinskii.storemonitor.baseapi.dto.cashregister.CashRegisterDtoResponse;
import ru.vglinskii.storemonitor.baseapi.dto.cashregister.CashRegisterStatusDtoResponse;
import ru.vglinskii.storemonitor.baseapi.model.CashRegister;
import ru.vglinskii.storemonitor.baseapi.projection.CashRegisterStatusProjection;

@Mapper
public interface CashRegisterMapper {
    CashRegisterDtoResponse toRegisterDto(CashRegister entity);

    @Mapping(target = "opened", expression = "java(p.getOpenedAt() != null && p.getClosedAt() == null)")
    CashRegisterStatusDtoResponse toRegisterStatusDto(CashRegisterStatusProjection p);
}
