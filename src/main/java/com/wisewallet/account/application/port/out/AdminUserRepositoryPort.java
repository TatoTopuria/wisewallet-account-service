package com.wisewallet.account.application.port.out;

import com.wisewallet.account.domain.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminUserRepositoryPort {
    Page<User> findAll(Pageable pageable);
}
