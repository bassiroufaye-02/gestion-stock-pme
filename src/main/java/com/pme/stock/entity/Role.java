package com.pme.stock.entity;

import jakarta.persistence.*;
import lombok.*;

// Représente un rôle d'autorisation attribué à un utilisateur pour gérer les accès.
@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nom", nullable = false, unique = true, length = 50)
    private String nom;
}
