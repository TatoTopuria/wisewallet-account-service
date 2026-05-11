package com.wisewallet.account.domain.repository;

import com.wisewallet.account.domain.model.EmailVerificationToken;

import java.util.Optional;
import java.util.UUID;

public interface EmailVerificationTokenRepositoryPort {
    EmailVerificationToken save(EmailVerificationToken token);
    Optional<EmailVerificationToken> findByToken(String token);
    void delete(EmailVerificationToken token);
}
