package com.fernandez.backend.application.port.in;

public interface IIpLockService {
    boolean isIpBlocked(String ip);
    void registerFailedAttempt(String ip);
}

