package org.grants.harvester;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration class to set up Cross-Origin Resource Sharing (CORS) for the application.
 * 
 * This allows web clients from different origins to access the /search endpoint.
 */
@Configuration
public class CorsConfig {

    /**
     * Defines a WebMvcConfigurer bean to customize CORS mappings.
     *
     * @return a WebMvcConfigurer with custom CORS settings
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            /**
             * Configures CORS mapping for the /search endpoint.
             * 
             * Allows POST requests from any origin (for development/testing).
             * In production, restrict allowedOrigins to trusted domains.
             */
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/search")
                        .allowedOrigins("*")  // TEMPORARY: allow any origin
                        .allowedMethods("POST");
            }
        };
    }
}
