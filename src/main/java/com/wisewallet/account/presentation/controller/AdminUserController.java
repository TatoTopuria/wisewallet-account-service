package com.wisewallet.account.presentation.controller;

import com.wisewallet.account.application.query.AdminQueryService;
import com.wisewallet.account.presentation.dto.response.AdminUserResponse;
import com.wisewallet.account.presentation.mapper.AccountMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminQueryService adminQueryService;
    private final AccountMapper accountMapper;

    @GetMapping("/users")
    public Page<AdminUserResponse> listUsers(@PageableDefault(size = 20) Pageable pageable) {
        return adminQueryService.findAllUsers(pageable).map(accountMapper::toAdminUserResponse);
    }
}
