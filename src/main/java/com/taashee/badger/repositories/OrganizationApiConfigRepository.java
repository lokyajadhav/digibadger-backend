package com.taashee.badger.repositories;

import com.taashee.badger.models.OrganizationApiConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrganizationApiConfigRepository extends JpaRepository<OrganizationApiConfig, Long> {
    
    // Find API configuration by organization ID
    @Query("SELECT oac FROM OrganizationApiConfig oac WHERE oac.organization.id = :organizationId")
    List<OrganizationApiConfig> findByOrganizationId(@Param("organizationId") Long organizationId);
    
    // Find active API configuration by organization ID
    @Query("SELECT oac FROM OrganizationApiConfig oac WHERE oac.organization.id = :organizationId AND oac.isActive = true")
    List<OrganizationApiConfig> findActiveByOrganizationId(@Param("organizationId") Long organizationId);
    
    // Find verified API configuration by organization ID
    @Query("SELECT oac FROM OrganizationApiConfig oac WHERE oac.organization.id = :organizationId AND oac.isVerified = true")
    List<OrganizationApiConfig> findVerifiedByOrganizationId(@Param("organizationId") Long organizationId);
    
    // Find API configuration by organization ID and API type
    @Query("SELECT oac FROM OrganizationApiConfig oac WHERE oac.organization.id = :organizationId AND oac.apiType = :apiType")
    List<OrganizationApiConfig> findByOrganizationIdAndApiType(@Param("organizationId") Long organizationId, @Param("apiType") OrganizationApiConfig.ApiType apiType);
    
    // Find API configuration by organization ID and API name
    @Query("SELECT oac FROM OrganizationApiConfig oac WHERE oac.organization.id = :organizationId AND oac.apiName = :apiName")
    Optional<OrganizationApiConfig> findByOrganizationIdAndApiName(@Param("organizationId") Long organizationId, @Param("apiName") String apiName);
    
    // Find healthy API configurations
    @Query("SELECT oac FROM OrganizationApiConfig oac WHERE oac.isActive = true AND oac.isVerified = true AND oac.errorCount < 5")
    List<OrganizationApiConfig> findHealthyConfigurations();
    
    // Find API configurations that need token refresh
    @Query("SELECT oac FROM OrganizationApiConfig oac WHERE oac.tokenExpiresAt IS NOT NULL AND oac.tokenExpiresAt <= :expiryTime")
    List<OrganizationApiConfig> findConfigurationsNeedingTokenRefresh(@Param("expiryTime") java.time.LocalDateTime expiryTime);
    
    // Find API configurations that need sync
    @Query("SELECT oac FROM OrganizationApiConfig oac WHERE oac.isActive = true AND oac.isVerified = true AND (oac.lastSyncAt IS NULL OR oac.lastSyncAt <= :syncTime)")
    List<OrganizationApiConfig> findConfigurationsNeedingSync(@Param("syncTime") java.time.LocalDateTime syncTime);
    
    // Find API configurations with errors
    @Query("SELECT oac FROM OrganizationApiConfig oac WHERE oac.errorCount > 0 ORDER BY oac.lastErrorAt DESC")
    List<OrganizationApiConfig> findConfigurationsWithErrors();
    
    // Count API configurations by organization ID
    @Query("SELECT COUNT(oac) FROM OrganizationApiConfig oac WHERE oac.organization.id = :organizationId")
    Long countByOrganizationId(@Param("organizationId") Long organizationId);
    
    // Count active API configurations by organization ID
    @Query("SELECT COUNT(oac) FROM OrganizationApiConfig oac WHERE oac.organization.id = :organizationId AND oac.isActive = true")
    Long countActiveByOrganizationId(@Param("organizationId") Long organizationId);
    
    // Count verified API configurations by organization ID
    @Query("SELECT COUNT(oac) FROM OrganizationApiConfig oac WHERE oac.organization.id = :organizationId AND oac.isVerified = true")
    Long countVerifiedByOrganizationId(@Param("organizationId") Long organizationId);
    
    // Find API configurations by API type
    @Query("SELECT oac FROM OrganizationApiConfig oac WHERE oac.apiType = :apiType")
    List<OrganizationApiConfig> findByApiType(@Param("apiType") OrganizationApiConfig.ApiType apiType);
    
    // Find API configurations created by a specific user
    @Query("SELECT oac FROM OrganizationApiConfig oac WHERE oac.createdBy.email = :userEmail")
    List<OrganizationApiConfig> findByCreatedByEmail(@Param("userEmail") String userEmail);
    
    // Find API configurations updated by a specific user
    @Query("SELECT oac FROM OrganizationApiConfig oac WHERE oac.updatedBy.email = :userEmail")
    List<OrganizationApiConfig> findByUpdatedByEmail(@Param("userEmail") String userEmail);
    
    // Find API configurations with webhook URLs
    @Query("SELECT oac FROM OrganizationApiConfig oac WHERE oac.webhookUrl IS NOT NULL AND oac.webhookUrl != ''")
    List<OrganizationApiConfig> findWithWebhooks();
    
    // Find API configurations by sync frequency
    @Query("SELECT oac FROM OrganizationApiConfig oac WHERE oac.syncFrequencyMinutes = :frequencyMinutes")
    List<OrganizationApiConfig> findBySyncFrequency(@Param("frequencyMinutes") Integer frequencyMinutes);
    
    // Find API configurations that haven't synced recently
    @Query("SELECT oac FROM OrganizationApiConfig oac WHERE oac.lastSyncAt IS NULL OR oac.lastSyncAt <= :lastSyncTime")
    List<OrganizationApiConfig> findNotRecentlySynced(@Param("lastSyncTime") java.time.LocalDateTime lastSyncTime);
    
    // Find API configurations by health status
    @Query("SELECT oac FROM OrganizationApiConfig oac WHERE oac.isActive = :isActive AND oac.isVerified = :isVerified AND oac.errorCount < :maxErrors")
    List<OrganizationApiConfig> findByHealthStatus(@Param("isActive") Boolean isActive, @Param("isVerified") Boolean isVerified, @Param("maxErrors") Integer maxErrors);
    
    // Get API configuration statistics by organization
    @Query("SELECT oac.apiType, COUNT(oac), SUM(CASE WHEN oac.isActive = true THEN 1 ELSE 0 END), SUM(CASE WHEN oac.isVerified = true THEN 1 ELSE 0 END) FROM OrganizationApiConfig oac WHERE oac.organization.id = :organizationId GROUP BY oac.apiType")
    List<Object[]> getApiConfigStatisticsByOrganization(@Param("organizationId") Long organizationId);
    
    // Find API configurations that need attention (high error count or expired tokens)
    @Query("SELECT oac FROM OrganizationApiConfig oac WHERE oac.isActive = true AND (oac.errorCount >= 3 OR (oac.tokenExpiresAt IS NOT NULL AND oac.tokenExpiresAt <= :now))")
    List<OrganizationApiConfig> findConfigurationsNeedingAttention(@Param("now") java.time.LocalDateTime now);
    
    // Find all active API configurations
    @Query("SELECT oac FROM OrganizationApiConfig oac WHERE oac.isActive = true")
    List<OrganizationApiConfig> findByIsActiveTrue();
} 