package com.pme.stock.security.service;

import com.pme.stock.entity.Role;
import com.pme.stock.entity.Utilisateur;
import com.pme.stock.repository.UtilisateurRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserDetailsServiceImpl - Tests unitaires")
class UserDetailsServiceImplTest {

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @Test
    @DisplayName("✅ loadUserByUsername - Utilisateur actif retourné avec ses rôles")
    void loadUserByUsername_actif_retourneUserDetails() {
        // GIVEN
        Role roleAdmin = Role.builder().id(1L).nom("ROLE_ADMIN").build();
        Utilisateur utilisateur = Utilisateur.builder()
                .id(1L)
                .email("admin@pme.com")
                .motDePasse("password")
                .actif(true)
                .roles(Set.of(roleAdmin))
                .build();

        given(utilisateurRepository.findByEmail("admin@pme.com")).willReturn(Optional.of(utilisateur));

        // WHEN
        UserDetails userDetails = userDetailsService.loadUserByUsername("admin@pme.com");

        // THEN
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("admin@pme.com");
        assertThat(userDetails.getPassword()).isEqualTo("password");
        assertThat(userDetails.getAuthorities()).hasSize(1);
        assertThat(userDetails.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_ADMIN");
    }

    @Test
    @DisplayName("❌ loadUserByUsername - Utilisateur inactif lève UsernameNotFoundException")
    void loadUserByUsername_inactif_doitLeverException() {
        // GIVEN
        Utilisateur utilisateur = Utilisateur.builder()
                .id(1L)
                .email("admin@pme.com")
                .motDePasse("password")
                .actif(false)
                .build();

        given(utilisateurRepository.findByEmail("admin@pme.com")).willReturn(Optional.of(utilisateur));

        // WHEN / THEN
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("admin@pme.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("Compte désactivé");
    }

    @Test
    @DisplayName("❌ loadUserByUsername - Utilisateur inexistant lève UsernameNotFoundException")
    void loadUserByUsername_inexistant_doitLeverException() {
        // GIVEN
        given(utilisateurRepository.findByEmail("non-existent@pme.com")).willReturn(Optional.empty());

        // WHEN / THEN
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("non-existent@pme.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("Utilisateur non trouvé");
    }
}
