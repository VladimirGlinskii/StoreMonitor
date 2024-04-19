package ru.vglinskii.storemonitor.functionscommon.api;

import java.io.IOException;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.EntityUtils;

public class AppHttpResponseHandler implements HttpClientResponseHandler<HttpResponse> {
    @Override
    public HttpResponse handleResponse(ClassicHttpResponse classicHttpResponse) throws HttpException, IOException {
        var response = new HttpResponse();
        response.setStatus(classicHttpResponse.getCode());

        var entity = classicHttpResponse.getEntity();
        if (entity != null) {
            response.setBody(EntityUtils.toString(entity));
        }

        return response;
    }
}
