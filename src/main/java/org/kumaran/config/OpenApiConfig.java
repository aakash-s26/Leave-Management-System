package org.kumaran.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme.Type;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT",
    in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {

    @Bean
    public OpenAPI userMicroserviceOpenAPI() {
        Server devServer = new Server();
        devServer.setUrl("http://localhost:8081");
        devServer.setDescription("Development server");

        Contact contact = new Contact();
        contact.setEmail("support@leavepal.com");
        contact.setName("LeavePal Support");

        License license = new License();
        license.setName("MIT License");
        license.setUrl("https://choosealicense.com/licenses/mit/");

        Info info = new Info()
                .title("LeavePal LMS API")
            .version("2.0")
                .contact(contact)
                .description("Leave Management System API for employee leave tracking and administration")
                .license(license);

        return new OpenAPI()
                .info(info)
            .servers(List.of(devServer))
            .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
            .schemaRequirement("bearerAuth", new io.swagger.v3.oas.models.security.SecurityScheme().type(Type.HTTP).scheme("bearer").bearerFormat("JWT"));
    }
}
