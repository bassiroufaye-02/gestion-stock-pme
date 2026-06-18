-- Recalcul des montants de lignes et commandes existantes
UPDATE lignes_commandes_clients
SET montant_ligne_ht = prix_unitaire_ht * quantite
WHERE montant_ligne_ht IS NULL OR montant_ligne_ht = 0;

UPDATE commandes_clients c
SET montant_ht = (
        SELECT COALESCE(SUM(l.montant_ligne_ht), 0)
        FROM lignes_commandes_clients l
        WHERE l.commande_id = c.id
    ),
    montant_tva = ROUND(
        (SELECT COALESCE(SUM(l.montant_ligne_ht), 0) FROM lignes_commandes_clients l WHERE l.commande_id = c.id)
        * COALESCE(c.taux_tva, 18.00) / 100,
        2
    )
WHERE montant_ht IS NULL OR montant_tva IS NULL OR montant_ttc IS NULL;

UPDATE commandes_clients
SET montant_ttc = montant_ht + montant_tva
WHERE montant_ttc IS NULL OR montant_ttc = 0;
