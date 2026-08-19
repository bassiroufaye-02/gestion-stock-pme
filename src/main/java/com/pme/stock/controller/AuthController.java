package com.pme.stock.controller;

import com.pme.stock.dto.request.ConnexionRequest;
import com.pme.stock.dto.request.InscriptionRequest;
import com.pme.stock.dto.request.RefreshTokenRequest;
import com.pme.stock.dto.response.AuthResponse;
import com.pme.stock.service.impl.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.Instant;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentification", description = "Inscription, connexion et gestion des tokens")
public class AuthController {

    private final AuthService authService;

    // Permet de créer un compte utilisateur et de retourner un token d'accès immédiatement.
    @PostMapping("/inscription")
    @Operation(summary = "Créer un nouveau compte utilisateur")
    public ResponseEntity<AuthResponse> inscrire(@Valid @RequestBody InscriptionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.inscrire(request));
    }

    // Permet à un utilisateur existant de se connecter et de récupérer les JWT pour accéder aux ressources sécurisées.
    @PostMapping("/connexion")
    @Operation(summary = "Se connecter et obtenir les tokens JWT")
    public ResponseEntity<AuthResponse> connecter(@Valid @RequestBody ConnexionRequest request) {
        return ResponseEntity.ok(authService.connecter(request));
    }

    // Permet de renouveler le token d'accès sans obliger l'utilisateur à se reconnecter.
    @PostMapping("/refresh")
    @Operation(summary = "Rafraîchir le token d'accès via le refresh token")
    public ResponseEntity<AuthResponse> rafraichir(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.rafraichirToken(request.getRefreshToken()));
    }

    // Permet de fermer la session active en invalidant le refresh token de l'utilisateur authentifié.
    @PostMapping("/deconnexion")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Se déconnecter (invalider le refresh token)")
    public ResponseEntity<?> deconnecter(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                    HttpStatus.UNAUTHORIZED, "Authentification requise pour se déconnecter");
            pd.setTitle("Non authentifié");
            pd.setType(URI.create("https://pme.com/errors/unauthorized"));
            pd.setProperty("timestamp", Instant.now());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(pd);
        }
        authService.deconnecter(userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}
