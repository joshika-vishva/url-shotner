package com.sniplink.service;

import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class AIService {

    // Suspicious TLDs
    private static final List<String> SUSPICIOUS_TLDS = Arrays.asList(
            ".tk", ".ml", ".ga", ".cf", ".gq", ".xyz", ".top", ".work", ".click"
    );

    // Malicious keywords
    private static final List<String> MALICIOUS_KEYWORDS = Arrays.asList(
            "phishing", "malware", "virus", "hack", "scam", "fraud", "fake",
            "password-reset", "account-verify", "login-verify", "secure-update"
    );

    // Suspicious patterns
    private static final Pattern IP_PATTERN = Pattern.compile("^https?://\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}");
    private static final Pattern EXCESSIVE_SUBDOMAIN = Pattern.compile("([a-zA-Z0-9-]+\\.){4,}");

    /**
     * Basic AI safety check for malicious URLs
     * Returns true if URL appears safe, false if potentially malicious
     */
    public boolean checkURLSafety(String url) {
        if (url == null || url.isEmpty()) {
            return false;
        }

        String lowerUrl = url.toLowerCase();

        // Check for suspicious TLDs
        for (String tld : SUSPICIOUS_TLDS) {
            if (lowerUrl.contains(tld)) {
                return false;
            }
        }

        // Check for malicious keywords
        for (String keyword : MALICIOUS_KEYWORDS) {
            if (lowerUrl.contains(keyword)) {
                return false;
            }
        }

        // Check for IP address URLs (often suspicious)
        if (IP_PATTERN.matcher(url).find()) {
            return false;
        }

        // Check for excessive subdomains (potential phishing)
        if (EXCESSIVE_SUBDOMAIN.matcher(url).find()) {
            return false;
        }

        // Check for homograph attacks (Unicode characters that look like ASCII)
        if (!url.matches("^[\\x00-\\x7F]*$")) {
            return false;
        }

        // Additional checks for URL shorteners (to prevent chaining)
        List<String> shortenerDomains = Arrays.asList(
                "bit.ly", "tinyurl", "goo.gl", "ow.ly", "t.co"
        );
        for (String domain : shortenerDomains) {
            if (lowerUrl.contains(domain)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Get safety status description
     */
    public String getSafetyDescription(boolean isSafe) {
        return isSafe ? "URL appears safe" : "URL flagged as potentially unsafe";
    }
}
