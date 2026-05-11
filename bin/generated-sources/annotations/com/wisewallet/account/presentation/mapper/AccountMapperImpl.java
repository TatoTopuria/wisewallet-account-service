package com.wisewallet.account.presentation.mapper;

import com.wisewallet.account.domain.model.Account;
import com.wisewallet.account.domain.model.AccountBalance;
import com.wisewallet.account.domain.model.AccountStatus;
import com.wisewallet.account.domain.model.AccountType;
import com.wisewallet.account.domain.model.User;
import com.wisewallet.account.domain.model.UserStatus;
import com.wisewallet.account.presentation.dto.response.AccountResponse;
import com.wisewallet.account.presentation.dto.response.AdminUserResponse;
import com.wisewallet.account.presentation.dto.response.BalanceResponse;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-11T15:48:10+0400",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.46.0.v20260407-0427, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class AccountMapperImpl implements AccountMapper {

    @Override
    public AccountResponse toAccountResponse(Account account, List<AccountBalance> balances) {
        if ( account == null && balances == null ) {
            return null;
        }

        UUID id = null;
        String nickname = null;
        AccountType type = null;
        AccountStatus status = null;
        Instant createdAt = null;
        if ( account != null ) {
            id = account.getId();
            nickname = account.getNickname();
            type = account.getType();
            status = account.getStatus();
            createdAt = account.getCreatedAt();
        }
        List<BalanceResponse> balances1 = null;
        balances1 = accountBalanceListToBalanceResponseList( balances );

        AccountResponse accountResponse = new AccountResponse( id, nickname, type, status, balances1, createdAt );

        return accountResponse;
    }

    @Override
    public BalanceResponse toBalanceResponse(AccountBalance balance) {
        if ( balance == null ) {
            return null;
        }

        String currency = null;
        BigDecimal amount = null;
        BigDecimal reservedAmount = null;

        currency = balance.getCurrency();
        amount = balance.getAmount();
        reservedAmount = balance.getReservedAmount();

        BigDecimal availableBalance = balance.availableBalance();

        BalanceResponse balanceResponse = new BalanceResponse( currency, amount, reservedAmount, availableBalance );

        return balanceResponse;
    }

    @Override
    public AdminUserResponse toAdminUserResponse(User user) {
        if ( user == null ) {
            return null;
        }

        UUID id = null;
        String email = null;
        String firstName = null;
        String lastName = null;
        UserStatus status = null;
        Instant createdAt = null;

        id = user.getId();
        email = user.getEmail();
        firstName = user.getFirstName();
        lastName = user.getLastName();
        status = user.getStatus();
        createdAt = user.getCreatedAt();

        List<String> roles = new java.util.ArrayList<>(user.getRoles());

        AdminUserResponse adminUserResponse = new AdminUserResponse( id, email, firstName, lastName, status, roles, createdAt );

        return adminUserResponse;
    }

    protected List<BalanceResponse> accountBalanceListToBalanceResponseList(List<AccountBalance> list) {
        if ( list == null ) {
            return null;
        }

        List<BalanceResponse> list1 = new ArrayList<BalanceResponse>( list.size() );
        for ( AccountBalance accountBalance : list ) {
            list1.add( toBalanceResponse( accountBalance ) );
        }

        return list1;
    }
}
