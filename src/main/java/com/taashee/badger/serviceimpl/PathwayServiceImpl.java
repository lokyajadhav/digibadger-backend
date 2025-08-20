package com.taashee.badger.serviceimpl;

import com.taashee.badger.models.Pathway;
import com.taashee.badger.models.PathwayDTO;
import com.taashee.badger.models.PathwayElement;
import com.taashee.badger.models.PathwayElementDTO;
import com.taashee.badger.models.PathwayProgress;
import com.taashee.badger.models.Organization;
import com.taashee.badger.models.User;
import com.taashee.badger.models.OrganizationStaff;
import com.taashee.badger.repositories.PathwayRepository;
import com.taashee.badger.repositories.PathwayElementRepository;
import com.taashee.badger.repositories.PathwayProgressRepository;
import com.taashee.badger.repositories.OrganizationRepository;
import com.taashee.badger.repositories.UserRepository;
import com.taashee.badger.repositories.OrganizationStaffRepository;
import com.taashee.badger.services.PathwayService;
import com.taashee.badger.exceptions.ResourceNotFoundException;
import com.taashee.badger.exceptions.UnauthorizedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.function.Supplier;
import java.util.ArrayList;

@Service
@Transactional
public class PathwayServiceImpl implements PathwayService {

    @Autowired
    private PathwayRepository pathwayRepository;

    @Autowired
    private PathwayElementRepository pathwayElementRepository;

    @Autowired
    private PathwayProgressRepository pathwayProgressRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrganizationStaffRepository organizationStaffRepository;

    @Override
    public PathwayDTO createPathway(PathwayDTO pathwayDTO, Long organizationId) {
        // Get current user from security context (for backward compatibility)
        String userEmail = (String) org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        return createPathwayForUser(pathwayDTO, organizationId, user);
    }

    private PathwayDTO createPathwayForUser(PathwayDTO pathwayDTO, Long organizationId, User user) {
        // Validate organization exists
        Organization organization = organizationRepository.findById(organizationId)
                .orElseGet(() -> {
                    // For development/testing: create a default organization if it doesn't exist
                    Organization defaultOrg = new Organization();
                    defaultOrg.setNameEnglish("Default Organization");
                    defaultOrg.setDescriptionEnglish("Default organization for testing");
                    defaultOrg.setEmail("default@organization.com");
                    defaultOrg.setUrlEnglish("https://default-organization.com");
                    return organizationRepository.save(defaultOrg);
                });

        // Check if pathway name already exists for this organization
        if (pathwayRepository.existsByNameAndOrganizationId(pathwayDTO.getName(), organizationId)) {
            throw new IllegalArgumentException("Pathway with this name already exists in the organization");
        }

        // Create new pathway
        Pathway pathway = new Pathway();
        pathway.setName(pathwayDTO.getName());
        pathway.setDescription(pathwayDTO.getDescription());
        pathway.setOrganization(organization);
        pathway.setCompletionType(pathwayDTO.getCompletionType() != null ? 
            Pathway.CompletionType.valueOf(pathwayDTO.getCompletionType().toUpperCase()) : 
            Pathway.CompletionType.CONJUNCTION);
        pathway.setCreatedBy(user); // Set the user who created the pathway

        Pathway savedPathway = pathwayRepository.save(pathway);
        return convertToDTO(savedPathway);
    }

    @Override
    public List<PathwayDTO> getPathwaysByOrganization(Long organizationId) {
        List<Pathway> pathways = pathwayRepository.findByOrganizationId(organizationId);
        return pathways.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<PathwayDTO> getPathwayById(Long pathwayId, Long organizationId) {
        Optional<Pathway> pathway = pathwayRepository.findById(pathwayId);
        
        if (pathway.isPresent() && pathway.get().getOrganization().getId().equals(organizationId)) {
            return Optional.of(convertToDTO(pathway.get()));
        }
        
        return Optional.empty();
    }

    @Override
    public PathwayDTO updatePathway(Long pathwayId, PathwayDTO pathwayDTO, Long organizationId) {
        Pathway pathway = pathwayRepository.findById(pathwayId)
                .orElseThrow(() -> new ResourceNotFoundException("Pathway not found"));

        // Check if pathway belongs to the organization
        if (!pathway.getOrganization().getId().equals(organizationId)) {
            throw new UnauthorizedException("Pathway does not belong to this organization");
        }

        // Update pathway fields
        pathway.setName(pathwayDTO.getName());
        pathway.setDescription(pathwayDTO.getDescription());
        pathway.setCompletionType(pathwayDTO.getCompletionType() != null ? 
            Pathway.CompletionType.valueOf(pathwayDTO.getCompletionType().toUpperCase()) : 
            Pathway.CompletionType.CONJUNCTION);

        Pathway updatedPathway = pathwayRepository.save(pathway);
        return convertToDTO(updatedPathway);
    }

    @Override
    public void deletePathway(Long pathwayId, Long organizationId) {
        Pathway pathway = pathwayRepository.findById(pathwayId)
                .orElseThrow(() -> new ResourceNotFoundException("Pathway not found"));

        // Check if pathway belongs to the organization
        if (!pathway.getOrganization().getId().equals(organizationId)) {
            throw new UnauthorizedException("Pathway does not belong to this organization");
        }

        pathwayRepository.delete(pathway);
    }

    @Override
    public List<PathwayDTO> getAvailablePathwaysForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // TODO: Get user's organization from OrganizationStaff or OrganizationUser relationship
        // For now, return all pathways (this should be filtered by user's organization)
        List<Pathway> pathways = pathwayRepository.findAll();
        
        return pathways.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void enrollUserInPathway(Long pathwayId, Long userId) {
        Pathway pathway = pathwayRepository.findById(pathwayId)
                .orElseThrow(() -> new ResourceNotFoundException("Pathway not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Check if user is already enrolled
        Optional<PathwayProgress> existingProgress = pathwayProgressRepository.findByPathwayIdAndUserEmail(pathwayId, user.getEmail());
        if (existingProgress.isPresent()) {
            throw new IllegalArgumentException("User is already enrolled in this pathway");
        }

        // Create new progress entry
        PathwayProgress progress = new PathwayProgress();
        progress.setPathway(pathway);
        progress.setUser(user);
        progress.setProgressPercentage(0.0);
        progress.setCompletedElements(0);
        progress.setTotalElements((int) pathwayElementRepository.countByPathwayId(pathwayId));
        progress.setIsCompleted(false);

        pathwayProgressRepository.save(progress);
    }

    @Override
    public PathwayDTO getPathwayProgress(Long pathwayId, Long userId) {
        Pathway pathway = pathwayRepository.findById(pathwayId)
                .orElseThrow(() -> new ResourceNotFoundException("Pathway not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        PathwayProgress progress = pathwayProgressRepository.findByPathwayIdAndUserEmail(pathwayId, user.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not enrolled in this pathway"));

        PathwayDTO pathwayDTO = convertToDTO(pathway);
        pathwayDTO.setCompletedElements(progress.getCompletedElements());
        pathwayDTO.setTotalElements(progress.getTotalElements());
        pathwayDTO.setProgressPercentage(progress.getProgressPercentage().doubleValue());
        pathwayDTO.setIsCompleted(progress.getIsCompleted());

        return pathwayDTO;
    }

    @Override
    public boolean pathwayExistsAndBelongsToOrganization(Long pathwayId, Long organizationId) {
        return pathwayRepository.existsByIdAndOrganizationId(pathwayId, organizationId);
    }

    // === ENTERPRISE-GRADE PUBLISHING FUNCTIONALITY ===

    @Override
    public PathwayDTO publishPathway(Long pathwayId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        Pathway pathway = pathwayRepository.findById(pathwayId)
                .orElseThrow(() -> new ResourceNotFoundException("Pathway not found"));
        
        // Check if user has permission to manage this pathway
        if (!hasPermissionToManagePathway(pathwayId, userEmail)) {
            throw new UnauthorizedException("You don't have permission to publish this pathway");
        }
        
        // Validate pathway before publishing
        List<String> validationErrors = validatePathwayForPublishing(pathwayId, userEmail);
        if (!validationErrors.isEmpty()) {
            throw new IllegalArgumentException("Pathway validation failed: " + String.join(", ", validationErrors));
        }
        
        // Publish the pathway
        pathway.publish(user);
        Pathway savedPathway = pathwayRepository.save(pathway);
        
        return convertToDTO(savedPathway);
    }

    @Override
    public PathwayDTO unpublishPathway(Long pathwayId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        Pathway pathway = pathwayRepository.findById(pathwayId)
                .orElseThrow(() -> new ResourceNotFoundException("Pathway not found"));
        
        // Check if user has permission to manage this pathway
        if (!hasPermissionToManagePathway(pathwayId, userEmail)) {
            throw new UnauthorizedException("You don't have permission to unpublish this pathway");
        }
        
        // Unpublish the pathway
        pathway.unpublish();
        Pathway savedPathway = pathwayRepository.save(pathway);
        
        return convertToDTO(savedPathway);
    }

    @Override
    public PathwayDTO archivePathway(Long pathwayId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        Pathway pathway = pathwayRepository.findById(pathwayId)
                .orElseThrow(() -> new ResourceNotFoundException("Pathway not found"));
        
        // Check if user has permission to manage this pathway
        if (!hasPermissionToManagePathway(pathwayId, userEmail)) {
            throw new UnauthorizedException("You don't have permission to archive this pathway");
        }
        
        // Archive the pathway
        pathway.archive();
        Pathway savedPathway = pathwayRepository.save(pathway);
        
        return convertToDTO(savedPathway);
    }

    @Override
    public List<String> validatePathwayForPublishing(Long pathwayId, String userEmail) {
        Pathway pathway = pathwayRepository.findById(pathwayId)
                .orElseThrow(() -> new ResourceNotFoundException("Pathway not found"));
        
        // Check if user has permission to manage this pathway
        if (!hasPermissionToManagePathway(pathwayId, userEmail)) {
            throw new UnauthorizedException("You don't have permission to validate this pathway");
        }
        
        return pathway.getValidationErrors();
    }

    @Override
    public PathwayDTO getPathwayValidationStatus(Long pathwayId, String userEmail) {
        Pathway pathway = pathwayRepository.findById(pathwayId)
                .orElseThrow(() -> new ResourceNotFoundException("Pathway not found"));
        
        // Check if user has permission to view this pathway
        if (!hasPermissionToViewPathway(pathwayId, userEmail)) {
            throw new UnauthorizedException("You don't have permission to view this pathway");
        }
        
        return convertToDTO(pathway);
    }

    // === ORGANIZATION-SCOPED ACCESS CONTROL ===

    @Override
    public boolean hasPermissionToManagePathway(Long pathwayId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        Pathway pathway = pathwayRepository.findById(pathwayId)
                .orElseThrow(() -> new ResourceNotFoundException("Pathway not found"));
        
        // ADMIN can manage all pathways
        if (user.getRoles().contains("ADMIN")) {
            return true;
        }
        
        // ISSUER can only manage pathways in their organization
        if (user.getRoles().contains("ISSUER")) {
            List<OrganizationStaff> staffList = organizationStaffRepository.findByUserId(user.getId());
            return staffList.stream()
                    .anyMatch(staff -> staff.getOrganization().getId().equals(pathway.getOrganization().getId()) &&
                                     "owner".equalsIgnoreCase(staff.getStaffRole()));
        }
        
        return false;
    }

    @Override
    public boolean hasPermissionToViewPathway(Long pathwayId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        Pathway pathway = pathwayRepository.findById(pathwayId)
                .orElseThrow(() -> new ResourceNotFoundException("Pathway not found"));
        
        // ADMIN can view all pathways
        if (user.getRoles().contains("ADMIN")) {
            return true;
        }
        
        // ISSUER can view pathways in their organization
        if (user.getRoles().contains("ISSUER")) {
            List<OrganizationStaff> staffList = organizationStaffRepository.findByUserId(user.getId());
            return staffList.stream()
                    .anyMatch(staff -> staff.getOrganization().getId().equals(pathway.getOrganization().getId()));
        }
        
        // USER can view published pathways in their organization
        if (user.getRoles().contains("USER")) {
            List<OrganizationStaff> staffList = organizationStaffRepository.findByUserId(user.getId());
            return staffList.stream()
                    .anyMatch(staff -> staff.getOrganization().getId().equals(pathway.getOrganization().getId())) &&
                   Pathway.PathwayStatus.PUBLISHED.equals(pathway.getStatus());
        }
        
        return false;
    }

    @Override
    public Long getUserOrganizationId(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        List<OrganizationStaff> staffList = organizationStaffRepository.findByUserId(user.getId());
        if (staffList.isEmpty()) {
            throw new ResourceNotFoundException("User is not associated with any organization");
        }
        
        // Return the first organization (assuming user belongs to one organization)
        return staffList.get(0).getOrganization().getId();
    }

    // === PATHWAY STATUS MANAGEMENT ===

    @Override
    public List<PathwayDTO> getPathwaysByStatus(Long organizationId, Pathway.PathwayStatus status) {
        List<Pathway> pathways = pathwayRepository.findByOrganizationIdAndStatus(organizationId, status);
        return pathways.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public PathwayDTO getPathwayStatistics(Long organizationId) {
        // This would return statistics about pathways in the organization
        // For now, returning a placeholder
        PathwayDTO stats = new PathwayDTO();
        stats.setName("Pathway Statistics");
        stats.setDescription("Statistics for organization " + organizationId);
        return stats;
    }

    // === STUDENT ACCESS ===

    @Override
    public List<PathwayDTO> getPublishedPathwaysForStudent(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        List<OrganizationStaff> staffList = organizationStaffRepository.findByUserId(user.getId());
        if (staffList.isEmpty()) {
            return new ArrayList<>();
        }
        
        Long organizationId = staffList.get(0).getOrganization().getId();
        return getPublishedPathwaysForOrganization(organizationId);
    }

    @Override
    public boolean canStudentEnrollInPathway(Long pathwayId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        Pathway pathway = pathwayRepository.findById(pathwayId)
                .orElseThrow(() -> new ResourceNotFoundException("Pathway not found"));
        
        // Check if pathway is published
        if (!Pathway.PathwayStatus.PUBLISHED.equals(pathway.getStatus())) {
            return false;
        }
        
        // Check if user belongs to the same organization
        List<OrganizationStaff> staffList = organizationStaffRepository.findByUserId(user.getId());
        return staffList.stream()
                .anyMatch(staff -> staff.getOrganization().getId().equals(pathway.getOrganization().getId()));
    }

    @Override
    public List<PathwayDTO> getPublishedPathwaysForOrganization(Long organizationId) {
        List<Pathway> pathways = pathwayRepository.findByOrganizationIdAndStatus(organizationId, Pathway.PathwayStatus.PUBLISHED);
        return pathways.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<PathwayDTO> getPublishedPathwayById(Long pathwayId, Long organizationId) {
        Optional<Pathway> pathway = pathwayRepository.findByIdAndOrganizationIdAndStatus(pathwayId, organizationId, Pathway.PathwayStatus.PUBLISHED);
        return pathway.map(this::convertToDTO);
    }

    @Override
    public PathwayDTO createPathwayForCurrentUser(PathwayDTO pathwayDTO, String userEmail) {
        // Get user's organization
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        List<OrganizationStaff> staffList = organizationStaffRepository.findByUserId(user.getId());
        if (staffList.isEmpty()) {
            throw new UnauthorizedException("User is not associated with any organization");
        }
        
        // For now, use the first organization. In the future, you might want to allow selection
        Organization organization = staffList.get(0).getOrganization();
        
        return createPathwayForUser(pathwayDTO, organization.getId(), user);
    }

    @Override
    public List<PathwayDTO> getPathwaysForCurrentUser(String userEmail) {
        try {
            // Get user's organization
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            
            List<OrganizationStaff> staffList = organizationStaffRepository.findByUserId(user.getId());
            if (staffList.isEmpty()) {
                return List.of(); // Return empty list if no organization
            }
            
            // For now, use the first organization. In the future, you might want to return all organizations
            Organization organization = staffList.get(0).getOrganization();
            
            // Add logging to debug the issue
            System.out.println("Fetching pathways for organization: " + organization.getId());
            
            List<PathwayDTO> pathways = getPathwaysByOrganization(organization.getId());
            System.out.println("Successfully fetched " + pathways.size() + " pathways");
            
            return pathways;
        } catch (Exception e) {
            System.err.println("Error in getPathwaysForCurrentUser: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to retrieve pathways: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<PathwayDTO> getPathwayByIdForCurrentUser(Long pathwayId, String userEmail) {
        // Get user's organization
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        List<OrganizationStaff> staffList = organizationStaffRepository.findByUserId(user.getId());
        if (staffList.isEmpty()) {
            return Optional.empty();
        }
        
        // For now, use the first organization. In the future, you might want to check all organizations
        Organization organization = staffList.get(0).getOrganization();
        
        return getPathwayById(pathwayId, organization.getId());
    }

    @Override
    public PathwayDTO updatePathwayForCurrentUser(Long pathwayId, PathwayDTO pathwayDTO, String userEmail) {
        // Get user's organization
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        List<OrganizationStaff> staffList = organizationStaffRepository.findByUserId(user.getId());
        if (staffList.isEmpty()) {
            throw new UnauthorizedException("User is not associated with any organization");
        }
        
        // For now, use the first organization. In the future, you might want to allow selection
        Organization organization = staffList.get(0).getOrganization();
        
        return updatePathway(pathwayId, pathwayDTO, organization.getId());
    }

    @Override
    public void deletePathwayForCurrentUser(Long pathwayId, String userEmail) {
        // Get user's organization
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        List<OrganizationStaff> staffList = organizationStaffRepository.findByUserId(user.getId());
        if (staffList.isEmpty()) {
            throw new UnauthorizedException("User is not associated with any organization");
        }
        
        // For now, use the first organization. In the future, you might want to allow selection
        Organization organization = staffList.get(0).getOrganization();
        
        deletePathway(pathwayId, organization.getId());
    }

    private PathwayDTO convertToDTO(Pathway pathway) {
        PathwayDTO dto = new PathwayDTO();
        dto.setId(pathway.getId());
        dto.setName(pathway.getName());
        dto.setDescription(pathway.getDescription());
        dto.setOrganizationId(pathway.getOrganization().getId());
        dto.setOrganizationName(pathway.getOrganization().getNameEnglish());
        dto.setCompletionType(pathway.getCompletionType() != null ? pathway.getCompletionType().name() : null);
        dto.setCreatedAt(pathway.getCreatedAt());
        dto.setUpdatedAt(pathway.getUpdatedAt());
        
        // Get elements if needed
        if (pathway.getElements() != null) {
            List<PathwayElementDTO> elementDTOs = pathway.getElements().stream()
                    .map(this::convertElementToDTO)
                    .collect(Collectors.toList());
            dto.setElements(elementDTOs);
        }
        
        return dto;
    }

    private PathwayElementDTO convertElementToDTO(PathwayElement element) {
        PathwayElementDTO dto = new PathwayElementDTO();
        dto.setId(element.getId());
        dto.setPathwayId(element.getPathway().getId());
        dto.setParentElementId(element.getParentElement() != null ? element.getParentElement().getId() : null);
        dto.setName(element.getName());
        dto.setDescription(element.getDescription());
        dto.setElementType(element.getElementType() != null ? element.getElementType().name() : null);
        dto.setOrderIndex(element.getOrderIndex());
        dto.setCompletionRule(element.getCompletionRule() != null ? element.getCompletionRule().name() : null);
        dto.setRequiredCount(element.getRequiredCount());
        dto.setIsOptional(element.getIsOptional());
        dto.setCountsTowardsParent(element.getCountsTowardsParent());
        dto.setCreatedAt(element.getCreatedAt());
        
        // Note: Badge associations are now handled through PathwayElementBadgeService
        // This method only converts basic element data
        
        return dto;
    }
} 