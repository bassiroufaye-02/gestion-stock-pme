package com.pme.stock.entity;

// Définit les états d'une commande fournisseur du moment de la commande jusqu'à la réception.
public enum StatutCommandeFournisseur {
    BROUILLON,      // Commande en cours de rédaction
    ENVOYEE,        // Commande envoyée au fournisseur
    RECUE_PARTIELLE,// Réception partielle
    RECUE,          // Totalement réceptionnée
    ANNULEE         // Annulée avant réception
}
