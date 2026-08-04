package dev.applytrack.backend.config;

import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.context.ApplicationListener;

public class RequiredProfileListener implements ApplicationListener<ApplicationPreparedEvent> {

    @Override
    public void onApplicationEvent(ApplicationPreparedEvent event) {
        if (event.getApplicationContext().getEnvironment().getActiveProfiles().length == 0) {
            throw new IllegalStateException(
                    "No active profile set. Please set SPRING_PROFILES_ACTIVE environment variable to one of the following: dev, test, prod.");
        }
    }
}