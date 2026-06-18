package com.pme.stock.security;

import com.pme.stock.security.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtService - Tests unitaires")
class JwtServiceTest {

    private JwtService jwtService;

    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        // Clé de 64 caractères minimum pour HMAC-SHA256
        ReflectionTestUtils.setField(jwtService, "secret",
                "test-secret-key-very-long-for-hmac-sha256-at-least-64-chars-xxxxxxxx");
        ReflectionTestUtils.setField(jwtService, "expirationMs", 900000L); // 15 min

        userDetails = User.builder()
                .username("user@pme.com")
                .password("hashedPassword")
                .authorities(Collections.emptyList())
                .build();
    }

    @Test
    @DisplayName("✅ genererToken doit produire un token non nul")
    void genererToken_doitProduireTokenNonNul() {
        String token = jwtService.genererToken(userDetails);
        assertThat(token).isNotNull().isNotBlank();
    }

    @Test
    @DisplayName("✅ extraireEmail doit retourner le bon email")
    void extraireEmail_doitRetournerEmail() {
        String token = jwtService.genererToken(userDetails);
        String email = jwtService.extraireEmail(token);
        assertThat(email).isEqualTo("user@pme.com");
    }

    @Test
    @DisplayName("✅ validerToken doit retourner vrai pour un token valide")
    void validerToken_tokenValide_doitRetournerVrai() {
        String token = jwtService.genererToken(userDetails);
        assertThat(jwtService.validerToken(token)).isTrue();
    }

    @Test
    @DisplayName("❌ validerToken doit retourner faux pour un token falsifié")
    void validerToken_tokenFalsifie_doitRetournerFaux() {
        String tokenFalsifie = "eyJhbGciOiJIUzI1NiJ9.fakePayload.fakeSignature";
        assertThat(jwtService.validerToken(tokenFalsifie)).isFalse();
    }

    @Test
    @DisplayName("❌ validerToken doit retourner faux pour un token vide")
    void validerToken_tokenVide_doitRetournerFaux() {
        assertThat(jwtService.validerToken("")).isFalse();
    }

    @Test
    @DisplayName("❌ validerToken doit retourner faux pour un token expiré")
    void validerToken_tokenExpire_doitRetournerFaux() {
        // Token avec expiration à -1ms (déjà expiré)
        ReflectionTestUtils.setField(jwtService, "expirationMs", -1L);
        String tokenExpire = jwtService.genererToken(userDetails);
        assertThat(jwtService.validerToken(tokenExpire)).isFalse();
    }

    @Test
    @DisplayName("✅ estValide doit retourner vrai pour le bon utilisateur")
    void estValide_bonUtilisateur_doitRetournerVrai() {
        String token = jwtService.genererToken(userDetails);
        assertThat(jwtService.estValide(token, userDetails)).isTrue();
    }

    @Test
    @DisplayName("❌ estValide doit retourner faux si l'email ne correspond pas")
    void estValide_mauvaisUtilisateur_doitRetournerFaux() {
        String token = jwtService.genererToken(userDetails);

        UserDetails autreUser = User.builder()
                .username("autre@pme.com")
                .password("pass")
                .authorities(Collections.emptyList())
                .build();

        assertThat(jwtService.estValide(token, autreUser)).isFalse();
    }
}
