package com.taashee.badger.serviceimpl;

import com.taashee.badger.models.PathwayElement;
import com.taashee.badger.models.PathwayElementBadge;
import com.taashee.badger.models.PathwayElementBadgeDTO;
import com.taashee.badger.models.BadgeClass;
import com.taashee.badger.repositories.PathwayElementBadgeRepository;
import com.taashee.badger.repositories.PathwayElementRepository;
import com.taashee.badger.repositories.BadgeClassRepository;
import com.taashee.badger.services.PathwayElementBadgeService;
import com.taashee.badger.exceptions.ResourceNotFoundException;
import com.taashee.badger.exceptions.UnauthorizedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class PathwayElementBadgeServiceImpl implements PathwayElementBadgeService {

    @Autowired
    private PathwayElementBadgeRepository pathwayElementBadgeRepository;

    @Autowired
    private PathwayElementRepository pathwayElementRepository;

    @Autowired
    private BadgeClassRepository badgeClassRepository;

    @Override
    public PathwayElementBadgeDTO addBadgeToElement(Long elementId, Long badgeClassId, Long organizationId, PathwayElementBadgeDTO badgeDTO) {
        // Validate element exists and belongs to organization
        PathwayElement element = pathwayElementRepository.findById(elementId)
                .orElseThrow(() -> new ResourceNotFoundException("Pathway element not found"));

        if (!element.getPathway().getOrganization().getId().equals(organizationId)) {
            throw new UnauthorizedException("Pathway element does not belong to this organization");
        }

        // Validate badge class exists
        BadgeClass badgeClass = badgeClassRepository.findById(badgeClassId)
                .orElseThrow(() -> new ResourceNotFoundException("Badge class not found"));

        // Check if relationship already exists
        if (pathwayElementBadgeRepository.existsByElementIdAndBadgeClassId(elementId, badgeClassId)) {
            throw new IllegalArgumentException("Badge is already associated with this element");
        }

        // Create new relationship
        PathwayElementBadge elementBadge = new PathwayElementBadge();
        elementBadge.setElement(element);
        elementBadge.setBadgeClass(badgeClass);
        elementBadge.setBadgeSource(badgeDTO.getBadgeSource() != null ? 
            PathwayElementBadge.BadgeSource.valueOf(badgeDTO.getBadgeSource().toUpperCase()) : 
            PathwayElementBadge.BadgeSource.BADGR);
        elementBadge.setExternalBadgeUrl(badgeDTO.getExternalBadgeUrl());
        // Convert String to Map if needed for external badge data
        if (badgeDTO.getExternalBadgeData() != null) {
            elementBadge.setExternalBadgeData(java.util.Map.of("data", badgeDTO.getExternalBadgeData()));
        }
        elementBadge.setIsRequired(badgeDTO.getIsRequired() != null ? badgeDTO.getIsRequired() : true);

        PathwayElementBadge savedElementBadge = pathwayElementBadgeRepository.save(elementBadge);
        return convertToDTO(savedElementBadge);
    }

    @Override
    public void removeBadgeFromElement(Long elementId, Long badgeClassId, Long organizationId) {
        // Validate element exists and belongs to organization
        PathwayElement element = pathwayElementRepository.findById(elementId)
                .orElseThrow(() -> new ResourceNotFoundException("Pathway element not found"));

        if (!element.getPathway().getOrganization().getId().equals(organizationId)) {
            throw new UnauthorizedException("Pathway element does not belong to this organization");
        }

        pathwayElementBadgeRepository.deleteByElementIdAndBadgeClassId(elementId, badgeClassId);
    }

    @Override
    public List<PathwayElementBadgeDTO> getBadgesForElement(Long elementId, Long organizationId) {
        // Validate element exists and belongs to organization
        PathwayElement element = pathwayElementRepository.findById(elementId)
                .orElseThrow(() -> new ResourceNotFoundException("Pathway element not found"));

        if (!element.getPathway().getOrganization().getId().equals(organizationId)) {
            throw new UnauthorizedException("Pathway element does not belong to this organization");
        }

        List<PathwayElementBadge> elementBadges = pathwayElementBadgeRepository.findByElementId(elementId);
        return elementBadges.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PathwayElementBadgeDTO> getRequiredBadgesForElement(Long elementId, Long organizationId) {
        // Validate element exists and belongs to organization
        PathwayElement element = pathwayElementRepository.findById(elementId)
                .orElseThrow(() -> new ResourceNotFoundException("Pathway element not found"));

        if (!element.getPathway().getOrganization().getId().equals(organizationId)) {
            throw new UnauthorizedException("Pathway element does not belong to this organization");
        }

        List<PathwayElementBadge> requiredBadges = pathwayElementBadgeRepository.findByElementIdAndIsRequiredTrue(elementId);
        return requiredBadges.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<PathwayElementBadgeDTO> getBadgeElementRelationship(Long elementId, Long badgeClassId, Long organizationId) {
        // Validate element exists and belongs to organization
        PathwayElement element = pathwayElementRepository.findById(elementId)
                .orElseThrow(() -> new ResourceNotFoundException("Pathway element not found"));

        if (!element.getPathway().getOrganization().getId().equals(organizationId)) {
            throw new UnauthorizedException("Pathway element does not belong to this organization");
        }

        Optional<PathwayElementBadge> elementBadge = pathwayElementBadgeRepository.findByElementIdAndBadgeClassId(elementId, badgeClassId);
        return elementBadge.map(this::convertToDTO);
    }

    @Override
    public PathwayElementBadgeDTO updateBadgeElementRelationship(Long elementId, Long badgeClassId, Long organizationId, PathwayElementBadgeDTO badgeDTO) {
        // Validate element exists and belongs to organization
        PathwayElement element = pathwayElementRepository.findById(elementId)
                .orElseThrow(() -> new ResourceNotFoundException("Pathway element not found"));

        if (!element.getPathway().getOrganization().getId().equals(organizationId)) {
            throw new UnauthorizedException("Pathway element does not belong to this organization");
        }

        PathwayElementBadge elementBadge = pathwayElementBadgeRepository.findByElementIdAndBadgeClassId(elementId, badgeClassId)
                .orElseThrow(() -> new ResourceNotFoundException("Badge-element relationship not found"));

        // Update fields
        if (badgeDTO.getBadgeSource() != null) {
            elementBadge.setBadgeSource(PathwayElementBadge.BadgeSource.valueOf(badgeDTO.getBadgeSource().toUpperCase()));
        }
        if (badgeDTO.getExternalBadgeUrl() != null) {
            elementBadge.setExternalBadgeUrl(badgeDTO.getExternalBadgeUrl());
        }
        if (badgeDTO.getExternalBadgeData() != null) {
            elementBadge.setExternalBadgeData(java.util.Map.of("data", badgeDTO.getExternalBadgeData()));
        }
        if (badgeDTO.getIsRequired() != null) {
            elementBadge.setIsRequired(badgeDTO.getIsRequired());
        }

        PathwayElementBadge updatedElementBadge = pathwayElementBadgeRepository.save(elementBadge);
        return convertToDTO(updatedElementBadge);
    }

    @Override
    public PathwayElementBadgeDTO addExternalBadgeToElement(Long elementId, Long organizationId, PathwayElementBadgeDTO externalBadgeDTO) {
        // Validate element exists and belongs to organization
        PathwayElement element = pathwayElementRepository.findById(elementId)
                .orElseThrow(() -> new ResourceNotFoundException("Pathway element not found"));

        if (!element.getPathway().getOrganization().getId().equals(organizationId)) {
            throw new UnauthorizedException("Pathway element does not belong to this organization");
        }

        // For external badges, we might not have a badge class ID
        // Create a placeholder or handle differently
        if (externalBadgeDTO.getBadgeClassId() == null) {
            throw new IllegalArgumentException("External badge must have a badge class ID");
        }

        // Validate badge class exists
        BadgeClass badgeClass = badgeClassRepository.findById(externalBadgeDTO.getBadgeClassId())
                .orElseThrow(() -> new ResourceNotFoundException("Badge class not found"));

        // Create external badge relationship
        PathwayElementBadge elementBadge = new PathwayElementBadge();
        elementBadge.setElement(element);
        elementBadge.setBadgeClass(badgeClass);
        elementBadge.setBadgeSource(externalBadgeDTO.getBadgeSource() != null ? 
            PathwayElementBadge.BadgeSource.valueOf(externalBadgeDTO.getBadgeSource().toUpperCase()) : 
            PathwayElementBadge.BadgeSource.EXTERNAL);
        elementBadge.setExternalBadgeUrl(externalBadgeDTO.getExternalBadgeUrl());
        if (externalBadgeDTO.getExternalBadgeData() != null) {
            elementBadge.setExternalBadgeData(java.util.Map.of("data", externalBadgeDTO.getExternalBadgeData()));
        }
        elementBadge.setIsRequired(externalBadgeDTO.getIsRequired() != null ? externalBadgeDTO.getIsRequired() : true);

        PathwayElementBadge savedElementBadge = pathwayElementBadgeRepository.save(elementBadge);
        return convertToDTO(savedElementBadge);
    }

    @Override
    public boolean isBadgeAssociatedWithElement(Long elementId, Long badgeClassId, Long organizationId) {
        // Validate element exists and belongs to organization
        PathwayElement element = pathwayElementRepository.findById(elementId)
                .orElseThrow(() -> new ResourceNotFoundException("Pathway element not found"));

        if (!element.getPathway().getOrganization().getId().equals(organizationId)) {
            throw new UnauthorizedException("Pathway element does not belong to this organization");
        }

        return pathwayElementBadgeRepository.existsByElementIdAndBadgeClassId(elementId, badgeClassId);
    }

    @Override
    public List<PathwayElementBadgeDTO> getBadgesForPathway(Long pathwayId, Long organizationId) {
        List<PathwayElementBadge> pathwayBadges = pathwayElementBadgeRepository.findByPathwayId(pathwayId);
        return pathwayBadges.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public long countRequiredBadgesForElement(Long elementId, Long organizationId) {
        // Validate element exists and belongs to organization
        PathwayElement element = pathwayElementRepository.findById(elementId)
                .orElseThrow(() -> new ResourceNotFoundException("Pathway element not found"));

        if (!element.getPathway().getOrganization().getId().equals(organizationId)) {
            throw new UnauthorizedException("Pathway element does not belong to this organization");
        }

        return pathwayElementBadgeRepository.countByElementIdAndIsRequiredTrue(elementId);
    }

    @Override
    public boolean validateBadgeElementRelationship(Long elementId, Long badgeClassId, Long organizationId) {
        try {
            // Validate element exists and belongs to organization
            PathwayElement element = pathwayElementRepository.findById(elementId)
                    .orElseThrow(() -> new ResourceNotFoundException("Pathway element not found"));

            if (!element.getPathway().getOrganization().getId().equals(organizationId)) {
                return false;
            }

            // Validate badge class exists
            BadgeClass badgeClass = badgeClassRepository.findById(badgeClassId)
                    .orElseThrow(() -> new ResourceNotFoundException("Badge class not found"));

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private PathwayElementBadgeDTO convertToDTO(PathwayElementBadge elementBadge) {
        PathwayElementBadgeDTO dto = new PathwayElementBadgeDTO();
        dto.setId(elementBadge.getId());
        dto.setElementId(elementBadge.getElement().getId());
        dto.setBadgeClassId(elementBadge.getBadgeClass().getId());
        dto.setBadgeClassName(elementBadge.getBadgeClass().getName());
        dto.setBadgeSource(elementBadge.getBadgeSource().name());
        dto.setExternalBadgeUrl(elementBadge.getExternalBadgeUrl());
        // Convert Map to String for DTO
        if (elementBadge.getExternalBadgeData() != null) {
            dto.setExternalBadgeData(elementBadge.getExternalBadgeData().get("data") != null ? 
                elementBadge.getExternalBadgeData().get("data").toString() : null);
        }
        dto.setIsRequired(elementBadge.getIsRequired());
        dto.setCreatedAt(elementBadge.getCreatedAt());
        
        // Additional badge information
        if (elementBadge.getBadgeClass() != null) {
            dto.setBadgeDescription(elementBadge.getBadgeClass().getDescription());
            dto.setBadgeImageUrl(elementBadge.getBadgeClass().getImage());
            dto.setBadgeCriteria(elementBadge.getBadgeClass().getCriteriaText());
        }
        
        return dto;
    }
} 