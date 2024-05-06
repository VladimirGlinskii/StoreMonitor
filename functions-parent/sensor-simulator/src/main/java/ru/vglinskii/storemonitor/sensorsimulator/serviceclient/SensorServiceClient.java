package ru.vglinskii.storemonitor.sensorsimulator.serviceclient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vglinskii.storemonitor.functionscommon.serviceclient.ServiceClientException;
import ru.vglinskii.storemonitor.functionscommon.serviceclient.ServiceClientInvalidArgumentsException;
import ru.vglinskii.storemonitor.functionscommon.http.AppHttpResponseHandler;
import ru.vglinskii.storemonitor.sensorsimulator.dto.UpdateSensorsValuesDtoRequest;

public class SensorServiceClient {
    private final static Logger LOGGER = LoggerFactory.getLogger(SensorServiceClient.class);
    private ObjectMapper objectMapper;
    private String apiUrl;
    private AppHttpResponseHandler responseHandler;
    private CloseableHttpClient httpClient;

    public SensorServiceClient(ObjectMapper objectMapper, String apiUrl) {
        this.objectMapper = objectMapper;
        this.apiUrl = apiUrl;
        this.responseHandler = new AppHttpResponseHandler();
        this.httpClient = HttpClients.createDefault();
    }

    public boolean updateSensorsValues(UpdateSensorsValuesDtoRequest requestDto) {
        var request = new HttpPost(String.format("%s/sensors/values", apiUrl));
        try {
            request.setEntity(new StringEntity(objectMapper.writeValueAsString(requestDto)));
            request.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());
        } catch (JsonProcessingException e) {
            LOGGER.error("Failed to create update sensors values request", e);
            throw new ServiceClientInvalidArgumentsException(e);
        }

        try {
            var response =  httpClient.execute(request, responseHandler);

            return response.is2xxSuccessful();
        } catch (IOException e) {
            LOGGER.error("Failed to execute update sensors values request", e);
            throw new ServiceClientException(e);
        }
    }
}
