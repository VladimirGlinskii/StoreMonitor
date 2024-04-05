package ru.vglinskii.storemonitor.cashiersimulator.api;

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
import ru.vglinskii.storemonitor.cashiersimulator.dto.UpdateCashRegisterStateDtoRequest;
import ru.vglinskii.storemonitor.cashiersimulator.model.CashRegister;
import ru.vglinskii.storemonitor.cashiersimulator.model.Cashier;

public class CashRegisterApi {
    private final static Logger LOGGER = LoggerFactory.getLogger(CashRegisterApi.class);
    private ObjectMapper objectMapper;
    private String apiUrl;
    private AppHttpResponseHandler responseHandler;

    public CashRegisterApi(ObjectMapper objectMapper, String apiUrl) {
        this.objectMapper = objectMapper;
        this.apiUrl = apiUrl;
        this.responseHandler = new AppHttpResponseHandler();
    }

    public HttpResponse openCashRegister(CashRegister cashRegister, Cashier cashier) {
        var requestDto = UpdateCashRegisterStateDtoRequest.builder()
                .cashierId(cashier.getId())
                .build();
        HttpUriRequest request;
        try {
            request = RequestBuilder.post()
                    .setUri(String.format("%s/cash-registers/%d/sessions", apiUrl, cashRegister.getId()))
                    .setEntity(new StringEntity(objectMapper.writeValueAsString(requestDto)))
                    .setHeader("X-Store-Id", String.valueOf(cashRegister.getStoreId()))
                    .setHeader("X-Secret-Key", cashier.getSecret())
                    .setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
                    .build();
        } catch (JsonProcessingException | UnsupportedEncodingException e) {
            LOGGER.error("Failed to create open cash register request", e);
            throw new ApiInvalidArgumentsException(e);
        }

        try (var httpClient = HttpClients.createDefault()) {
            return httpClient.execute(request, responseHandler);
        } catch (IOException e) {
            LOGGER.error("Failed to execute open cash register request", e);
            throw new ApiClientException(e);
        }
    }

    public HttpResponse closeCashRegister(CashRegister cashRegister, Cashier cashier) {
        var requestDto = UpdateCashRegisterStateDtoRequest.builder()
                .cashierId(cashier.getId())
                .build();
        HttpUriRequest request;
        try {
            request = RequestBuilder.delete()
                    .setUri(String.format("%s/cash-registers/%d/sessions", apiUrl, cashRegister.getId()))
                    .setEntity(new StringEntity(objectMapper.writeValueAsString(requestDto)))
                    .setHeader("X-Store-Id", String.valueOf(cashRegister.getStoreId()))
                    .setHeader("X-Secret-Key", cashier.getSecret())
                    .setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
                    .build();
        } catch (JsonProcessingException | UnsupportedEncodingException e) {
            LOGGER.error("Failed to create close cash register request", e);
            throw new ApiInvalidArgumentsException(e);
        }

        try (var httpClient = HttpClients.createDefault()) {
            return httpClient.execute(request, responseHandler);
        } catch (IOException e) {
            LOGGER.error("Failed to execute close cash register request", e);
            throw new ApiClientException(e);
        }
    }
}
