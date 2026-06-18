package com.pme.stock.security.filter;

import com.pme.stock.security.service.JwtService;
import com.pme.stock.security.service.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.IOException;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthenticationFilter - Tests unitaires")
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsServiceImpl userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("✅ doFilterInternal - Aucun en-tête Authorization - Continue le filtre")
    void doFilterInternal_sansAuthorizationHeader_continueFiltre() throws ServletException, IOException {
        given(request.getHeader("Authorization")).willReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("✅ doFilterInternal - En-tête Authorization ne commence pas par Bearer - Continue le filtre")
    void doFilterInternal_sansBearer_continueFiltre() throws ServletException, IOException {
        given(request.getHeader("Authorization")).willReturn("Basic credentials");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("✅ doFilterInternal - Token invalide - Continue le filtre sans authentifier")
    void doFilterInternal_tokenInvalide_continueFiltreSansAuthentifier() throws ServletException, IOException {
        given(request.getHeader("Authorization")).willReturn("Bearer invalid-token");
        given(jwtService.validerToken("invalid-token")).willReturn(false);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("✅ doFilterInternal - Token valide, authentifie l'utilisateur avec succès")
    void doFilterInternal_tokenValide_authentifieUtilisateur() throws ServletException, IOException {
        given(request.getHeader("Authorization")).willReturn("Bearer valid-token");
        given(jwtService.validerToken("valid-token")).willReturn(true);
        given(jwtService.extraireEmail("valid-token")).willReturn("user@pme.com");

        UserDetails userDetails = new User("user@pme.com", "password", Collections.emptyList());
        given(userDetailsService.loadUserByUsername("user@pme.com")).willReturn(userDetails);
        given(jwtService.estValide("valid-token", userDetails)).willReturn(true);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo("user@pme.com");
    }

    @Test
    @DisplayName("✅ doFilterInternal - Token valide mais estValide retourne faux - Pas d'authentification")
    void doFilterInternal_tokenValideMaisEstValideFaux_pasDAuthentification() throws ServletException, IOException {
        given(request.getHeader("Authorization")).willReturn("Bearer valid-token");
        given(jwtService.validerToken("valid-token")).willReturn(true);
        given(jwtService.extraireEmail("valid-token")).willReturn("user@pme.com");

        UserDetails userDetails = new User("user@pme.com", "password", Collections.emptyList());
        given(userDetailsService.loadUserByUsername("user@pme.com")).willReturn(userDetails);
        given(jwtService.estValide("valid-token", userDetails)).willReturn(false);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("✅ doFilterInternal - Exception levée lors du traitement - Est capturée et continue le filtre")
    void doFilterInternal_exceptionLevee_estCaptureeEtFiltreContinue() throws ServletException, IOException {
        given(request.getHeader("Authorization")).willReturn("Bearer token-provoking-error");
        given(jwtService.validerToken("token-provoking-error")).willThrow(new RuntimeException("Expired token"));

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}
