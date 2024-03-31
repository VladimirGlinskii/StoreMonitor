package ru.vglinskii.storemonitor.functionscommon.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public class ResourceUtils {
    public static String getResourceContent(String resource) throws IOException {
        try (var inputStream = ResourceUtils.class.getResourceAsStream(resource);) {
            if (inputStream == null) {
                throw new IllegalArgumentException("Resource not found: " + resource);
            }

            return new BufferedReader(new InputStreamReader(inputStream))
                    .lines().collect(Collectors.joining("\n"));
        }
    }
}
