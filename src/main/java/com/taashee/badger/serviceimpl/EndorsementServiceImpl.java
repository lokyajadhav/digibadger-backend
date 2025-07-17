package com.taashee.badger.serviceimpl;

import com.taashee.badger.models.Endorsement;
import com.taashee.badger.repositories.EndorsementRepository;
import com.taashee.badger.services.EndorsementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EndorsementServiceImpl implements EndorsementService {
    @Autowired
    private EndorsementRepository endorsementRepository;

    @Override
    public Endorsement createEndorsement(Endorsement endorsement) {
        return endorsementRepository.save(endorsement);
    }

    @Override
    public List<Endorsement> getAllEndorsements() {
        return endorsementRepository.findAll();
    }

    @Override
    public Optional<Endorsement> getEndorsementById(Long id) {
        return endorsementRepository.findById(id);
    }

    @Override
    public Endorsement updateEndorsement(Long id, Endorsement endorsement) {
        Endorsement existing = endorsementRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Endorsement not found"));
        existing.setClaim(endorsement.getClaim());
        existing.setDescription(endorsement.getDescription());
        // Optionally update endorser/endorsee if allowed
        return endorsementRepository.save(existing);
    }

    @Override
    public void deleteEndorsement(Long id) {
        endorsementRepository.deleteById(id);
    }

    @Override
    public Endorsement acceptEndorsement(Long id) {
        Endorsement endorsement = endorsementRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Endorsement not found"));
        endorsement.setStatus(Endorsement.Status.ACCEPTED);
        return endorsementRepository.save(endorsement);
    }

    @Override
    public Endorsement revokeEndorsement(Long id, String reason) {
        Endorsement endorsement = endorsementRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Endorsement not found"));
        endorsement.setStatus(Endorsement.Status.REVOKED);
        endorsement.setRevocationReason(reason);
        return endorsementRepository.save(endorsement);
    }

    @Override
    public Endorsement rejectEndorsement(Long id, String reason) {
        Endorsement endorsement = endorsementRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Endorsement not found"));
        endorsement.setStatus(Endorsement.Status.REJECTED);
        endorsement.setRejectionReason(reason);
        return endorsementRepository.save(endorsement);
    }

    @Override
    public Endorsement archiveEndorsement(Long id, boolean archive) {
        Endorsement endorsement = endorsementRepository.findById(id).orElseThrow(() -> new RuntimeException("Endorsement not found"));
        endorsement.setArchived(archive);
        return endorsementRepository.save(endorsement);
    }

    @Override
    public List<Endorsement> bulkArchiveEndorsements(List<Long> ids, boolean archive) {
        List<Endorsement> endorsements = endorsementRepository.findAllById(ids);
        for (Endorsement endorsement : endorsements) {
            endorsement.setArchived(archive);
        }
        return endorsementRepository.saveAll(endorsements);
    }

    @Override
    public void bulkDeleteEndorsements(List<Long> ids) {
        endorsementRepository.deleteAllById(ids);
    }
} 