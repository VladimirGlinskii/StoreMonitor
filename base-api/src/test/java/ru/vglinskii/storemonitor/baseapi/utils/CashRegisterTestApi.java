package ru.vglinskii.storemonitor.baseapi.utils;

import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import ru.vglinskii.storemonitor.baseapi.dto.cashregister.CreateCashRegisterDtoRequest;
import ru.vglinskii.storemonitor.baseapi.dto.cashregister.UpdateCashRegisterStatusDtoRequest;

public class CashRegisterTestApi extends BaseTestApi {
    @Autowired
    private MockMvc mockMvc;

    private final String BASE_URL = "/api/cash-registers";

    public ResultActions createCashRegister(long storeId, CreateCashRegisterDtoRequest request) throws Exception {
        return mockMvc
                .perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request))
                        .header("storeId", storeId)
                );
    }

    public ResultActions openCashRegisterSession(
            long storeId,
            long id,
            UpdateCashRegisterStatusDtoRequest request
    ) throws Exception {
        return mockMvc
                .perform(post(BASE_URL + "/" + id + "/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request))
                        .header("storeId", storeId)
                );
    }

    public ResultActions closeCashRegisterSession(
            long storeId,
            long id,
            UpdateCashRegisterStatusDtoRequest request
    ) throws Exception {
        return mockMvc
                .perform(delete(BASE_URL + "/" + id + "/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request))
                        .header("storeId", storeId)
                );
    }

    public ResultActions deleteCashRegister(long storeId, long id) throws Exception {
        return mockMvc.perform(delete(BASE_URL + "/" + id)
                .header("storeId", storeId)
        );
    }

    public ResultActions getCashRegistersStatuses(long storeId) throws Exception {
        return mockMvc.perform(get(BASE_URL + "/statuses")
                .header("storeId", storeId)
        );
    }

    public ResultActions getCashRegistersWorkSummary(
            long storeId,
            String from,
            String to
    ) throws Exception {
        return mockMvc.perform(get(BASE_URL + "/work-summary")
                .header("storeId", storeId)
                .queryParam("from", from)
                .queryParam("to", to)
        );
    }
}
