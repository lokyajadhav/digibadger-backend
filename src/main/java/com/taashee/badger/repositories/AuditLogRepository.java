package com.taashee.badger.repositories;

import com.taashee.badger.models.AuditLog;
import com.taashee.badger.models.Pathway;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    
    List<AuditLog> findByPathway(Pathway pathway);
    
    List<AuditLog> findByPathwayOrderByTimestampDesc(Pathway pathway);
    
    Page<AuditLog> findByPathwayOrderByTimestampDesc(Pathway pathway, Pageable pageable);
    
    List<AuditLog> findByEntityTypeAndEntityIdOrderByTimestampDesc(String entityType, Long entityId);
    
    @Query("SELECT al FROM AuditLog al WHERE al.pathway = :pathway AND al.timestamp >= :since ORDER BY al.timestamp DESC")
    List<AuditLog> findByPathwayAndTimestampAfter(@Param("pathway") Pathway pathway, @Param("since") LocalDateTime since);
    
    @Query("SELECT al FROM AuditLog al WHERE al.pathway = :pathway AND al.action = :action ORDER BY al.timestamp DESC")
    List<AuditLog> findByPathwayAndAction(@Param("pathway") Pathway pathway, @Param("action") AuditLog.AuditAction action);
}