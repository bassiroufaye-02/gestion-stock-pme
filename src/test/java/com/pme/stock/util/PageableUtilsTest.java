package com.pme.stock.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PageableUtils")
class PageableUtilsTest {

    @Test
    @DisplayName("ignore un tri invalide (ex. string) et applique le tri par défaut")
    void sanitize_triInvalide_utiliseTriParDefaut() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("string").ascending());

        Pageable result = PageableUtils.sanitize(pageable, "raisonSociale", PageableUtils.CLIENT_SORT_FIELDS);

        assertThat(result.getSort().getOrderFor("raisonSociale")).isNotNull();
        assertThat(result.getSort().getOrderFor("string")).isNull();
    }

    @Test
    @DisplayName("conserve un tri valide")
    void sanitize_triValide_conserveLeTri() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("code").descending());

        Pageable result = PageableUtils.sanitize(pageable, "raisonSociale", PageableUtils.CLIENT_SORT_FIELDS);

        assertThat(result.getSort().getOrderFor("code").isDescending()).isTrue();
    }
}
