package com.albaraka_bank.albaraka.albaraka_v1;

import com.albaraka.AlbarakaV1Application;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
        classes = AlbarakaV1Application.class,
        properties = {
                "spring.security.oauth2.resourceserver.jwt.issuer-uri=http://127.0.0.1:8080/realms/albaraka-realm"
        }
)
class AlbarakaV1ApplicationTests {
    @Test
    void contextLoads() {}
}
