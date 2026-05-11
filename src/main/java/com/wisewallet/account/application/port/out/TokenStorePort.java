package com.wisewallet.account.application.port.out;

public interface TokenStorePort {
    void blockJti(String jti, long ttlSeconds);
    boolean isBlocked(String jti);
}
