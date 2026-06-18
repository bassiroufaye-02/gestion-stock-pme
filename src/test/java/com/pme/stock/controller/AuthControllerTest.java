package com.pme.stock.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pme.stock.dto.request.ConnexionRequest;
import com.pme.stock.dto.request.InscriptionRequest;
import com.pme.stock.dto.request.RefreshTokenRequest;
import com.pme.stock.dto.response.AuthResponse;
import com.pme.stock.security.service.JwtService;
import com.pme.stock.security.service.UserDetailsServiceImpl;
import com.pme.stock.service.impl.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@DisplayName("AuthController - Tests d'intégration MockMvc")
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private AuthService authService;
    @MockBean private JwtService jwtService;
    @MockBean private UserDetailsServiceImpl userDetailsService;

    private AuthResponse buildAuthResponse() {
        return AuthResponse.builder()
                .accessToken("eyJhbGciOiJIUzI1NiJ9.test")
                .refreshToken("uuid-refresh-token")
                .tokenType("Bearer")
                .expiresIn(900L)
                .email("jean.dupont@pme.com")
                .nomComplet("Jean Dupont")
                .roles(Set.of("ROLE_EMPLOYE"))
                .build();
    }

    @Test
    @DisplayName("✅ POST /auth/connexion - identifiants valides retourne 200 + tokens")
    void connexion_identifiantsValides_retourne200() throws Exception {
        // GIVEN
        ConnexionRequest request = new ConnexionRequest();
        request.setEmail("jean.dupont@pme.com");
        request.setMotDePasse("Pass@123");

        when(authService.connecter(any())).thenReturn(buildAuthResponse());

        // WHEN + THEN
        mockMvc.perform(post("/api/v1/auth/connexion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.email").value("jean.dupont@pme.com"));
    }

    @Test
    @DisplayName("❌ POST /auth/connexion - email invalide retourne 400")
    void connexion_emailInvalide_retourne400() throws Exception {
        // GIVEN
        ConnexionRequest request = new ConnexionRequest();
        request.setEmail("pas-un-email");
        request.setMotDePasse("Pass@123");

        // WHEN + THEN
        mockMvc.perform(post("/api/v1/auth/connexion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("❌ POST /auth/connexion - champs vides retourne 400")
    void connexion_champsVides_retourne400() throws Exception {
        // GIVEN
        ConnexionRequest request = new ConnexionRequest();

        // WHEN + THEN
        mockMvc.perform(post("/api/v1/auth/connexion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("✅ POST /auth/inscription - données valides retourne 201")
    void inscription_donneesValides_retourne201() throws Exception {
        // GIVEN
        InscriptionRequest request = new InscriptionRequest();
        request.setNom("Dupont");
        request.setPrenom("Jean");
        request.setEmail("jean.dupont@pme.com");
        request.setMotDePasse("Pass@123!");

        when(authService.inscrire(any())).thenReturn(buildAuthResponse());

        // WHEN + THEN
        mockMvc.perform(post("/api/v1/auth/inscription")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.email").value("jean.dupont@pme.com"));
    }

    @Test
    @DisplayName("✅ POST /auth/deconnexion - utilisateur authentifié retourne 204")
    @WithMockUser(username = "jean.dupont@pme.com", roles = "EMPLOYE")
    void deconnexion_utilisateurAuthentifie_retourne204() throws Exception {
        mockMvc.perform(post("/api/v1/auth/deconnexion").with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("❌ POST /auth/deconnexion - utilisateur non authentifié retourne 401")
    void deconnexion_nonAuthentifie_retourne401() throws Exception {
        mockMvc.perform(post("/api/v1/auth/deconnexion").with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("✅ POST /auth/refresh - refresh token valide retourne 200 + tokens")
    void refresh_tokenValide_retourne200() throws Exception {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("uuid-refresh-token");

        when(authService.rafraichirToken("uuid-refresh-token")).thenReturn(buildAuthResponse());

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists());
    }
}
