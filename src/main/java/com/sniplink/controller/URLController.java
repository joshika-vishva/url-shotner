package com.sniplink.controller;

import com.sniplink.dto.ApiResponse;
import com.sniplink.dto.ShortenRequest;
import com.sniplink.dto.URLResponse;
import com.sniplink.service.URLService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class URLController {

    @Autowired
    private URLService urlService;

    @PostMapping("/shorten")
    public ResponseEntity<ApiResponse> shortenURL(@Valid @RequestBody ShortenRequest request) {
        URLResponse urlResponse = urlService.shortenURL(request);
        return ResponseEntity.ok(ApiResponse.success("URL shortened successfully", urlResponse));
    }

    @GetMapping("/user/urls")
    public ResponseEntity<ApiResponse> getUserURLs() {
        List<URLResponse> urls = urlService.getUserURLs();
        return ResponseEntity.ok(ApiResponse.success("URLs retrieved successfully", urls));
    }

    @GetMapping("/url/{id}")
    public ResponseEntity<ApiResponse> getURL(@PathVariable Long id) {
        URLResponse url = urlService.getURLById(id);
        return ResponseEntity.ok(ApiResponse.success("URL retrieved successfully", url));
    }

    @PutMapping("/url/{id}")
    public ResponseEntity<ApiResponse> updateURL(@PathVariable Long id, 
                                                  @Valid @RequestBody ShortenRequest request) {
        URLResponse url = urlService.updateURL(id, request);
        return ResponseEntity.ok(ApiResponse.success("URL updated successfully", url));
    }

    @DeleteMapping("/url/{id}")
    public ResponseEntity<ApiResponse> deleteURL(@PathVariable Long id) {
        urlService.deleteURL(id);
        return ResponseEntity.ok(ApiResponse.success("URL deleted successfully", null));
    }

    @PostMapping("/url/{id}/toggle")
    public ResponseEntity<ApiResponse> toggleURL(@PathVariable Long id) {
        urlService.toggleURL(id);
        return ResponseEntity.ok(ApiResponse.success("URL status toggled successfully", null));
    }
}
