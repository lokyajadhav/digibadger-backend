package com.taashee.badger.services;

import com.taashee.badger.models.*;
import com.taashee.badger.repositories.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class AuditLoggingService {
    
    @Autowired
    private AuditLogRepository auditLogRepository;
    
    /**
     * Logs a pathway-related action
     */
    public AuditLog logPathwayAction(String action, Pathway pathway, User user, String description) {
        return logPathwayAction(action, pathway, user, description, null);
    }
    
    /**
     * Logs a pathway-related action with additional context
     */
    public AuditLog logPathwayAction(String action, Pathway pathway, User user, String description, HttpServletRequest request) {
        AuditLog auditLog = AuditLog.createPathwayLog(action, pathway, user, description);
        
        if (request != null) {
            auditLog.setIpAddress(getClientIpAddress(request));
            auditLog.setUserAgent(request.getHeader("User-Agent"));
        }
        
        return auditLogRepository.save(auditLog);
    }
    
    /**
     * Logs a step-related action
     */
    public AuditLog logStepAction(String action, PathwayStep step, User user, String description) {
        return logStepAction(action, step, user, description, null);
    }
    
    /**
     * Logs a step-related action with additional context
     */
    public AuditLog logStepAction(String action, PathwayStep step, User user, String description, HttpServletRequest request) {
        AuditLog auditLog = AuditLog.createStepLog(action, step, user, description);
        
        if (request != null) {
            auditLog.setIpAddress(getClientIpAddress(request));
            auditLog.setUserAgent(request.getHeader("User-Agent"));
        }
        
        return auditLogRepository.save(auditLog);
    }
    
    /**
     * Logs a requirement-related action
     */
    public AuditLog logRequirementAction(String action, StepRequirement requirement, User user, String description) {
        return logRequirementAction(action, requirement, user, description, null);
    }
    
    /**
     * Logs a requirement-related action with additional context
     */
    public AuditLog logRequirementAction(String action, StepRequirement requirement, User user, String description, HttpServletRequest request) {
        AuditLog auditLog = AuditLog.createRequirementLog(action, requirement, user, description);
        
        if (request != null) {
            auditLog.setIpAddress(getClientIpAddress(request));
            auditLog.setUserAgent(request.getHeader("User-Agent"));
        }
        
        return auditLogRepository.save(auditLog);
    }
    
    /**
     * Logs a custom action
     */
    public AuditLog logCustomAction(AuditLog.AuditAction action, String entityType, Long entityId, 
                                  String entityName, String description, User user, Pathway pathway) {
        return logCustomAction(action, entityType, entityId, entityName, description, user, pathway, null);
    }
    
    /**
     * Logs a custom action with additional context
     */
    public AuditLog logCustomAction(AuditLog.AuditAction action, String entityType, Long entityId, 
                                  String entityName, String description, User user, Pathway pathway, HttpServletRequest request) {
        AuditLog auditLog = new AuditLog(action, entityType, entityId, entityName, description, user);
        auditLog.setPathway(pathway);
        
        if (request != null) {
            auditLog.setIpAddress(getClientIpAddress(request));
            auditLog.setUserAgent(request.getHeader("User-Agent"));
        }
        
        return auditLogRepository.save(auditLog);
    }
    
    /**
     * Gets audit logs for a pathway
     */
    public List<AuditLog> getPathwayAuditLogs(Long pathwayId) {
        Pathway pathway = new Pathway();
        pathway.setId(pathwayId);
        return auditLogRepository.findByPathwayOrderByTimestampDesc(pathway);
    }
    
    /**
     * Gets audit logs for a specific entity
     */
    public List<AuditLog> getEntityAuditLogs(String entityType, Long entityId) {
        return auditLogRepository.findByEntityTypeAndEntityIdOrderByTimestampDesc(entityType, entityId);
    }
    
    /**
     * Gets audit logs for a pathway since a specific time
     */
    public List<AuditLog> getPathwayAuditLogsSince(Long pathwayId, LocalDateTime since) {
        Pathway pathway = new Pathway();
        pathway.setId(pathwayId);
        return auditLogRepository.findByPathwayAndTimestampAfter(pathway, since);
    }
    
    /**
     * Gets audit logs for a pathway by action type
     */
    public List<AuditLog> getPathwayAuditLogsByAction(Long pathwayId, AuditLog.AuditAction action) {
        Pathway pathway = new Pathway();
        pathway.setId(pathwayId);
        return auditLogRepository.findByPathwayAndAction(pathway, action);
    }
    
    /**
     * Extracts client IP address from request
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}
