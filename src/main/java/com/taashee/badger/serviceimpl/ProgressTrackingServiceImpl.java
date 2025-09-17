package com.taashee.badger.serviceimpl;

import com.taashee.badger.models.*;
import com.taashee.badger.repositories.*;
import com.taashee.badger.services.ProgressTrackingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProgressTrackingServiceImpl implements ProgressTrackingService {
    private final PathwayProgressRepository pathwayProgressRepository;
    private final StepProgressRepository stepProgressRepository;
    private final PathwayRepository pathwayRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final StepVersionRepository stepVersionRepository;
    private final GroupMemberRepository groupMemberRepository;

    public ProgressTrackingServiceImpl(PathwayProgressRepository pathwayProgressRepository,
                                     StepProgressRepository stepProgressRepository,
                                     PathwayRepository pathwayRepository,
                                     GroupRepository groupRepository,
                                     UserRepository userRepository,
                                     StepVersionRepository stepVersionRepository,
                                     GroupMemberRepository groupMemberRepository) {
        this.pathwayProgressRepository = pathwayProgressRepository;
        this.stepProgressRepository = stepProgressRepository;
        this.pathwayRepository = pathwayRepository;
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.stepVersionRepository = stepVersionRepository;
        this.groupMemberRepository = groupMemberRepository;
    }

    @Override
    @Transactional
    public PathwayProgress updatePathwayProgress(Long pathwayId, Long userId, Long groupId) {
        Pathway pathway = pathwayRepository.findById(pathwayId)
                .orElseThrow(() -> new RuntimeException("Pathway not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        // Calculate progress percentage
        int progressPercentage = calculatePathwayProgress(pathwayId, userId, groupId);
        
        // Get or create pathway progress
        PathwayProgress pathwayProgress = pathwayProgressRepository
                .findByPathwayIdAndUserIdAndGroupId(pathwayId, userId, groupId)
                .orElse(new PathwayProgress());
        
        pathwayProgress.setPathway(pathway);
        pathwayProgress.setUser(user);
        pathwayProgress.setGroup(group);
        pathwayProgress.setPercent(progressPercentage);
        pathwayProgress.setCompleted(progressPercentage == 100);
        
        return pathwayProgressRepository.save(pathwayProgress);
    }

    @Override
    @Transactional
    public StepProgress updateStepProgress(Long stepVersionId, Long userId, Long groupId, StepProgress.ProgressStatus status) {
        StepVersion stepVersion = stepVersionRepository.findById(stepVersionId)
                .orElseThrow(() -> new RuntimeException("Step version not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        // Get or create step progress
        StepProgress stepProgress = stepProgressRepository
                .findByStepVersionIdAndUserIdAndGroupId(stepVersionId, userId, groupId)
                .orElse(new StepProgress());
        
        stepProgress.setStepVersion(stepVersion);
        stepProgress.setUser(user);
        stepProgress.setGroup(group);
        stepProgress.setStatus(status);
        
        if (status == StepProgress.ProgressStatus.COMPLETED) {
            stepProgress.setCompletedAt(LocalDateTime.now());
        } else {
            stepProgress.setCompletedAt(null);
        }
        
        return stepProgressRepository.save(stepProgress);
    }

    @Override
    @Transactional
    public void completeStep(Long stepVersionId, Long userId, Long groupId) {
        updateStepProgress(stepVersionId, userId, groupId, StepProgress.ProgressStatus.COMPLETED);
        
        // Update pathway progress after step completion
        StepVersion stepVersion = stepVersionRepository.findById(stepVersionId)
                .orElseThrow(() -> new RuntimeException("Step version not found"));
        
        Long pathwayId = stepVersion.getPathwayVersion().getPathway().getId();
        updatePathwayProgress(pathwayId, userId, groupId);
    }

    @Override
    public PathwayProgress getPathwayProgress(Long pathwayId, Long userId, Long groupId) {
        return pathwayProgressRepository.findByPathwayIdAndUserIdAndGroupId(pathwayId, userId, groupId)
                .orElse(null);
    }

    @Override
    public List<PathwayProgress> getGroupPathwayProgress(Long pathwayId, Long groupId) {
        return pathwayProgressRepository.findByPathwayIdAndGroupId(pathwayId, groupId);
    }

    @Override
    public List<PathwayProgress> getUserPathwayProgress(Long userId) {
        return pathwayProgressRepository.findByUserId(userId);
    }

    @Override
    public List<PathwayProgress> getUserPathwayProgressForGroup(Long userId, Long groupId) {
        return pathwayProgressRepository.findByUserId(userId).stream()
                .filter(progress -> progress.getGroup().getId().equals(groupId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getUserSubscribedPathways(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Get all groups the user is a member of
        List<GroupMember> userGroupMemberships = groupMemberRepository.findByUser(user);
        
        Set<Pathway> subscribedPathways = new HashSet<>();
        
        for (GroupMember membership : userGroupMemberships) {
            Group group = membership.getGroup();
            // Get all pathways this group is subscribed to
            List<PathwayGroupSubscription> groupSubscriptions = group.getPathwaySubscriptions();
            
            for (PathwayGroupSubscription subscription : groupSubscriptions) {
                Pathway pathway = subscription.getPathway();
                // Only include published pathways
                if (pathway.getStatus() == PathwayStatus.PUBLISHED) {
                    subscribedPathways.add(pathway);
                }
            }
        }
        
        List<Map<String, Object>> pathwayList = new ArrayList<>();
        
        for (Pathway pathway : subscribedPathways) {
            Map<String, Object> pathwayData = new HashMap<>();
            pathwayData.put("pathwayId", pathway.getId());
            pathwayData.put("pathwayName", pathway.getName());
            pathwayData.put("pathwayDescription", pathway.getDescription());
            pathwayData.put("pathwayStatus", pathway.getStatus());
            
            // Calculate user's progress for this pathway
            // We need to find which group the user is using for this pathway
            List<GroupMember> userMemberships = userGroupMemberships.stream()
                    .filter(membership -> {
                        Group group = membership.getGroup();
                        return group.getPathwaySubscriptions().stream()
                                .anyMatch(sub -> sub.getPathway().getId().equals(pathway.getId()));
                    })
                    .collect(Collectors.toList());
            
            if (!userMemberships.isEmpty()) {
                // Use the first group membership for this pathway
                GroupMember primaryMembership = userMemberships.get(0);
                Group group = primaryMembership.getGroup();
                
                int progressPercentage = calculatePathwayProgress(pathway.getId(), userId, group.getId());
                pathwayData.put("progressPercentage", progressPercentage);
                pathwayData.put("groupId", group.getId());
                pathwayData.put("groupName", group.getName());
                
                // Get step progress details
                List<StepVersion> stepVersions = stepVersionRepository.findByPathway(pathway);
                List<StepVersion> nonOptionalSteps = stepVersions.stream()
                        .filter(stepVersion -> !Boolean.TRUE.equals(stepVersion.getOptionalStep()))
                        .collect(Collectors.toList());
                
                int completedSteps = (int) nonOptionalSteps.stream()
                        .mapToLong(stepVersion -> {
                            Optional<StepProgress> stepProgress = stepProgressRepository
                                    .findByStepVersionIdAndUserIdAndGroupId(stepVersion.getId(), userId, group.getId());
                            return stepProgress.isPresent() && 
                                   stepProgress.get().getStatus() == StepProgress.ProgressStatus.COMPLETED ? 1 : 0;
                        })
                        .sum();
                
                pathwayData.put("completedSteps", completedSteps);
                pathwayData.put("totalSteps", nonOptionalSteps.size());
                pathwayData.put("isCompleted", progressPercentage == 100);
            } else {
                pathwayData.put("progressPercentage", 0);
                pathwayData.put("completedSteps", 0);
                pathwayData.put("totalSteps", 0);
                pathwayData.put("isCompleted", false);
            }
            
            pathwayList.add(pathwayData);
        }
        
        return pathwayList;
    }

    @Override
    public Map<String, Object> getGroupProgressSummary(Long groupId, Long pathwayId) {
        // Get all group members
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));
        
        List<GroupMember> groupMembers = group.getMembers();
        
        if (groupMembers.isEmpty()) {
            return Map.of(
                "overallProgress", 0,
                "totalMembers", 0,
                "completedSteps", 0,
                "totalSteps", 0,
                "memberProgress", List.of()
            );
        }
        
        // Calculate progress for each member
        List<Map<String, Object>> memberProgressList = new ArrayList<>();
        int totalProgressSum = 0;
        int totalCompletedSteps = 0;
        int totalSteps = 0;
        
        // Get total steps for the pathway (if pathwayId is provided)
        if (pathwayId != null) {
            Pathway pathway = pathwayRepository.findById(pathwayId)
                    .orElseThrow(() -> new RuntimeException("Pathway not found"));
            
            List<StepVersion> stepVersions = stepVersionRepository.findByPathway(pathway);
            List<StepVersion> nonOptionalSteps = stepVersions.stream()
                    .filter(stepVersion -> !Boolean.TRUE.equals(stepVersion.getOptionalStep()))
                    .collect(Collectors.toList());
            totalSteps = nonOptionalSteps.size();
        }
        
        for (GroupMember member : groupMembers) {
            User user = member.getUser();
            int memberProgress = 0;
            int memberCompletedSteps = 0;
            
            if (pathwayId != null) {
                // Calculate progress for specific pathway
                memberProgress = calculatePathwayProgress(pathwayId, user.getId(), groupId);
                
                // Count completed steps for this member
                List<StepVersion> stepVersions = stepVersionRepository.findByPathway(
                        pathwayRepository.findById(pathwayId).orElseThrow(() -> new RuntimeException("Pathway not found"))
                );
                List<StepVersion> nonOptionalSteps = stepVersions.stream()
                        .filter(stepVersion -> !Boolean.TRUE.equals(stepVersion.getOptionalStep()))
                        .collect(Collectors.toList());
                
                memberCompletedSteps = (int) nonOptionalSteps.stream()
                        .mapToLong(stepVersion -> {
                            Optional<StepProgress> stepProgress = stepProgressRepository
                                    .findByStepVersionIdAndUserIdAndGroupId(stepVersion.getId(), user.getId(), groupId);
                            return stepProgress.isPresent() && 
                                   stepProgress.get().getStatus() == StepProgress.ProgressStatus.COMPLETED ? 1 : 0;
                        })
                        .sum();
            } else {
                // Calculate overall progress across all pathways
                List<PathwayProgress> userPathwayProgress = getUserPathwayProgressForGroup(user.getId(), groupId);
                if (!userPathwayProgress.isEmpty()) {
                    memberProgress = (int) Math.round(userPathwayProgress.stream()
                            .mapToInt(PathwayProgress::getPercent)
                            .average()
                            .orElse(0.0));
                }
            }
            
            totalProgressSum += memberProgress;
            totalCompletedSteps += memberCompletedSteps;
            
            Map<String, Object> memberData = new HashMap<>();
            memberData.put("userId", user.getId());
            memberData.put("userName", user.getFirstName() + " " + user.getLastName());
            memberData.put("userEmail", user.getEmail());
            memberData.put("progressPercentage", memberProgress);
            memberData.put("completedSteps", memberCompletedSteps);
            
            memberProgressList.add(memberData);
        }
        
        int overallProgress = groupMembers.isEmpty() ? 0 : Math.round((float) totalProgressSum / groupMembers.size());
        
        Map<String, Object> result = new HashMap<>();
        result.put("overallProgress", overallProgress);
        result.put("totalMembers", groupMembers.size());
        result.put("completedSteps", totalCompletedSteps);
        result.put("totalSteps", totalSteps);
        result.put("memberProgress", memberProgressList);
        
        return result;
    }

    @Override
    public Map<String, Object> getIndividualProgressSummary(Long userId) {
        List<PathwayProgress> userProgress = getUserPathwayProgress(userId);
        
        return Map.of(
            "totalPathways", userProgress.size(),
            "completedPathways", userProgress.stream().mapToLong(p -> p.getCompleted() ? 1 : 0).sum(),
            "averageProgress", userProgress.isEmpty() ? 0 : 
                Math.round(userProgress.stream().mapToInt(PathwayProgress::getPercent).average().orElse(0.0))
        );
    }

    @Override
    public int calculatePathwayProgress(Long pathwayId, Long userId, Long groupId) {
        // Get all step versions for the pathway
        List<StepVersion> stepVersions = stepVersionRepository.findByPathway(
                pathwayRepository.findById(pathwayId).orElseThrow(() -> new RuntimeException("Pathway not found"))
        );
        
        // Filter to only non-optional steps (optional steps don't count toward progress)
        List<StepVersion> nonOptionalSteps = stepVersions.stream()
                .filter(stepVersion -> !Boolean.TRUE.equals(stepVersion.getOptionalStep()))
                .collect(Collectors.toList());
        
        if (nonOptionalSteps.isEmpty()) {
            return 100; // If no non-optional steps, consider it 100% complete
        }
        
        // Count completed steps
        long completedSteps = nonOptionalSteps.stream()
                .mapToLong(stepVersion -> {
                    Optional<StepProgress> stepProgress = stepProgressRepository
                            .findByStepVersionIdAndUserIdAndGroupId(stepVersion.getId(), userId, groupId);
                    return stepProgress.isPresent() && 
                           stepProgress.get().getStatus() == StepProgress.ProgressStatus.COMPLETED ? 1 : 0;
                })
                .sum();
        
        return (int) Math.round((double) completedSteps / nonOptionalSteps.size() * 100);
    }

    @Override
    @Transactional
    public void recalculateAllProgressForPathway(Long pathwayId) {
        // Get all pathway progress records for this pathway
        List<PathwayProgress> allProgress = pathwayProgressRepository.findByPathwayId(pathwayId);
        
        for (PathwayProgress progress : allProgress) {
            updatePathwayProgress(pathwayId, progress.getUser().getId(), progress.getGroup().getId());
        }
    }
}
