package ru.vglinskii.storemonitor.cashiersimulator.api;

import java.io.IOException;
import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vglinskii.storemonitor.cashiersimulator.model.Cashier;
import ru.vglinskii.storemonitor.functionscommon.api.ApiClientException;
import ru.vglinskii.storemonitor.functionscommon.api.AppHttpResponseHandler;
import ru.vglinskii.storemonitor.functionscommon.api.HttpResponse;
import ru.vglinskii.storemonitor.functionscommon.model.CashRegister;

public class CashRegisterApi {
    private final static Logger LOGGER = LoggerFactory.getLogger(CashRegisterApi.class);
    private String apiUrl;
    private AppHttpResponseHandler responseHandler;
    private CloseableHttpClient httpClient;

    public CashRegisterApi(String apiUrl) {
        this.apiUrl = apiUrl;
        this.responseHandler = new AppHttpResponseHandler();
        this.httpClient = HttpClients.createDefault();
    }

    public HttpResponse openCashRegister(CashRegister cashRegister, Cashier cashier) {
        try {
            var request = new HttpPost(String.format("%s/cash-registers/%d/sessions", apiUrl, cashRegister.getId()));
            request.setHeader("X-Secret-Key", cashier.getSecret());

            return httpClient.execute(request, responseHandler);
        } catch (IOException e) {
            LOGGER.error("Failed to execute open cash register request", e);
            throw new ApiClientException(e);
        }
    }

    public HttpResponse closeCashRegister(CashRegister cashRegister, Cashier cashier) {
        try {
            var request = new HttpDelete(String.format("%s/cash-registers/%d/sessions", apiUrl, cashRegister.getId()));
            request.setHeader("X-Secret-Key", cashier.getSecret());

            return httpClient.execute(request, responseHandler);
        } catch (IOException e) {
            LOGGER.error("Failed to execute close cash register request", e);
            throw new ApiClientException(e);
        }
    }
}
