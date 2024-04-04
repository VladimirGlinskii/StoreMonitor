package ru.vglinskii.storemonitor.cashiersimulator.api;

import java.io.IOException;
import org.apache.http.HttpEntity;
import org.apache.http.client.ResponseHandler;
import org.apache.http.util.EntityUtils;

public class AppHttpResponseHandler implements ResponseHandler<HttpResponse> {
    @Override
    public HttpResponse handleResponse(org.apache.http.HttpResponse httpResponse) throws IOException {
        var response = new HttpResponse();
        response.setStatus(httpResponse.getStatusLine().getStatusCode());

        HttpEntity entity = httpResponse.getEntity();
        if (entity != null) {
            response.setBody(EntityUtils.toString(entity));
        }

        return response;
    }
}
