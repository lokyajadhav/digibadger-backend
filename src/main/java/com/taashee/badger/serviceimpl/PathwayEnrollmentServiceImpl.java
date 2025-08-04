package com.taashee.badger.serviceimpl;

import com.taashee.badger.models.*;
import com.taashee.badger.repositories.*;
import com.taashee.badger.services.PathwayEnrollmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class PathwayEnrollmentServiceImpl implements PathwayEnrollmentService {

    @Autowired
    private PathwayProgressRepository pathwayProgressRepository;
    
    @Autowired
    private PathwayElementProgressRepository pathwayElementProgressRepository;
    
    @Autowired
    private PathwayRepository pathwayRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    // Removed group-related repositories - simplified approach
    
    @Autowired
    private BadgeInstanceRepository badgeInstanceRepository;
    
    @Autowired
    private OrganizationStaffRepository organizationStaffRepository;

    @Override
    public PathwayProgress enrollUserInPathway(Long pathwayId, String userEmail) {
        // Check if user is already enrolled
        Optional<PathwayProgress> existingProgress = pathwayProgressRepository
                .findByPathwayIdAndUserEmail(pathwayId, userEmail);
        
        if (existingProgress.isPresent()) {
            throw new RuntimeException("User is already enrolled in this pathway");
        }

        // Get pathway and user
        Pathway pathway = pathwayRepository.findById(pathwayId)
                .orElseThrow(() -> new RuntimeException("Pathway not found"));
        
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if pathway is published
        if (!Pathway.PathwayStatus.PUBLISHED.equals(pathway.getStatus())) {
            throw new RuntimeException("Pathway is not available for enrollment");
        }

        // Create pathway progress (simplified - no group association)
        PathwayProgress progress = new PathwayProgress();
        progress.setPathway(pathway);
        progress.setUser(user);
        progress.setProgressPercentage(0.0);
        progress.setCompletedElements(0);
        progress.setTotalElements(pathway.getTotalElementsCount());
        progress.setCompletedBadges(0);
        progress.setTotalBadges(pathway.getTotalBadgesCount());
        progress.setIsCompleted(false);
        progress.setStartedAt(LocalDateTime.now());
        progress.setLastActivityAt(LocalDateTime.now());
        progress.setTimeSpentMinutes(0);
        progress.setAchievements(new ArrayList<>());

        // Initialize element progress for all pathway elements
        List<PathwayElementProgress> elementProgressList = new ArrayList<>();
        for (PathwayElement element : pathway.getElements()) {
            PathwayElementProgress elementProgress = new PathwayElementProgress();
            elementProgress.setPathwayProgress(progress);
            elementProgress.setElement(element);
            elementProgress.setIsCompleted(false);
            elementProgress.setCompletedAt(null);
            elementProgress.setTimeSpentMinutes(0);
            elementProgressList.add(elementProgress);
        }
        
        progress.setElementProgress(elementProgressList);
        
        return pathwayProgressRepository.save(progress);
    }

    @Override
    public void unenrollUserFromPathway(Long pathwayId, String userEmail) {
        Optional<PathwayProgress> progress = pathwayProgressRepository
                .findByPathwayIdAndUserEmail(pathwayId, userEmail);
        
        if (progress.isPresent()) {
            pathwayProgressRepository.delete(progress.get());
        }
    }

    @Override
    public Optional<PathwayProgress> getUserPathwayProgress(Long pathwayId, String userEmail) {
        return pathwayProgressRepository.findByPathwayIdAndUserEmail(pathwayId, userEmail);
    }

    @Override
    public List<PathwayProgress> getUserEnrollments(String userEmail) {
        return pathwayProgressRepository.findByUserEmail(userEmail);
    }

    @Override
    public PathwayElementProgress completeElement(Long pathwayId, Long elementId, String userEmail) {
        // Get pathway progress
        PathwayProgress pathwayProgress = pathwayProgressRepository
                .findByPathwayIdAndUserEmail(pathwayId, userEmail)
                .orElseThrow(() -> new RuntimeException("User not enrolled in pathway"));

        // Get element progress
        PathwayElementProgress elementProgress = pathwayElementProgressRepository
                .findByPathwayProgressIdAndElementId(pathwayProgress.getId(), elementId)
                .orElseThrow(() -> new RuntimeException("Element progress not found"));

        // Check if element can be completed
        if (!canCompleteElement(pathwayId, elementId, userEmail)) {
            throw new RuntimeException("Element cannot be completed yet");
        }

        // Complete the element
        elementProgress.setIsCompleted(true);
        elementProgress.setCompletedAt(LocalDateTime.now());

        // Update pathway progress
        pathwayProgress.setCompletedElements(pathwayProgress.getCompletedElements() + 1);
        pathwayProgress.setLastActivityAt(LocalDateTime.now());
        
        // Recalculate progress percentage
        double progressPercentage = (double) pathwayProgress.getCompletedElements() / pathwayProgress.getTotalElements() * 100;
        pathwayProgress.setProgressPercentage(progressPercentage);

        // Check if pathway is completed
        if (progressPercentage >= 100.0) {
            pathwayProgress.setIsCompleted(true);
            pathwayProgress.setCompletedAt(LocalDateTime.now());
            issueCompletionBadge(pathwayId, userEmail);
        }

        pathwayProgressRepository.save(pathwayProgress);
        return pathwayElementProgressRepository.save(elementProgress);
    }

    @Override
    public List<Pathway> getAvailablePathways(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Get user's organization memberships
        List<OrganizationStaff> staffMemberships = organizationStaffRepository.findByUserId(user.getId());
        List<Long> organizationIds = staffMemberships.stream()
                .map(staff -> staff.getOrganization().getId())
                .collect(Collectors.toList());

        // Get published pathways from user's organizations (simplified - no group subscriptions)
        return pathwayRepository.findByOrganizationIdInAndStatus(organizationIds, Pathway.PathwayStatus.PUBLISHED);
    }

    @Override
    public Object getPathwayAnalytics(Long pathwayId, String userEmail) {
        // Verify user has access to pathway analytics
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Pathway pathway = pathwayRepository.findById(pathwayId)
                .orElseThrow(() -> new RuntimeException("Pathway not found"));

        // Check if user belongs to pathway's organization
        List<OrganizationStaff> staffMemberships = organizationStaffRepository.findByUserId(user.getId());
        boolean hasAccess = staffMemberships.stream()
                .anyMatch(staff -> staff.getOrganization().getId().equals(pathway.getOrganization().getId()));
        
        if (!hasAccess) {
            throw new RuntimeException("Access denied to pathway analytics");
        }

        // Get analytics data
        List<PathwayProgress> allProgress = pathwayProgressRepository.findByPathwayId(pathwayId);
        
        Map<String, Object> analytics = new HashMap<>();
        analytics.put("totalEnrollments", allProgress.size());
        analytics.put("completedEnrollments", allProgress.stream().filter(PathwayProgress::getIsCompleted).count());
        analytics.put("inProgressEnrollments", allProgress.stream().filter(p -> !p.getIsCompleted() && p.getProgressPercentage() > 0).count());
        analytics.put("notStartedEnrollments", allProgress.stream().filter(p -> p.getProgressPercentage() == 0).count());
        
        if (!allProgress.isEmpty()) {
            double avgProgress = allProgress.stream()
                    .mapToDouble(PathwayProgress::getProgressPercentage)
                    .average()
                    .orElse(0.0);
            analytics.put("averageProgress", avgProgress);
        }

        return analytics;
    }

    // Removed bulkEnrollGroup - not needed with simplified approach

    @Override
    public void updateProgressOnBadgeEarned(Long pathwayId, Long userId, Long badgeId) {
        Optional<PathwayProgress> progress = pathwayProgressRepository.findByPathwayIdAndUserId(pathwayId, userId);
        if (progress.isPresent()) {
            PathwayProgress pathwayProgress = progress.get();
            pathwayProgress.setCompletedBadges(pathwayProgress.getCompletedBadges() + 1);
            pathwayProgress.setLastActivityAt(LocalDateTime.now());
            pathwayProgressRepository.save(pathwayProgress);
        }
    }

    @Override
    public boolean canCompleteElement(Long pathwayId, Long elementId, String userEmail) {
        PathwayProgress pathwayProgress = pathwayProgressRepository
                .findByPathwayIdAndUserEmail(pathwayId, userEmail)
                .orElse(null);
        
        if (pathwayProgress == null) {
            return false;
        }

        PathwayElement element = pathwayProgress.getPathway().getElements().stream()
                .filter(e -> e.getId().equals(elementId))
                .findFirst()
                .orElse(null);
        
        if (element == null) {
            return false;
        }

        // Check prerequisites
        if (!element.getPrerequisites().isEmpty()) {
            List<Long> completedElementIds = pathwayProgress.getElementProgress().stream()
                    .filter(PathwayElementProgress::getIsCompleted)
                    .map(ep -> ep.getElement().getId())
                    .collect(Collectors.toList());
            
            return element.getPrerequisites().stream()
                    .allMatch(completedElementIds::contains);
        }

        return true;
    }

    @Override
    public boolean isPathwayCompleted(Long pathwayId, String userEmail) {
        Optional<PathwayProgress> progress = pathwayProgressRepository
                .findByPathwayIdAndUserEmail(pathwayId, userEmail);
        return progress.map(PathwayProgress::getIsCompleted).orElse(false);
    }

    @Override
    public void issueCompletionBadge(Long pathwayId, String userEmail) {
        PathwayProgress pathwayProgress = pathwayProgressRepository
                .findByPathwayIdAndUserEmail(pathwayId, userEmail)
                .orElseThrow(() -> new RuntimeException("Pathway progress not found"));

        Pathway pathway = pathwayProgress.getPathway();
        User user = pathwayProgress.getUser();

        if (pathway.getCompletionBadge() != null && !pathwayProgress.getCompletionBadgeIssued()) {
            // Create badge instance for completion
            BadgeInstance completionBadge = new BadgeInstance();
            completionBadge.setBadgeClass(pathway.getCompletionBadge());
            completionBadge.setRecipient(user);
            completionBadge.setOrganization(pathway.getOrganization());
            completionBadge.setIssuedOn(LocalDateTime.now());
            completionBadge.setNarrative("Pathway completion: " + pathway.getName());

            badgeInstanceRepository.save(completionBadge);

            // Update pathway progress
            pathwayProgress.setCompletionBadgeIssued(true);
            pathwayProgress.setCompletionBadgeIssuedAt(LocalDateTime.now());
            pathwayProgressRepository.save(pathwayProgress);
        }
    }
} 