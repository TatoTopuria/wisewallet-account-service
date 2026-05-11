package com.wisewallet.account.presentation.controller;

import com.wisewallet.account.application.query.AccountQueryService;
import com.wisewallet.account.domain.model.User;
import com.wisewallet.account.presentation.dto.response.InternalUserInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
public class InternalUserController {

    private final AccountQueryService accountQueryService;

    @GetMapping("/{userId}")
    public InternalUserInfoResponse getUserInfo(@PathVariable UUID userId) {
        User user = accountQueryService.getUserInfo(userId);
        return new InternalUserInfoResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName()
        );
    }
}
