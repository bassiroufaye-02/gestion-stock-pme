package com.pme.stock.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Set;

public final class PageableUtils {

    public static final Set<String> CLIENT_SORT_FIELDS = Set.of(
            "id", "code", "raisonSociale", "email", "ville", "createdAt");

    public static final Set<String> COMMANDE_SORT_FIELDS = Set.of(
            "id", "numeroCommande", "dateCommande", "statut", "montantHT", "montantTTC", "createdAt");

    public static final Set<String> FOURNISSEUR_SORT_FIELDS = Set.of(
            "id", "code", "raisonSociale", "email", "ville", "actif", "createdAt");

    public static final Set<String> COMMANDE_FOURNISSEUR_SORT_FIELDS = Set.of(
            "id", "numeroCommande", "dateCommande", "statut", "montantHT", "montantTTC", "createdAt");

    private PageableUtils() {
    }

    // Permet de traiter sanitize.
    public static Pageable sanitize(Pageable pageable, String defaultSortField, Set<String> allowedFields) {
        Sort sort = sanitizeSort(pageable.getSort(), defaultSortField, allowedFields);
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
    }

    // Permet de traiter sanitizeSort.
    private static Sort sanitizeSort(Sort sort, String defaultSortField, Set<String> allowedFields) {
        if (sort.isUnsorted()) {
            return Sort.by(defaultSortField).ascending();
        }

        List<Sort.Order> validOrders = sort.stream()
                .filter(order -> allowedFields.contains(order.getProperty()))
                .toList();

        if (validOrders.isEmpty()) {
            return Sort.by(defaultSortField).ascending();
        }

        return Sort.by(validOrders);
    }
}
