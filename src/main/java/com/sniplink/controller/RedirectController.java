package com.sniplink.controller;

import com.sniplink.entity.URL;
import com.sniplink.exception.InvalidRequestException;
import com.sniplink.exception.ResourceNotFoundException;
import com.sniplink.repository.URLRepository;
import com.sniplink.service.AnalyticsService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.crypto.password.PasswordEncoder;

@Controller
public class RedirectController {

    @Autowired
    private URLRepository urlRepository;

    @Autowired
    private AnalyticsService analyticsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/")
    public String index() {
        return "forward:/index.html";
    }

    @GetMapping("/{shortCode:[a-zA-Z0-9_-]+}")
    public String redirect(@PathVariable String shortCode,
                          @RequestParam(required = false) String password,
                          HttpServletRequest request) {
        
        URL url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new ResourceNotFoundException("Short URL not found"));

        // Check if URL is active
        if (!url.getIsActive()) {
            throw new InvalidRequestException("This URL has been disabled");
        }

        // Check if URL is expired
        if (url.isExpired()) {
            throw new InvalidRequestException("This URL has expired");
        }

        // Check if URL is safe
        if (!url.getIsSafe()) {
            throw new InvalidRequestException("This URL has been flagged as unsafe");
        }

        // Check if password protected
        if (url.isPasswordProtected()) {
            if (password == null || password.isEmpty()) {
                // Redirect to password prompt page
                return "redirect:/password.html?c=" + shortCode;
            }
            // Verify password
            if (!passwordEncoder.matches(password, url.getPassword())) {
                return "redirect:/password.html?c=" + shortCode + "&error=invalid";
            }
        }

        // Log click analytics
        String ipAddress = getClientIP(request);
        String userAgent = request.getHeader("User-Agent");
        analyticsService.logClick(url, ipAddress, userAgent);

        // Redirect to original URL
        return "redirect:" + url.getOriginalUrl();
    }

    private String getClientIP(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
