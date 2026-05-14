package com.nexra.hrms.nexra.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Platform-wide Jackson ObjectMapper configuration. Registers a single primary
 * ObjectMapper bean with Java time support enabled and timestamps disabled.
 * All modules that need JSON serialization MUST inject this bean rather than
 * constructing their own ObjectMapper instances.
 *
 * @author niteshjaitwar
 */
@Configuration
public class NexraJacksonConfig {

    /**
     * Creates the shared Jackson ObjectMapper with platform-standard settings:
     * <ul>
     *   <li>JavaTimeModule registered for {@code Instant}, {@code LocalDate}, etc.</li>
     *   <li>WRITE_DATES_AS_TIMESTAMPS disabled so dates serialize as ISO-8601 strings.</li>
     * </ul>
     *
     * @return configured ObjectMapper bean
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
}
