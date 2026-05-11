package com.wisewallet.account.infrastructure.config;

import com.wisewallet.account.domain.service.BalanceLowCheckService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainServiceConfig {

    @Bean
    public BalanceLowCheckService balanceLowCheckService() {
        return new BalanceLowCheckService();
    }
}
