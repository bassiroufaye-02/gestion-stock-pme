package com.pme.stock.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Gestion Stock PME — API",
        version = "v1.0",
        description = "Plateforme de gestion de stock et commandes pour PME. Projet Master Génie Logiciel.",
        contact = @Contact(name = "Équipe PME", email = "contact@pme.com")
    ),
    servers = {
        @Server(url = "http://localhost:8080", description = "Serveur de développement")
    }
)
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT",
    in = SecuritySchemeIn.HEADER,
    description = "Entrez le token JWT obtenu via /api/v1/auth/connexion"
)
public class OpenApiConfig {
}
