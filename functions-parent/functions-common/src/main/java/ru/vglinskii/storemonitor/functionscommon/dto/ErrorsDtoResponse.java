package ru.vglinskii.storemonitor.functionscommon.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorsDtoResponse {
    private List<ErrorDtoResponse> errors;

    public ErrorsDtoResponse(ErrorDtoResponse error) {
        this.errors = new ArrayList<>();
        this.errors.add(error);
    }
}
