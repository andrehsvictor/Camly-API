package andrehsvictor.camly;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import io.restassured.RestAssured;

@Testcontainers
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class AbstractIntegrationTest {

    @LocalServerPort
    private int port;

    @Container
    private static final GenericContainer<?> redis = new GenericContainer<>("redis:alpine")
            .withExposedPorts(6379)
            .withReuse(true);

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:alpine")
            .withDatabaseName("camly")
            .withUsername("camly")
            .withPassword("camly")

            .withReuse(true);

    @Container
    private static final GenericContainer<?> minio = new GenericContainer<>("quay.io/minio/minio:latest")
            .withCommand("server /data")
            .withExposedPorts(9000)
            .withEnv("MINIO_ROOT_USER", "minio")
            .withEnv("MINIO_ROOT_PASSWORD", "minio123")
            .withReuse(true);

    @Container
    private static final GenericContainer<?> mailHog = new GenericContainer<>("mailhog/mailhog:latest")
            .withExposedPorts(1025, 8025)
            .withEnv("MH_STORAGE", "memory")
            .waitingFor(Wait.forHttp("/api/v1/messages").forPort(8025).forStatusCode(200))
            .withReuse(true);

    @BeforeAll
    static void startContainers() {
        redis.start();
        postgres.start();
        minio.start();
        mailHog.start();

        assertThat(postgres.isRunning()).isTrue();
        assertThat(redis.isRunning()).isTrue();
        assertThat(minio.isRunning()).isTrue();
        assertThat(mailHog.isRunning()).isTrue();
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> postgres.getJdbcUrl());
        registry.add("spring.datasource.username", () -> postgres.getUsername());
        registry.add("spring.datasource.password", () -> postgres.getPassword());
        registry.add("spring.data.redis.host", () -> redis.getHost());
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
        registry.add("camly.minio.endpoint", () -> "http://" + minio.getHost() + ":" + minio.getMappedPort(9000));
        registry.add("camly.minio.admin.username", () -> minio.getEnvMap().get("MINIO_ROOT_USER"));
        registry.add("camly.minio.admin.password", () -> minio.getEnvMap().get("MINIO_ROOT_PASSWORD"));
        registry.add("camly.minio.bucket.name", () -> "camly");
        registry.add("spring.mail.host", () -> mailHog.getHost());
        registry.add("spring.mail.port", () -> mailHog.getMappedPort(1025));

    }

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    protected String getMailHogUrl() {
        return "http://" + mailHog.getHost() + ":" + mailHog.getMappedPort(8025);
    }

}
