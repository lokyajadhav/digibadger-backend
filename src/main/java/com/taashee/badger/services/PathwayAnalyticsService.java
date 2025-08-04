package com.taashee.badger.services;

import java.util.Map;

public interface PathwayAnalyticsService {
    
    // Get pathway overview analytics
    Map<String, Object> getPathwayOverview(Long pathwayId, String userEmail);
    
    // Get enrollment trends
    Map<String, Object> getEnrollmentTrends(Long pathwayId, String period, String userEmail);
    
    // Get completion analytics
    Map<String, Object> getCompletionAnalytics(Long pathwayId, String userEmail);
    
    // Get element performance analytics
    Map<String, Object> getElementPerformance(Long pathwayId, String userEmail);
    
    // Get user engagement analytics
    Map<String, Object> getUserEngagement(Long pathwayId, String userEmail);
    
    // Get organization-wide pathway analytics
    Map<String, Object> getOrganizationPathwayAnalytics(Long organizationId, String userEmail);
    
    // Export pathway report
    String exportPathwayReport(Long pathwayId, String format, String userEmail);
    
    // Get real-time metrics
    Map<String, Object> getRealTimeMetrics(Long pathwayId, String userEmail);
} 