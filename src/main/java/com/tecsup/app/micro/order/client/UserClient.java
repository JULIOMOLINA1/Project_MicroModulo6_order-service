package com.tecsup.app.micro.order.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@RequiredArgsConstructor
@Component
public class UserClient {

    private final RestTemplate restTemplate;

    @Value("${user.service.url}")
    private String userServiceUrl;

    @CircuitBreaker(name = "userService",
            fallbackMethod = "getUserByIdFallback")
    public User getUserById(Long userId) {
        String url = userServiceUrl + "/api/users/" + userId;
        log.info("Calling User Service at: {}", url);

        try {
            User usr = restTemplate.getForObject(url, User.class);
            if (usr == null) {
                log.error("User Service returned null for user id: {}", userId);
                throw new RuntimeException("User not found with id: " + userId);
            }
            log.info("User retrieved successfully: {}", usr);
            return usr;
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            log.error("HTTP error calling User Service for id {}: Status {} - {}", userId, e.getStatusCode(), e.getMessage());
            throw new RuntimeException("User not found with id: " + userId, e);
        } catch (Exception e) {
            log.error("Unexpected error calling User Service for id {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("Error calling User Service: " + e.getMessage(), e);
        }
    }

    private User getUserByIdFallback(Long createdBy, Throwable throwable) {
        log.warn("Fallback method invoked for getUserById due to: {}", throwable.getMessage());
        return User.builder()
                .id(createdBy)
                .name("Unknown User")
                .email("Unknown Email")
                .build();
    }
}
