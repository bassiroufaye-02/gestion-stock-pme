package com.pme.stock.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

class BcryptHashCheckTest {

    private static final String ADMIN_HASH =
            "$2a$12$DAGHY8lfuKZbcpLbTH7ZLOYxINdltV7/RBnlA2fMeA5giKtsSv/tS";

    @Test
    void adminPasswordHash_mustMatchAdminAt123() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
        assertThat(encoder.matches("Admin@123", ADMIN_HASH)).isTrue();
    }
}
