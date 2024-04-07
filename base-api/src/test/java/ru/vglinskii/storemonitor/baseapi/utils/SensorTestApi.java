package ru.vglinskii.storemonitor.baseapi.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

public class SensorTestApi extends BaseTestApi {
    @Autowired
    private MockMvc mockMvc;

    private final String BASE_URL = "/api/sensors";

    public ResultActions getSensors(long storeId) throws Exception {
        return mockMvc.perform(get(BASE_URL)
                .header("X-Store-Id", storeId)
        );
    }

    public ResultActions getTemperatureReport(
            long storeId,
            String from,
            String to
    ) throws Exception {
        return mockMvc.perform(get(BASE_URL + "/temperature")
                .header("X-Store-Id", storeId)
                .queryParam("from", from)
                .queryParam("to", to)
        );
    }
}
