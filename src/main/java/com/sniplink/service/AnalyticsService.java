package com.sniplink.service;

import com.sniplink.dto.DashboardStats;
import com.sniplink.dto.URLResponse;
import com.sniplink.entity.ClickLog;
import com.sniplink.entity.URL;
import com.sniplink.entity.User;
import com.sniplink.exception.ResourceNotFoundException;
import com.sniplink.repository.ClickLogRepository;
import com.sniplink.repository.URLRepository;
import com.sniplink.repository.UserRepository;
import com.sniplink.util.UserAgentParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    @Autowired
    private URLRepository urlRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClickLogRepository clickLogRepository;

    @Value("${server.port:8080}")
    private String serverPort;

    @Transactional
    public void logClick(URL url, String ipAddress, String userAgent) {
        ClickLog clickLog = new ClickLog();
        clickLog.setUrl(url);
        clickLog.setIpAddress(ipAddress);
        clickLog.setDevice(UserAgentParser.getDevice(userAgent));
        clickLog.setBrowser(UserAgentParser.getBrowser(userAgent));
        clickLog.setLocation("Unknown"); // Can integrate with IP geolocation service

        clickLogRepository.save(clickLog);

        // Increment click count
        url.incrementClickCount();
        urlRepository.save(url);
    }

    public DashboardStats getDashboardStats() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Long totalLinks = urlRepository.countByUser(user);
        Long totalClicks = urlRepository.getTotalClicksByUser(user);
        if (totalClicks == null) totalClicks = 0L;

        Long activeLinks = urlRepository.findByUser(user).stream()
                .filter(URL::getIsActive)
                .count();

        List<URLResponse> topLinks = urlRepository.findTopUrlsByUser(user).stream()
                .limit(5)
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        Map<String, Long> dailyClicks = getDailyClicks(user.getId());

        return DashboardStats.builder()
                .totalLinks(totalLinks)
                .totalClicks(totalClicks)
                .activeLinks(activeLinks)
                .topLinks(topLinks)
                .dailyClicks(dailyClicks)
                .build();
    }

    private Map<String, Long> getDailyClicks(Long userId) {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        LocalDateTime now = LocalDateTime.now();

        List<ClickLog> clicks = clickLogRepository.findClicksByUserAndDateRange(userId, sevenDaysAgo, now);

        Map<String, Long> dailyClicks = new LinkedHashMap<>();
        
        for (int i = 6; i >= 0; i--) {
            LocalDateTime date = LocalDateTime.now().minusDays(i);
            String dateKey = date.toLocalDate().toString();
            dailyClicks.put(dateKey, 0L);
        }

        clicks.forEach(click -> {
            String dateKey = click.getTimestamp().toLocalDate().toString();
            dailyClicks.put(dateKey, dailyClicks.getOrDefault(dateKey, 0L) + 1);
        });

        return dailyClicks;
    }

    public List<ClickLog> getURLClickLogs(Long urlId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        URL url = urlRepository.findById(urlId)
                .orElseThrow(() -> new ResourceNotFoundException("URL not found"));

        if (!url.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("URL not found");
        }

        return clickLogRepository.findByUrlOrderByTimestampDesc(url);
    }

    private URLResponse mapToResponse(URL url) {
        return URLResponse.builder()
                .id(url.getId())
                .originalUrl(url.getOriginalUrl())
                .shortCode(url.getShortCode())
                .shortUrl("http://localhost:" + serverPort + "/" + url.getShortCode())
                .createdAt(url.getCreatedAt())
                .expiresAt(url.getExpiresAt())
                .clickCount(url.getClickCount())
                .tags(url.getTags())
                .isActive(url.getIsActive())
                .isSafe(url.getIsSafe())
                .isPasswordProtected(url.isPasswordProtected())
                .build();
    }
}
