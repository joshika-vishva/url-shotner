package com.sniplink.service;

import com.sniplink.dto.ShortenRequest;
import com.sniplink.dto.URLResponse;
import com.sniplink.entity.URL;
import com.sniplink.entity.User;
import com.sniplink.exception.DuplicateResourceException;
import com.sniplink.exception.InvalidRequestException;
import com.sniplink.exception.ResourceNotFoundException;
import com.sniplink.repository.URLRepository;
import com.sniplink.repository.UserRepository;
import com.sniplink.util.ShortCodeGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class URLService {

    @Autowired
    private URLRepository urlRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AIService aiService;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Transactional
    public URLResponse shortenURL(ShortenRequest request) {
        User user = null;
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !authentication.getPrincipal().equals("anonymousUser")) {
            String username = authentication.getName();
            user = userRepository.findByUsername(username).orElse(null);
        }

        // Generate or use custom slug
        String shortCode;
        if (request.getCustomSlug() != null && !request.getCustomSlug().isEmpty()) {
            if (!ShortCodeGenerator.isValidCustomSlug(request.getCustomSlug())) {
                throw new InvalidRequestException("Custom slug must be 3-20 characters and contain only letters, numbers, hyphens, and underscores");
            }
            if (urlRepository.existsByShortCode(request.getCustomSlug())) {
                throw new DuplicateResourceException("Custom slug already exists");
            }
            shortCode = request.getCustomSlug();
        } else {
            shortCode = generateUniqueShortCode();
        }

        // Create URL entity
        URL url = new URL();
        url.setOriginalUrl(request.getUrl());
        url.setShortCode(shortCode);
        url.setExpiresAt(request.getExpiresAt());
        url.setTags(request.getTags());
        url.setUser(user);

        // Set password if provided
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            url.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        // AI Safety Check
        boolean isSafe = aiService.checkURLSafety(request.getUrl());
        url.setIsSafe(isSafe);

        urlRepository.save(url);

        return mapToResponse(url);
    }

    public URLResponse getURLById(Long id) {
        URL url = urlRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("URL not found"));

        if (url.getUser() != null) {
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
                throw new InvalidRequestException("You don't have permission to access this URL");
            }
            User user = userRepository.findByUsername(authentication.getName())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            
            if (!url.getUser().getId().equals(user.getId())) {
                throw new InvalidRequestException("You don't have permission to access this URL");
            }
        }

        return mapToResponse(url);
    }

    public List<URLResponse> getUserURLs() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<URL> urls = urlRepository.findByUserOrderByCreatedAtDesc(user);
        return urls.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional
    public URLResponse updateURL(Long id, ShortenRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        URL url = urlRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("URL not found"));

        if (!url.getUser().getId().equals(user.getId())) {
            throw new InvalidRequestException("You don't have permission to update this URL");
        }

        url.setExpiresAt(request.getExpiresAt());
        url.setTags(request.getTags());
        url.setIsActive(true);

        urlRepository.save(url);
        return mapToResponse(url);
    }

    @Transactional
    public void deleteURL(Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        URL url = urlRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("URL not found"));

        if (!url.getUser().getId().equals(user.getId())) {
            throw new InvalidRequestException("You don't have permission to delete this URL");
        }

        urlRepository.delete(url);
    }

    @Transactional
    public void toggleURL(Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        URL url = urlRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("URL not found"));

        if (!url.getUser().getId().equals(user.getId())) {
            throw new InvalidRequestException("You don't have permission to toggle this URL");
        }

        url.setIsActive(!url.getIsActive());
        urlRepository.save(url);
    }

    private String generateUniqueShortCode() {
        String shortCode;
        do {
            shortCode = ShortCodeGenerator.generate();
        } while (urlRepository.existsByShortCode(shortCode));
        return shortCode;
    }

    private URLResponse mapToResponse(URL url) {
        return URLResponse.builder()
                .id(url.getId())
                .originalUrl(url.getOriginalUrl())
                .shortCode(url.getShortCode())
                .shortUrl(baseUrl + "/" + url.getShortCode())
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
