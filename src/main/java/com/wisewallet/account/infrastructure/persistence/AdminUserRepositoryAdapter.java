package com.wisewallet.account.infrastructure.persistence;

import com.wisewallet.account.application.port.out.AdminUserRepositoryPort;
import com.wisewallet.account.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AdminUserRepositoryAdapter implements AdminUserRepositoryPort {

    private final UserJpaRepository jpa;

    @Override
    public Page<User> findAll(Pageable pageable) {
        return jpa.findAll(pageable);
    }
}
