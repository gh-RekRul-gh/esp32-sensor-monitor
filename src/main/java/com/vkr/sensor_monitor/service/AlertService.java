package com.vkr.sensor_monitor.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    @Value("${alert.recipient-email}")
    private String recipient;

    @Value("${alert.cooldown-minutes:10}")
    private int cooldownMinutes;

    private final ConcurrentMap<String, LocalDateTime> lastAlertTime = new ConcurrentHashMap<>();

    public void sendIfCooldownPassed(String alertKey, String subject, String body) {
        LocalDateTime last = lastAlertTime.get(alertKey);
        if (last != null && last.plusMinutes(cooldownMinutes).isAfter(LocalDateTime.now())) {
            log.debug("Alert '{}' suppressed by cooldown", alertKey);
            return;
        }
        lastAlertTime.put(alertKey, LocalDateTime.now());
        send(subject, body);
    }

    @Async
    public void send(String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(recipient);
            message.setSubject("[Sensor Monitor] " + subject);
            message.setText(body);
            mailSender.send(message);
            log.info("Alert sent: {}", subject);
        } catch (Exception e) {
            log.error("Failed to send alert '{}': {}", subject, e.getMessage());
        }
    }
}
