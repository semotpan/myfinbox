package io.myfinbox

import org.springframework.boot.SpringApplication
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

@TestConfiguration(proxyBeanMethods = false)
class TestServerApplication {

    @Bean
    @ServiceConnection
    PostgreSQLContainer<? extends PostgreSQLContainer> mysqlContainer() {
        return new PostgreSQLContainer<>(DockerImageName.parse("postgres:16"))
    }

    static void main(String[] args) {
        SpringApplication.from(ServerApplication::main)
                .with(TestServerApplication.class)
                .run(args)
    }
}
