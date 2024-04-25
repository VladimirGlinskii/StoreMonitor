package ru.vglinskii.storemonitor.functionscommon.database;

import java.util.Properties;
import ru.vglinskii.storemonitor.functionscommon.config.CommonApplicationProperties;

public class DatabaseConnectivityFactory {
    public static DatabaseConnectivity create(
            CommonApplicationProperties properties
    ) {
        return create(
                properties.getDbUrl(),
                properties.getDbUser(),
                properties.getDbPassword()
        );
    }

    public static DatabaseConnectivity create(
            String url,
            String user,
            String password
    ) {
        var props = new Properties();
        props.setProperty("ssl", "true");
        props.setProperty("user", user);
        props.setProperty("password", password);

        return new DatabaseConnectivity(url, props);
    }
}
