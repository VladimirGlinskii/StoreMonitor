package ru.vglinskii.storemonitor.baseapi.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.vglinskii.storemonitor.baseapi.dto.decommissionedreport.DecommissionedReportDtoResponse;
import ru.vglinskii.storemonitor.baseapi.model.DecommissionedReport;

@Mapper
public interface DecommissionedReportMapper {
    @Mapping(target = "datetime", source = "createdAt")
    DecommissionedReportDtoResponse toReportDto(DecommissionedReport entity);
}
