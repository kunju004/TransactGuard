package com.transactguard.transaction.config;

import com.transactguard.transaction.domain.Account;
import com.transactguard.transaction.repository.AccountRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
public class DemoDataConfig {

    @Bean
    @ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true", matchIfMissing = true)
    CommandLineRunner seedDemoAccounts(AccountRepository accountRepository) {
        return args -> {
            if (accountRepository.count() > 0) {
                return;
            }

            accountRepository.save(new Account("acct_market_001", "USD", new BigDecimal("25000.00"), true));
            accountRepository.save(new Account("acct_starter_002", "USD", new BigDecimal("750.00"), true));
            accountRepository.save(new Account("acct_review_003", "USD", new BigDecimal("5000.00"), false));
        };
    }
}
