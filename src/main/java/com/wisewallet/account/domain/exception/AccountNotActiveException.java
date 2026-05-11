package com.wisewallet.account.domain.exception;

import java.util.UUID;

public class AccountNotActiveException extends RuntimeException {
    public AccountNotActiveException(UUID accountId) {
        super("Account is not active: " + accountId);
    }

    public AccountNotActiveException(String message) {
        super(message);
    }
}
