package com.example.spring_boot_mongodb_docker.config;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@EnableAspectJAutoProxy
public class MetricsConfig {

    private final MeterRegistry meterRegistry;

    // Counters for business metrics
    private final Counter userRegistrationCounter;
    private final Counter loginSuccessCounter;
    private final Counter loginFailureCounter;
    private final Counter itemCreationCounter;

    // Timers for performance metrics
    private final Timer databaseOperationTimer;
    private final Timer authenticationTimer;

    public MetricsConfig(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        // Initialize counters
        this.userRegistrationCounter = Counter.builder("app.user.registration")
                .description("Number of user registrations")
                .register(meterRegistry);

        this.loginSuccessCounter = Counter.builder("app.user.login.success")
                .description("Number of successful logins")
                .register(meterRegistry);

        this.loginFailureCounter = Counter.builder("app.user.login.failure")
                .description("Number of failed logins")
                .register(meterRegistry);

        this.itemCreationCounter = Counter.builder("app.item.creation")
                .description("Number of items created")
                .register(meterRegistry);

        // Initialize timers
        this.databaseOperationTimer = Timer.builder("app.database.operation.time")
                .description("Time taken for database operations")
                .register(meterRegistry);

        this.authenticationTimer = Timer.builder("app.authentication.time")
                .description("Time taken for authentication")
                .register(meterRegistry);
    }

    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }

    // Getters for counters and timers to be used in services

    public Counter getUserRegistrationCounter() {
        return userRegistrationCounter;
    }

    public Counter getLoginSuccessCounter() {
        return loginSuccessCounter;
    }

    public Counter getLoginFailureCounter() {
        return loginFailureCounter;
    }

    public Counter getItemCreationCounter() {
        return itemCreationCounter;
    }

    public Timer getDatabaseOperationTimer() {
        return databaseOperationTimer;
    }

    public Timer getAuthenticationTimer() {
        return authenticationTimer;
    }

    // Example method to record database operation time
    public <T> T recordDatabaseOperationTime(String operationName, java.util.function.Supplier<T> operation) {
        return databaseOperationTimer.record(() -> {
            try {
                return operation.get();
            } catch (Exception e) {
                // You might want to record errors as well
                meterRegistry.counter("app.database.operation.error", "operation", operationName).increment();
                throw e;
            }
        });
    }
}

