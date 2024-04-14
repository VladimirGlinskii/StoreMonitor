package ru.vglinskii.storemonitor.cashiersimulator.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vglinskii.storemonitor.cashiersimulator.model.CashRegister;
import ru.vglinskii.storemonitor.cashiersimulator.model.Cashier;
import ru.vglinskii.storemonitor.functionscommon.api.ApiClientException;
import ru.vglinskii.storemonitor.functionscommon.api.AppHttpResponseHandler;
import ru.vglinskii.storemonitor.functionscommon.api.HttpResponse;

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
        HttpUriRequest request = RequestBuilder.post()
                .setUri(String.format("%s/cash-registers/%d/sessions", apiUrl, cashRegister.getId()))
                .setHeader("X-Secret-Key", cashier.getSecret())
                .build();

        try (var httpClient = HttpClients.createDefault()) {
            return httpClient.execute(request, responseHandler);
        } catch (IOException e) {
            LOGGER.error("Failed to execute open cash register request", e);
            throw new ApiClientException(e);
        }
    }

    public HttpResponse closeCashRegister(CashRegister cashRegister, Cashier cashier) {
        HttpUriRequest request = RequestBuilder.delete()
                .setUri(String.format("%s/cash-registers/%d/sessions", apiUrl, cashRegister.getId()))
                .setHeader("X-Secret-Key", cashier.getSecret())
                .build();

        try (var httpClient = HttpClients.createDefault()) {
            return httpClient.execute(request, responseHandler);
        } catch (IOException e) {
            LOGGER.error("Failed to execute close cash register request", e);
            throw new ApiClientException(e);
        }
    }
}
