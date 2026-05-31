package com.nexra.hrms.nexra.modules.crm;

import com.nexra.hrms.nexra.support.AbstractMySQLIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = "nexra.crm.enforce-auth=true")
@ActiveProfiles("test")
class CrmContactMySQLIntegrationTest extends AbstractMySQLIntegrationTest {

    @Test
    void contextLoadsWithMySqlContainer() {
        // Validates Flyway migrations against MySQL dialect when Docker is available.
    }
}
