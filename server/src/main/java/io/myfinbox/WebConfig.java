package io.myfinbox;

import io.myfinbox.shared.ApiFailureHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class WebConfig {

    @Bean
    public ApiFailureHandler apiFailureHandler() {
        return new ApiFailureHandler();
    }
}
