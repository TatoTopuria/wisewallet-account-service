package com.wisewallet.account.domain.service;

import java.math.BigDecimal;

/**
 * Pure domain service: checks whether available balance is at or below the threshold.
 * No framework dependencies.
 */
public class BalanceLowCheckService {

    public boolean isBalanceLow(BigDecimal availableBalance, BigDecimal threshold) {
        return availableBalance.compareTo(threshold) <= 0;
    }
}
