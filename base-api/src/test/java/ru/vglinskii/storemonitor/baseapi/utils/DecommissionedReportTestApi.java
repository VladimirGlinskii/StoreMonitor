package ru.vglinskii.storemonitor.baseapi.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import ru.vglinskii.storemonitor.baseapi.model.Employee;

public class DecommissionedReportTestApi extends BaseTestApi {
    @Autowired
    private MockMvc mockMvc;

    private final String BASE_URL = "/api/decommissioned-reports";

    public ResultActions getAll(
            Employee as,
            String from,
            String to
    ) throws Exception {
        return mockMvc.perform(authorized(
                get(BASE_URL)
                        .queryParam("from", from)
                        .queryParam("to", to),
                as
        ));
    }
}
