package com.leave.management.system.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI leaveManagementOpenAPI() {
        Server devServer = new Server();
        devServer.setUrl("http://localhost:8087");
        devServer.setDescription("Development server for Leave Management System");

        Contact contact = new Contact()
                .name("Leave Management Support")
                .email("support@leavemanagement.com")
                .url("http://localhost:8087");

        License license = new License()
                .name("MIT License")
                .url("https://choosealicense.com/licenses/mit/");

        Info info = new Info()
                .title("Leave Management System API")
                .version("1.0.0")
                .description("HR leave management system API Module")
                .termsOfService("http://localhost:8087/terms")
                .contact(contact)
                .license(license);

        return new OpenAPI()
                .info(info)
                .servers(List.of(devServer));
    }
}
