package com.pme.stock.service.impl;

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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UtilisateurRepository utilisateurRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Value("${app.jwt.expiration-ms}")
    private long expirationMs;

    @Value("${app.jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    @Transactional
    public AuthResponse inscrire(InscriptionRequest request) {
        if (utilisateurRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Un compte existe déjà avec l'email : " + request.getEmail());
        }

        Utilisateur utilisateur = Utilisateur.builder()
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .email(request.getEmail())
                .motDePasse(passwordEncoder.encode(request.getMotDePasse()))
                .actif(true)
                .build();

        Set<String> rolesRequis = (request.getRoles() == null || request.getRoles().isEmpty())
                ? Set.of("ROLE_EMPLOYE")
                : request.getRoles();

        rolesRequis.forEach(nomRole -> {
            Role role = roleRepository.findByNom(nomRole)
                    .orElseThrow(() -> new BusinessException("Rôle inconnu : " + nomRole));
            utilisateur.ajouterRole(role);
        });

        utilisateurRepository.save(utilisateur);
        log.info("Nouvel utilisateur inscrit : {}", utilisateur.getEmail());

        return genererAuthResponse(utilisateur);
    }

    @Transactional
    public AuthResponse connecter(ConnexionRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getMotDePasse())
        );

        Utilisateur utilisateur = utilisateurRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException("Utilisateur introuvable"));

        // Supprimer les anciens refresh tokens
        refreshTokenRepository.deleteByUtilisateur(utilisateur);

        log.info("Connexion réussie pour : {}", utilisateur.getEmail());
        return genererAuthResponse(utilisateur);
    }

    @Transactional
    public AuthResponse rafraichirToken(String refreshTokenStr) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenStr)
                .orElseThrow(() -> new BusinessException("Refresh token invalide ou introuvable"));

        if (refreshToken.isExpire()) {
            refreshTokenRepository.delete(refreshToken);
            throw new BusinessException("Refresh token expiré. Veuillez vous reconnecter.");
        }

        Utilisateur utilisateur = refreshToken.getUtilisateur();
        refreshTokenRepository.delete(refreshToken);

        return genererAuthResponse(utilisateur);
    }

    @Transactional
    public void deconnecter(String email) {
        utilisateurRepository.findByEmail(email).ifPresent(u -> {
            refreshTokenRepository.deleteByUtilisateur(u);
            log.info("Déconnexion de : {}", email);
        });
    }

    private AuthResponse genererAuthResponse(Utilisateur utilisateur) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(utilisateur.getEmail());
        String accessToken = jwtService.genererToken(userDetails);

        RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(refreshExpirationMs))
                .utilisateur(utilisateur)
                .build();
        refreshTokenRepository.save(refreshToken);

        Set<String> roles = utilisateur.getRoles().stream()
                .map(Role::getNom)
                .collect(Collectors.toSet());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(expirationMs / 1000)
                .email(utilisateur.getEmail())
                .nomComplet(utilisateur.getPrenom() + " " + utilisateur.getNom())
                .roles(roles)
                .build();
    }
}
