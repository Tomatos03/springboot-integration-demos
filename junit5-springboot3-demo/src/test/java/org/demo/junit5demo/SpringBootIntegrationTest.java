package org.demo.junit5demo;

import org.demo.junit5demo.service.CalculatorService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(
        classes = Junit5Springboot3DemoApplication.class,
        properties = {
                "demo.message=Hello from @SpringBootTest properties"
        }
)
@TestPropertySource(properties = {
        "demo.region=CN",
        "demo.timeout=30"
})
@DisplayName("Spring Boot 3 Integration Test Demo")
@Tag("integration")
class SpringBootIntegrationTest {

    @Autowired
    private CalculatorService calculatorService;

    @Autowired
    private Environment environment;

    @Test
    @DisplayName("Should load Spring context and inject beans")
    void shouldLoadContextAndInjectBeans() {
        assertAll(
                () -> assertNotNull(calculatorService, "CalculatorService should be injected"),
                () -> assertNotNull(environment, "Environment should be injected")
        );
    }

    @Test
    @DisplayName("Should read inline properties from @SpringBootTest and @TestPropertySource")
    void shouldReadTestProperties() {
        assertAll(
                () -> assertEquals("Hello from @SpringBootTest properties", environment.getProperty("demo.message")),
                () -> assertEquals("CN", environment.getProperty("demo.region")),
                () -> assertEquals("30", environment.getProperty("demo.timeout"))
        );
    }

    @Nested
    @DisplayName("With real Spring-managed service bean")
    class ServiceIntegrationCases {

        @Test
        @DisplayName("Should execute CalculatorService in integration test")
        void shouldUseCalculatorService() {
            assertAll(
                    () -> assertEquals(7, calculatorService.add(3, 4)),
                    () -> assertEquals(5, calculatorService.divide(10, 2))
            );
        }
    }
}