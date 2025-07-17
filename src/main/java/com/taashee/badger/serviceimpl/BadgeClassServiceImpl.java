package com.taashee.badger.serviceimpl;

import com.taashee.badger.models.BadgeClass;
import com.taashee.badger.repositories.BadgeClassRepository;
import com.taashee.badger.services.BadgeClassService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import com.taashee.badger.models.BadgeInstance;
import com.taashee.badger.models.BadgeInstanceAwardRequest;
import com.taashee.badger.repositories.BadgeInstanceRepository;

import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

@Service
public class BadgeClassServiceImpl implements BadgeClassService {
    @Autowired
    private BadgeClassRepository badgeClassRepository;

    @Autowired
    private BadgeInstanceRepository badgeInstanceRepository;

    @Override
    public BadgeClass createBadgeClass(BadgeClass badgeClass) {
        return badgeClassRepository.save(badgeClass);
    }

    @Override
    public BadgeClass updateBadgeClass(Long id, BadgeClass badgeClass) {
        badgeClass.setId(id);
        return badgeClassRepository.save(badgeClass);
    }

    @Override
    public void deleteBadgeClass(Long id) {
        badgeClassRepository.deleteById(id);
    }

    @Override
    public Optional<BadgeClass> getBadgeClassById(Long id) {
        return badgeClassRepository.findById(id);
    }

    @Override
    public List<BadgeClass> getAllBadgeClasses() {
        return badgeClassRepository.findAll();
    }

    @Override
    public BadgeClass archiveBadgeClass(Long id, boolean archive) {
        BadgeClass badgeClass = badgeClassRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("BadgeClass not found"));
        badgeClass.setArchived(archive);
        return badgeClassRepository.save(badgeClass);
    }

    @Override
    public List<BadgeInstance> awardEnrollments(Long badgeClassId, List<BadgeInstanceAwardRequest> requests) {
        BadgeClass badgeClass = badgeClassRepository.findById(badgeClassId)
            .orElseThrow(() -> new RuntimeException("BadgeClass not found"));
        List<BadgeInstance> awarded = new ArrayList<>();
        for (BadgeInstanceAwardRequest req : requests) {
            BadgeInstance instance = new BadgeInstance();
            instance.setBadgeClass(badgeClass);
            instance.setRecipientIdentifier(req.getRecipientIdentifier());
            instance.setRecipientType(req.getRecipientType());
            instance.setAwardType(req.getAwardType());
            instance.setNarrative(req.getNarrative());
            // Set other fields as needed (e.g., issuedOn, issuer, etc.)
            awarded.add(badgeInstanceRepository.save(instance));
        }
        return awarded;
    }

    @Override
    public List<BadgeClass> bulkArchiveBadgeClasses(List<Long> ids, boolean archive) {
        List<BadgeClass> badgeClasses = badgeClassRepository.findAllById(ids);
        for (BadgeClass bc : badgeClasses) {
            bc.setArchived(archive);
        }
        return badgeClassRepository.saveAll(badgeClasses);
    }

    @Override
    public void bulkDeleteBadgeClasses(List<Long> ids) {
        badgeClassRepository.deleteAllById(ids);
    }
} 