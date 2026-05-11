package com.wisewallet.account.domain.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class BalanceLowCheckServiceTest {

    private final BalanceLowCheckService service = new BalanceLowCheckService();

    @Test
    void belowThreshold_returnsTrue() {
        assertThat(service.isBalanceLow(new BigDecimal("50.00"), new BigDecimal("100.00"))).isTrue();
    }

    @Test
    void atThreshold_returnsTrue() {
        assertThat(service.isBalanceLow(new BigDecimal("100.00"), new BigDecimal("100.00"))).isTrue();
    }

    @Test
    void aboveThreshold_returnsFalse() {
        assertThat(service.isBalanceLow(new BigDecimal("100.01"), new BigDecimal("100.00"))).isFalse();
    }

    @Test
    void zeroBalance_belowPositiveThreshold_returnsTrue() {
        assertThat(service.isBalanceLow(BigDecimal.ZERO, new BigDecimal("100.00"))).isTrue();
    }
}
