package com.taashee.badger.serviceimpl;

import com.taashee.badger.models.PathwayElement;
import com.taashee.badger.models.PathwayElementDTO;
import com.taashee.badger.models.Pathway;
import com.taashee.badger.models.User;
import com.taashee.badger.models.Organization;
import com.taashee.badger.models.OrganizationStaff;
import com.taashee.badger.repositories.PathwayElementRepository;
import com.taashee.badger.repositories.PathwayRepository;
import com.taashee.badger.repositories.UserRepository;
import com.taashee.badger.repositories.OrganizationStaffRepository;

import com.taashee.badger.services.PathwayElementService;
import com.taashee.badger.exceptions.ResourceNotFoundException;
import com.taashee.badger.exceptions.UnauthorizedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class PathwayElementServiceImpl implements PathwayElementService {

    @Autowired
    private PathwayElementRepository pathwayElementRepository;

    @Autowired
    private PathwayRepository pathwayRepository;

    @Autowired
    private OrganizationStaffRepository organizationStaffRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public PathwayElementDTO createPathwayElement(PathwayElementDTO elementDTO, Long pathwayId, Long organizationId) {
        // Validate pathway exists and belongs to organization
        Pathway pathway = pathwayRepository.findById(pathwayId)
                .orElseThrow(() -> new ResourceNotFoundException("Pathway not found"));

        if (!pathway.getOrganization().getId().equals(organizationId)) {
            throw new UnauthorizedException("Pathway does not belong to this organization");
        }

        // Get next order index
        Integer nextOrderIndex = pathwayElementRepository.findNextOrderIndex(pathwayId);

        // Create new pathway element
        PathwayElement element = new PathwayElement();
        element.setPathway(pathway);
        element.setName(elementDTO.getName());
        element.setDescription(elementDTO.getDescription());
        element.setElementType(elementDTO.getElementType() != null ? 
            PathwayElement.ElementType.valueOf(elementDTO.getElementType().toUpperCase()) : 
            PathwayElement.ElementType.ELEMENT);
        element.setOrderIndex(nextOrderIndex);
        element.setCompletionRule(elementDTO.getCompletionRule() != null ? 
            PathwayElement.CompletionRule.valueOf(elementDTO.getCompletionRule().toUpperCase()) : 
            PathwayElement.CompletionRule.ALL);
        element.setRequiredCount(elementDTO.getRequiredCount() != null ? elementDTO.getRequiredCount() : 1);
        element.setIsOptional(elementDTO.getIsOptional() != null ? elementDTO.getIsOptional() : false);
        element.setCountsTowardsParent(elementDTO.getCountsTowardsParent() != null ? elementDTO.getCountsTowardsParent() : true);

        // Set parent element if provided
        if (elementDTO.getParentElementId() != null) {
            PathwayElement parentElement = pathwayElementRepository.findById(elementDTO.getParentElementId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent element not found"));
            element.setParentElement(parentElement);
        }

        PathwayElement savedElement = pathwayElementRepository.save(element);
        return convertToDTO(savedElement);
    }

    @Override
    public List<PathwayElementDTO> getPathwayElements(Long pathwayId, Long organizationId) {
        // Validate pathway exists and belongs to organization
        if (!pathwayRepository.existsById(pathwayId)) {
            throw new ResourceNotFoundException("Pathway not found");
        }

        Pathway pathway = pathwayRepository.findById(pathwayId).get();
        if (!pathway.getOrganization().getId().equals(organizationId)) {
            throw new UnauthorizedException("Pathway does not belong to this organization");
        }

        List<PathwayElement> elements = pathwayElementRepository.findByPathwayIdOrderByOrderIndex(pathwayId);
        return elements.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<PathwayElementDTO> getPathwayElementById(Long elementId, Long pathwayId, Long organizationId) {
        Optional<PathwayElement> element = pathwayElementRepository.findById(elementId);
        
        if (element.isPresent()) {
            PathwayElement pathwayElement = element.get();
            if (pathwayElement.getPathway().getId().equals(pathwayId) && 
                pathwayElement.getPathway().getOrganization().getId().equals(organizationId)) {
                return Optional.of(convertToDTO(pathwayElement));
            }
        }
        
        return Optional.empty();
    }

    @Override
    public PathwayElementDTO updatePathwayElement(Long elementId, PathwayElementDTO elementDTO, Long pathwayId, Long organizationId) {
        PathwayElement element = pathwayElementRepository.findById(elementId)
                .orElseThrow(() -> new ResourceNotFoundException("Pathway element not found"));

        // Check if element belongs to the pathway and organization
        if (!element.getPathway().getId().equals(pathwayId) || 
            !element.getPathway().getOrganization().getId().equals(organizationId)) {
            throw new UnauthorizedException("Pathway element does not belong to this organization");
        }

        // Update element fields
        element.setName(elementDTO.getName());
        element.setDescription(elementDTO.getDescription());
        element.setElementType(elementDTO.getElementType() != null ? 
            PathwayElement.ElementType.valueOf(elementDTO.getElementType().toUpperCase()) : 
            PathwayElement.ElementType.ELEMENT);
        element.setCompletionRule(elementDTO.getCompletionRule() != null ? 
            PathwayElement.CompletionRule.valueOf(elementDTO.getCompletionRule().toUpperCase()) : 
            PathwayElement.CompletionRule.ALL);
        element.setRequiredCount(elementDTO.getRequiredCount() != null ? elementDTO.getRequiredCount() : 1);
        element.setIsOptional(elementDTO.getIsOptional() != null ? elementDTO.getIsOptional() : false);
        element.setCountsTowardsParent(elementDTO.getCountsTowardsParent() != null ? elementDTO.getCountsTowardsParent() : true);

        // Update parent element if provided
        if (elementDTO.getParentElementId() != null) {
            PathwayElement parentElement = pathwayElementRepository.findById(elementDTO.getParentElementId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent element not found"));
            element.setParentElement(parentElement);
        } else {
            element.setParentElement(null);
        }

        PathwayElement updatedElement = pathwayElementRepository.save(element);
        return convertToDTO(updatedElement);
    }

    @Override
    public void deletePathwayElement(Long elementId, Long pathwayId, Long organizationId) {
        PathwayElement element = pathwayElementRepository.findById(elementId)
                .orElseThrow(() -> new ResourceNotFoundException("Pathway element not found"));

        // Check if element belongs to the pathway and organization
        if (!element.getPathway().getId().equals(pathwayId) || 
            !element.getPathway().getOrganization().getId().equals(organizationId)) {
            throw new UnauthorizedException("Pathway element does not belong to this organization");
        }

        pathwayElementRepository.delete(element);
    }

    @Override
    public void addBadgeRequirement(Long elementId, Long badgeClassId, Long pathwayId, Long organizationId) {
        // This method is deprecated - use PathwayElementBadgeService instead
        throw new UnsupportedOperationException("Use PathwayElementBadgeService.addBadgeToElement() instead");
    }

    @Override
    public void removeBadgeRequirement(Long elementId, Long badgeClassId, Long pathwayId, Long organizationId) {
        // This method is deprecated - use PathwayElementBadgeService instead
        throw new UnsupportedOperationException("Use PathwayElementBadgeService.removeBadgeFromElement() instead");
    }

    @Override
    public Integer getNextOrderIndex(Long pathwayId) {
        return pathwayElementRepository.findNextOrderIndex(pathwayId);
    }

    @Override
    public boolean pathwayElementExistsAndBelongsToOrganization(Long elementId, Long pathwayId, Long organizationId) {
        Optional<PathwayElement> element = pathwayElementRepository.findById(elementId);
        return element.isPresent() && 
               element.get().getPathway().getId().equals(pathwayId) && 
               element.get().getPathway().getOrganization().getId().equals(organizationId);
    }

    @Override
    public PathwayElementDTO createPathwayElementForCurrentUser(PathwayElementDTO elementDTO, Long pathwayId, String userEmail) {
        // Get user's organization
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        List<OrganizationStaff> staffList = organizationStaffRepository.findByUserId(user.getId());
        if (staffList.isEmpty()) {
            throw new UnauthorizedException("User is not associated with any organization");
        }
        
        // For now, use the first organization. In the future, you might want to allow selection
        Organization organization = staffList.get(0).getOrganization();
        
        return createPathwayElement(elementDTO, pathwayId, organization.getId());
    }

    @Override
    public List<PathwayElementDTO> getPathwayElementsForCurrentUser(Long pathwayId, String userEmail) {
        // Get user's organization
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        List<OrganizationStaff> staffList = organizationStaffRepository.findByUserId(user.getId());
        if (staffList.isEmpty()) {
            return List.of(); // Return empty list if no organization
        }
        
        // For now, use the first organization. In the future, you might want to return all organizations
        Organization organization = staffList.get(0).getOrganization();
        
        return getPathwayElements(pathwayId, organization.getId());
    }

    @Override
    public Optional<PathwayElementDTO> getPathwayElementByIdForCurrentUser(Long elementId, Long pathwayId, String userEmail) {
        // Get user's organization
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        List<OrganizationStaff> staffList = organizationStaffRepository.findByUserId(user.getId());
        if (staffList.isEmpty()) {
            return Optional.empty();
        }
        
        // For now, use the first organization. In the future, you might want to check all organizations
        Organization organization = staffList.get(0).getOrganization();
        
        return getPathwayElementById(elementId, pathwayId, organization.getId());
    }

    @Override
    public PathwayElementDTO updatePathwayElementForCurrentUser(Long elementId, PathwayElementDTO elementDTO, Long pathwayId, String userEmail) {
        // Get user's organization
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        List<OrganizationStaff> staffList = organizationStaffRepository.findByUserId(user.getId());
        if (staffList.isEmpty()) {
            throw new UnauthorizedException("User is not associated with any organization");
        }
        
        // For now, use the first organization. In the future, you might want to allow selection
        Organization organization = staffList.get(0).getOrganization();
        
        return updatePathwayElement(elementId, elementDTO, pathwayId, organization.getId());
    }

    @Override
    public void deletePathwayElementForCurrentUser(Long elementId, Long pathwayId, String userEmail) {
        // Get user's organization
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        List<OrganizationStaff> staffList = organizationStaffRepository.findByUserId(user.getId());
        if (staffList.isEmpty()) {
            throw new UnauthorizedException("User is not associated with any organization");
        }
        
        // For now, use the first organization. In the future, you might want to allow selection
        Organization organization = staffList.get(0).getOrganization();
        
        deletePathwayElement(elementId, pathwayId, organization.getId());
    }

    private PathwayElementDTO convertToDTO(PathwayElement element) {
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