package ru.vglinskii.storemonitor.functionscommon.utils;

import yandex.cloud.sdk.functions.Context;

public class TestContext implements Context {
    @Override
    public String getRequestId() {
        return null;
    }

    @Override
    public String getFunctionId() {
        return null;
    }

    @Override
    public String getFunctionVersionId() {
        return null;
    }

    @Override
    public int getMemoryLimit() {
        return 0;
    }

    @Override
    public String getTokenJson() {
        return "{\"access_token\": \"test\"}";
    }
}
