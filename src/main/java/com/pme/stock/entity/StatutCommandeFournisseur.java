package com.pme.stock.entity;

public enum StatutCommandeFournisseur {
    BROUILLON,      // Commande en cours de rédaction
    ENVOYEE,        // Commande envoyée au fournisseur
    RECUE_PARTIELLE,// Réception partielle
    RECUE,          // Totalement réceptionnée
    ANNULEE         // Annulée avant réception
}
