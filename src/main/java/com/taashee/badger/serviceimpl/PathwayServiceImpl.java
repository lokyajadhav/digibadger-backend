package com.taashee.badger.serviceimpl;

import com.taashee.badger.models.*;
import com.taashee.badger.repositories.*;
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

    @Override
    public PathwayDTO createPathway(PathwayDTO pathwayDTO, Long organizationId) {
        // Validate organization exists
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

        // Check if pathway name already exists for this organization
        if (pathwayRepository.existsByNameAndOrganizationId(pathwayDTO.getName(), organizationId)) {
            throw new IllegalArgumentException("Pathway with this name already exists in the organization");
        }

        // Create new pathway
        Pathway pathway = new Pathway();
        pathway.setName(pathwayDTO.getName());
        pathway.setDescription(pathwayDTO.getDescription());
        pathway.setOrganization(organization);
        pathway.setCompletionType(pathwayDTO.getCompletionType());

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
        pathway.setCompletionType(pathwayDTO.getCompletionType());

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
        Optional<PathwayProgress> existingProgress = pathwayProgressRepository.findByUserIdAndPathwayId(userId, pathwayId);
        if (existingProgress.isPresent()) {
            throw new IllegalArgumentException("User is already enrolled in this pathway");
        }

        // Create new progress entry
        PathwayProgress progress = new PathwayProgress();
        progress.setPathway(pathway);
        progress.setUser(user);
        progress.setProgressPercentage(BigDecimal.ZERO);
        progress.setCompletedElements(0);
        progress.setTotalElements((int) pathwayElementRepository.countByPathwayId(pathwayId));
        progress.setIsCompleted(false);

        pathwayProgressRepository.save(progress);
    }

    @Override
    public PathwayDTO getPathwayProgress(Long pathwayId, Long userId) {
        Pathway pathway = pathwayRepository.findById(pathwayId)
                .orElseThrow(() -> new ResourceNotFoundException("Pathway not found"));

        PathwayProgress progress = pathwayProgressRepository.findByUserIdAndPathwayId(userId, pathwayId)
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
        Optional<Pathway> pathway = pathwayRepository.findById(pathwayId);
        return pathway.isPresent() && pathway.get().getOrganization().getId().equals(organizationId);
    }

    private PathwayDTO convertToDTO(Pathway pathway) {
        PathwayDTO dto = new PathwayDTO();
        dto.setId(pathway.getId());
        dto.setName(pathway.getName());
        dto.setDescription(pathway.getDescription());
        dto.setOrganizationId(pathway.getOrganization().getId());
        dto.setOrganizationName(pathway.getOrganization().getNameEnglish());
        dto.setCompletionType(pathway.getCompletionType());
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