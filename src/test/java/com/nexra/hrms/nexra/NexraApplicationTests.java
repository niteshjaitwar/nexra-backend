package com.nexra.hrms.nexra;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Smoke test for the Nexra modular monolith bootstrap.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:nexra;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=validate",
    "spring.flyway.enabled=true",
    "app.auth.jwt.secret=test-jwt-secret-test-jwt-secret-test-jwt",
    "app.auth.oauth2.default-client-secret=test-client-secret",
    "app.auth.oauth2.ephemeral-key-enabled=true",
    "app.auth.mail.enabled=false",
    "app.auth.security.redis-enabled=false"
})
@ActiveProfiles("test")
class NexraApplicationTests {

    @Test
    void contextLoads() {
    }
}
