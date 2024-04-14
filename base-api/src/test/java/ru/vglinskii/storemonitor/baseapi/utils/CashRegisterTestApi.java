package ru.vglinskii.storemonitor.baseapi.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import ru.vglinskii.storemonitor.baseapi.dto.cashregister.CreateCashRegisterDtoRequest;
import ru.vglinskii.storemonitor.baseapi.model.Employee;

public class CashRegisterTestApi extends BaseTestApi {
    @Autowired
    private MockMvc mockMvc;

    private final String BASE_URL = "/api/cash-registers";

    public ResultActions createCashRegister(
            Employee as,
            CreateCashRegisterDtoRequest request
    ) throws Exception {
        return mockMvc
                .perform(authorized(
                        post(BASE_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(toJson(request)),
                        as
                ));
    }

    public ResultActions openCashRegisterSession(Employee as, long id) throws Exception {
        return mockMvc
                .perform(authorized(
                        post(BASE_URL + "/" + id + "/sessions")
                                .contentType(MediaType.APPLICATION_JSON),
                        as
                ));
    }

    public ResultActions closeCashRegisterSession(Employee as, long id) throws Exception {
        return mockMvc
                .perform(authorized(
                        delete(BASE_URL + "/" + id + "/sessions")
                                .contentType(MediaType.APPLICATION_JSON),
                        as
                ));
    }

    public ResultActions deleteCashRegister(Employee as, long id) throws Exception {
        return mockMvc.perform(authorized(
                delete(BASE_URL + "/" + id),
                as
        ));
    }

    public ResultActions getCashRegistersStatuses(Employee as) throws Exception {
        return mockMvc.perform(authorized(
                get(BASE_URL + "/statuses"),
                as
        ));
    }

    public ResultActions getCashRegistersWorkSummary(
            Employee as,
            String from,
            String to
    ) throws Exception {
        return mockMvc.perform(authorized(
                get(BASE_URL + "/work-summary")
                        .queryParam("from", from)
                        .queryParam("to", to),
                as
        ));
    }
}
