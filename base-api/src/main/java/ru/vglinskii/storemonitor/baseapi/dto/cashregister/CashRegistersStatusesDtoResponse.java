package ru.vglinskii.storemonitor.baseapi.dto.cashregister;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CashRegistersStatusesDtoResponse {
    private List<CashRegisterStatusDtoResponse> statuses;
}
