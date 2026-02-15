package com.fernandez.backend.service;

public interface IIpLockService {
    boolean isIpBlocked(String ip);
    void registerFailedAttempt(String ip);
}

