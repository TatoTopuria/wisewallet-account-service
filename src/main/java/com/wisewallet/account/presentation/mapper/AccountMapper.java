package com.wisewallet.account.presentation.mapper;

import com.wisewallet.account.domain.model.Account;
import com.wisewallet.account.domain.model.AccountBalance;
import com.wisewallet.account.domain.model.User;
import com.wisewallet.account.presentation.dto.response.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AccountMapper {

    @Mapping(target = "id", source = "account.id")
    @Mapping(target = "nickname", source = "account.nickname")
    @Mapping(target = "type", source = "account.type")
    @Mapping(target = "status", source = "account.status")
    @Mapping(target = "createdAt", source = "account.createdAt")
    @Mapping(target = "balances", source = "balances")
    AccountResponse toAccountResponse(Account account, List<AccountBalance> balances);

    @Mapping(target = "availableBalance", expression = "java(balance.availableBalance())")
    BalanceResponse toBalanceResponse(AccountBalance balance);

    @Mapping(target = "roles", expression = "java(new java.util.ArrayList<>(user.getRoles()))")
    AdminUserResponse toAdminUserResponse(User user);

    default InternalBalanceResponse toInternalBalanceResponse(AccountBalance balance) {
        return new InternalBalanceResponse(
                balance.getAccount().getId(),
                balance.getCurrency(),
                balance.getAmount(),
                balance.getReservedAmount(),
                balance.availableBalance()
        );
    }
}
