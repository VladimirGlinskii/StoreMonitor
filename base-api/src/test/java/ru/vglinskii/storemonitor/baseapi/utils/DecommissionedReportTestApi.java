package ru.vglinskii.storemonitor.baseapi.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

public class DecommissionedReportTestApi extends BaseTestApi {
    @Autowired
    private MockMvc mockMvc;

    private final String BASE_URL = "/api/decommissioned-reports";

    public ResultActions getAll(
            long storeId,
            String from,
            String to
    ) throws Exception {
        return mockMvc.perform(get(BASE_URL)
                .header("X-Store-Id", storeId)
                .queryParam("from", from)
                .queryParam("to", to)
        );
    }
}
