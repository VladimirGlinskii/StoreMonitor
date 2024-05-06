package ru.vglinskii.storemonitor.cashiersimulator.serviceclient;

import java.io.IOException;
import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vglinskii.storemonitor.cashiersimulator.model.Cashier;
import ru.vglinskii.storemonitor.functionscommon.serviceclient.ServiceClientException;
import ru.vglinskii.storemonitor.functionscommon.http.AppHttpResponseHandler;
import ru.vglinskii.storemonitor.functionscommon.model.CashRegister;

public class CashRegisterServiceClient {
    private final static Logger LOGGER = LoggerFactory.getLogger(CashRegisterServiceClient.class);
    private String apiUrl;
    private AppHttpResponseHandler responseHandler;
    private CloseableHttpClient httpClient;

    public CashRegisterServiceClient(String apiUrl) {
        this.apiUrl = apiUrl;
        this.responseHandler = new AppHttpResponseHandler();
        this.httpClient = HttpClients.createDefault();
    }

    public boolean openCashRegister(CashRegister cashRegister, Cashier cashier) {
        try {
            var request = new HttpPost(String.format("%s/cash-registers/%d/sessions", apiUrl, cashRegister.getId()));
            request.setHeader("X-Secret-Key", cashier.getSecret());

            var response = httpClient.execute(request, responseHandler);

            return response.is2xxSuccessful();
        } catch (IOException e) {
            LOGGER.error("Failed to execute open cash register request", e);
            throw new ServiceClientException(e);
        }
    }

    public boolean closeCashRegister(CashRegister cashRegister, Cashier cashier) {
        try {
            var request = new HttpDelete(String.format("%s/cash-registers/%d/sessions", apiUrl, cashRegister.getId()));
            request.setHeader("X-Secret-Key", cashier.getSecret());

            var response = httpClient.execute(request, responseHandler);

            return response.is2xxSuccessful();
        } catch (IOException e) {
            LOGGER.error("Failed to execute close cash register request", e);
            throw new ServiceClientException(e);
        }
    }
}
