package com.security.microservice.scheduler;

import com.security.microservice.repository.PendingUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class PendingUserCleanupScheduler {

    private final PendingUserRepository pendingUserRepository;

    @Scheduled(fixedRate = 30 * 60 * 1000)
    @Transactional
    public void cleanupExpiredPendingUsers() {
        log.info("Running scheduled cleanup for expired pending registrations");
        int deletedCount = pendingUserRepository.deleteExpiredPendingUsers(LocalDateTime.now());
        log.info("Cleaned up {} expired pending registration(s)", deletedCount);
    }

}
