package com.wisewallet.account.infrastructure.email;

import com.wisewallet.account.application.port.out.EmailPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Email adapter — in local/dev profile, logs the verification link to console.
 * In production, swap this bean with an SMTP-backed implementation.
 */
@Component
@Slf4j
public class MailSenderEmailAdapter implements EmailPort {

    @Override
    public void sendVerification(String toEmail, String token, String userName) {
        log.info("[EMAIL] Verification link for user={} to={}: /api/auth/verify?token={}",
                userName, toEmail, token);
    }
}
