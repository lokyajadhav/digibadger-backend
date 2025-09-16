package com.taashee.badger.services;

import com.taashee.badger.models.PathwayProgress;
import com.taashee.badger.models.StepProgress;
import com.taashee.badger.models.User;
import com.taashee.badger.models.Group;
import java.util.List;
import java.util.Map;

public interface ProgressTrackingService {
    // Progress tracking
    PathwayProgress updatePathwayProgress(Long pathwayId, Long userId, Long groupId);
    StepProgress updateStepProgress(Long stepVersionId, Long userId, Long groupId, StepProgress.ProgressStatus status);
    void completeStep(Long stepVersionId, Long userId, Long groupId);
    
    // Progress retrieval
    PathwayProgress getPathwayProgress(Long pathwayId, Long userId, Long groupId);
    List<PathwayProgress> getGroupPathwayProgress(Long pathwayId, Long groupId);
    List<PathwayProgress> getUserPathwayProgress(Long userId);
    List<PathwayProgress> getUserPathwayProgressForGroup(Long userId, Long groupId);
    
    // User subscribed pathways
    List<Map<String, Object>> getUserSubscribedPathways(Long userId);
    
    // Group progress aggregation
    Map<String, Object> getGroupProgressSummary(Long groupId, Long pathwayId);
    Map<String, Object> getIndividualProgressSummary(Long userId);
    
    // Progress calculation
    int calculatePathwayProgress(Long pathwayId, Long userId, Long groupId);
    void recalculateAllProgressForPathway(Long pathwayId);
}
