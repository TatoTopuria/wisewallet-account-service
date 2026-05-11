package com.wisewallet.account.infrastructure.persistence;

import com.wisewallet.account.domain.model.EmailVerificationToken;
import com.wisewallet.account.domain.repository.EmailVerificationTokenRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class EmailVerificationTokenRepositoryAdapter implements EmailVerificationTokenRepositoryPort {

    private final EmailVerificationTokenJpaRepository jpa;

    @Override
    public EmailVerificationToken save(EmailVerificationToken token) {
        return jpa.save(token);
    }

    @Override
    public Optional<EmailVerificationToken> findByToken(String token) {
        return jpa.findByToken(token);
    }

    @Override
    public void delete(EmailVerificationToken token) {
        jpa.delete(token);
    }
}
