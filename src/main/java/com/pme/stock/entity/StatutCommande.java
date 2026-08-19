package com.pme.stock.entity;

// Définit les états possibles d'une commande client pendant son cycle de vente et de livraison.
public enum StatutCommande {
    BROUILLON, CONFIRMEE, EN_PREPARATION, EXPEDIEE, LIVREE, ANNULEE
}
