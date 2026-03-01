package com.sniplink.controller;

import com.sniplink.dto.ApiResponse;
import com.sniplink.dto.DashboardStats;
import com.sniplink.entity.ClickLog;
import com.sniplink.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse> getDashboardStats() {
        DashboardStats stats = analyticsService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.success("Dashboard stats retrieved successfully", stats));
    }

    @GetMapping("/url/{id}/clicks")
    public ResponseEntity<ApiResponse> getURLClickLogs(@PathVariable Long id) {
        List<ClickLog> clickLogs = analyticsService.getURLClickLogs(id);
        return ResponseEntity.ok(ApiResponse.success("Click logs retrieved successfully", clickLogs));
    }
}
