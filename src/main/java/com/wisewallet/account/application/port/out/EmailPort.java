package com.wisewallet.account.application.port.out;

public interface EmailPort {
    void sendVerification(String toEmail, String token, String userName);
}
