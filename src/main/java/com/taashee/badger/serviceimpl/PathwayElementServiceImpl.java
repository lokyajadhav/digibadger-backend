package com.taashee.badger.serviceimpl;

import com.taashee.badger.models.*;
import com.taashee.badger.repositories.*;
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
    private BadgeClassRepository badgeClassRepository;

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
        element.setElementType(elementDTO.getElementType());
        element.setOrderIndex(nextOrderIndex);

        // Set badge class if provided
        if (elementDTO.getBadgeClassId() != null) {
            BadgeClass badgeClass = badgeClassRepository.findById(elementDTO.getBadgeClassId())
                    .orElseThrow(() -> new ResourceNotFoundException("Badge class not found"));
            element.setBadgeClass(badgeClass);
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
        element.setElementType(elementDTO.getElementType());

        // Update badge class if provided
        if (elementDTO.getBadgeClassId() != null) {
            BadgeClass badgeClass = badgeClassRepository.findById(elementDTO.getBadgeClassId())
                    .orElseThrow(() -> new ResourceNotFoundException("Badge class not found"));
            element.setBadgeClass(badgeClass);
        } else {
            element.setBadgeClass(null);
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
        // This would be implemented when we add badge requirements to pathway elements
        // For now, this is a placeholder
        throw new UnsupportedOperationException("Badge requirements not yet implemented");
    }

    @Override
    public void removeBadgeRequirement(Long elementId, Long badgeClassId, Long pathwayId, Long organizationId) {
        // This would be implemented when we add badge requirements to pathway elements
        // For now, this is a placeholder
        throw new UnsupportedOperationException("Badge requirements not yet implemented");
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

    private PathwayElementDTO convertToDTO(PathwayElement element) {
        PathwayElementDTO dto = new PathwayElementDTO();
        dto.setId(element.getId());
        dto.setPathwayId(element.getPathway().getId());
        dto.setElementType(element.getElementType());
        dto.setOrderIndex(element.getOrderIndex());
        dto.setName(element.getName());
        dto.setDescription(element.getDescription());
        dto.setCreatedAt(element.getCreatedAt());
        
        if (element.getBadgeClass() != null) {
            dto.setBadgeClassId(element.getBadgeClass().getId());
            dto.setBadgeClassName(element.getBadgeClass().getName());
        }
        
        return dto;
    }
} 