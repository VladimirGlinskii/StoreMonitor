package ru.vglinskii.storemonitor.baseapi.dto.cashregister;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.vglinskii.storemonitor.baseapi.utils.ValidationErrorMessages;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateCashRegisterDtoRequest {
    @NotBlank(message = ValidationErrorMessages.REQUIRED_FIELD)
    private String inventoryNumber;
}
