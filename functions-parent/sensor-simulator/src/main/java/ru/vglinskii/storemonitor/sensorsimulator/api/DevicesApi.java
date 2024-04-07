package ru.vglinskii.storemonitor.sensorsimulator.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vglinskii.storemonitor.functionscommon.api.ApiClientException;
import ru.vglinskii.storemonitor.functionscommon.api.ApiInvalidArgumentsException;
import ru.vglinskii.storemonitor.functionscommon.api.AppHttpResponseHandler;
import ru.vglinskii.storemonitor.functionscommon.api.HttpResponse;
import ru.vglinskii.storemonitor.sensorsimulator.dto.UpdateSensorsValuesDtoRequest;

public class DevicesApi {
    private final static Logger LOGGER = LoggerFactory.getLogger(DevicesApi.class);
    private ObjectMapper objectMapper;
    private String apiUrl;
    private AppHttpResponseHandler responseHandler;

    public DevicesApi(ObjectMapper objectMapper, String apiUrl) {
        this.objectMapper = objectMapper;
        this.apiUrl = apiUrl;
        this.responseHandler = new AppHttpResponseHandler();
    }

    public HttpResponse updateSensorsValues(UpdateSensorsValuesDtoRequest requestDto) {
        HttpUriRequest request;
        try {
            request = RequestBuilder.post()
                    .setUri(String.format("%s/sensors/values", apiUrl))
                    .setEntity(new StringEntity(objectMapper.writeValueAsString(requestDto)))
                    .setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
                    .build();
        } catch (JsonProcessingException | UnsupportedEncodingException e) {
            LOGGER.error("Failed to create update sensors values request", e);
            throw new ApiInvalidArgumentsException(e);
        }

        try (var httpClient = HttpClients.createDefault()) {
            return httpClient.execute(request, responseHandler);
        } catch (IOException e) {
            LOGGER.error("Failed to execute update sensors values request", e);
            throw new ApiClientException(e);
        }
    }
}
