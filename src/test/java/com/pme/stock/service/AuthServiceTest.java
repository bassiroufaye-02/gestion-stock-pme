package com.pme.stock.service;

import com.pme.stock.dto.request.ConnexionRequest;
import com.pme.stock.dto.request.InscriptionRequest;
import com.pme.stock.dto.response.AuthResponse;
import com.pme.stock.entity.RefreshToken;
import com.pme.stock.entity.Role;
import com.pme.stock.entity.Utilisateur;
import com.pme.stock.exception.BusinessException;
import com.pme.stock.repository.RefreshTokenRepository;
import com.pme.stock.repository.RoleRepository;
import com.pme.stock.repository.UtilisateurRepository;
import com.pme.stock.security.service.JwtService;
import com.pme.stock.service.impl.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService - Tests unitaires")
class AuthServiceTest {

    @Mock private UtilisateurRepository utilisateurRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtService jwtService;
    @Mock private UserDetailsService userDetailsService;

    @InjectMocks
    private AuthService authService;

    private Role roleEmploye;
    private Utilisateur utilisateur;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "expirationMs", 900000L);
        ReflectionTestUtils.setField(authService, "refreshExpirationMs", 604800000L);

        roleEmploye = Role.builder().id(1L).nom("ROLE_EMPLOYE").build();

        utilisateur = Utilisateur.builder()
                .id(1L).nom("Dupont").prenom("Jean")
                .email("jean.dupont@pme.com")
                .motDePasse("$2a$12$encoded")
                .actif(true).build();
        utilisateur.ajouterRole(roleEmploye);
    }

    // ============================================================
    //  INSCRIPTION
    // ============================================================

    @Test
    @DisplayName("✅ inscrire - nouvel utilisateur avec rôle par défaut")
    void inscrire_nouvelEmail_doitCreerUtilisateur() {
        // GIVEN
        InscriptionRequest request = new InscriptionRequest();
        request.setNom("Dupont"); request.setPrenom("Jean");
        request.setEmail("jean.dupont@pme.com"); request.setMotDePasse("Pass@123");

        UserDetails userDetails = User.withUsername("jean.dupont@pme.com")
                .password("encoded").roles("EMPLOYE").build();

        when(utilisateurRepository.existsByEmail(anyString())).thenReturn(false);
        when(roleRepository.findByNom("ROLE_EMPLOYE")).thenReturn(Optional.of(roleEmploye));
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$12$encoded");
        when(utilisateurRepository.save(any())).thenReturn(utilisateur);
        when(userDetailsService.loadUserByUsername(anyString())).thenReturn(userDetails);
        when(jwtService.genererToken(any())).thenReturn("access-token-xyz");
        when(refreshTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // WHEN
        AuthResponse response = authService.inscrire(request);

        // THEN
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("access-token-xyz");
        assertThat(response.getEmail()).isEqualTo("jean.dupont@pme.com");
        verify(utilisateurRepository).save(any(Utilisateur.class));
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("❌ inscrire - email déjà utilisé doit lever BusinessException")
    void inscrire_emailExistant_doitLeverBusinessException() {
        // GIVEN
        InscriptionRequest request = new InscriptionRequest();
        request.setEmail("jean.dupont@pme.com"); request.setMotDePasse("Pass@123");
        when(utilisateurRepository.existsByEmail("jean.dupont@pme.com")).thenReturn(true);

        // WHEN + THEN
        assertThatThrownBy(() -> authService.inscrire(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("existe déjà");
        verify(utilisateurRepository, never()).save(any());
    }

    // ============================================================
    //  CONNEXION
    // ============================================================

    @Test
    @DisplayName("✅ connecter - identifiants valides retourne les tokens")
    void connecter_identifiantsValides_doitRetournerTokens() {
        // GIVEN
        ConnexionRequest request = new ConnexionRequest();
        request.setEmail("jean.dupont@pme.com"); request.setMotDePasse("Pass@123");

        UserDetails userDetails = User.withUsername("jean.dupont@pme.com")
                .password("encoded").roles("EMPLOYE").build();

        when(authenticationManager.authenticate(any())).thenReturn(
                new UsernamePasswordAuthenticationToken("jean.dupont@pme.com", "Pass@123"));
        when(utilisateurRepository.findByEmail("jean.dupont@pme.com"))
                .thenReturn(Optional.of(utilisateur));
        when(userDetailsService.loadUserByUsername(anyString())).thenReturn(userDetails);
        when(jwtService.genererToken(any())).thenReturn("fresh-access-token");
        when(refreshTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // WHEN
        AuthResponse response = authService.connecter(request);

        // THEN
        assertThat(response.getAccessToken()).isEqualTo("fresh-access-token");
        assertThat(response.getRefreshToken()).isNotBlank();
        verify(refreshTokenRepository).deleteByUtilisateur(utilisateur);
    }

    // ============================================================
    //  REFRESH TOKEN
    // ============================================================

    @Test
    @DisplayName("❌ rafraichirToken - token expiré doit lever BusinessException")
    void rafraichirToken_tokenExpire_doitLeverException() {
        // GIVEN
        RefreshToken expired = RefreshToken.builder()
                .token("expired-token")
                .expiryDate(Instant.now().minusSeconds(3600))
                .utilisateur(utilisateur)
                .build();

        when(refreshTokenRepository.findByToken("expired-token")).thenReturn(Optional.of(expired));

        // WHEN + THEN
        assertThatThrownBy(() -> authService.rafraichirToken("expired-token"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("expiré");
        verify(refreshTokenRepository).delete(expired);
    }

    @Test
    @DisplayName("❌ rafraichirToken - token introuvable doit lever BusinessException")
    void rafraichirToken_tokenInconnu_doitLeverException() {
        when(refreshTokenRepository.findByToken("unknown")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> authService.rafraichirToken("unknown"))
                .isInstanceOf(BusinessException.class);
    }
}
