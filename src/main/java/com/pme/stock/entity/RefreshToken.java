package com.pme.stock.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

// Représente un jeton de rafraîchissement utilisé pour renouveler une session JWT.
@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token", nullable = false, unique = true, length = 250)
    private String token;

    @Column(name = "expiry_date", nullable = false)
    private Instant expiryDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;

    // Permet de vérifier si le jeton a expiré et doit être invalidé.
    public boolean isExpire() {
        return Instant.now().isAfter(this.expiryDate);
    }
}
