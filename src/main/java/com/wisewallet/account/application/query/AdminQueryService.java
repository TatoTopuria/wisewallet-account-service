package com.wisewallet.account.application.query;

import com.wisewallet.account.application.port.out.AdminUserRepositoryPort;
import com.wisewallet.account.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminQueryService {

    private final AdminUserRepositoryPort adminUserRepository;

    @Transactional(readOnly = true)
    public Page<User> findAllUsers(Pageable pageable) {
        return adminUserRepository.findAll(pageable);
    }
}
