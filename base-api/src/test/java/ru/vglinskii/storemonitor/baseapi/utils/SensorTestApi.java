package ru.vglinskii.storemonitor.baseapi.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import ru.vglinskii.storemonitor.baseapi.model.Employee;

public class SensorTestApi extends BaseTestApi {
    @Autowired
    private MockMvc mockMvc;

    private final String BASE_URL = "/api/sensors";

    public ResultActions getSensors() throws Exception {
        return mockMvc.perform(get(BASE_URL));
    }

    public ResultActions getTemperatureReport(
            Employee as,
            String from,
            String to
    ) throws Exception {
        return mockMvc.perform(authorized(
                get(BASE_URL + "/temperature")
                        .queryParam("from", from)
                        .queryParam("to", to),
                as
        ));
    }
}
