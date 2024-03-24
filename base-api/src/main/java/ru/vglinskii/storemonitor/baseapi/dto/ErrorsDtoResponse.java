package ru.vglinskii.storemonitor.baseapi.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
