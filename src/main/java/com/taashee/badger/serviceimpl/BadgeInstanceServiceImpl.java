package com.taashee.badger.serviceimpl;

import com.taashee.badger.models.BadgeInstance;
import com.taashee.badger.repositories.BadgeInstanceRepository;
import com.taashee.badger.services.BadgeInstanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BadgeInstanceServiceImpl implements BadgeInstanceService {
    @Autowired
    private BadgeInstanceRepository badgeInstanceRepository;

    @Override
    public BadgeInstance createBadgeInstance(BadgeInstance badgeInstance) {
        return badgeInstanceRepository.save(badgeInstance);
    }

    @Override
    public BadgeInstance updateBadgeInstance(Long id, BadgeInstance badgeInstance) {
        badgeInstance.setId(id);
        return badgeInstanceRepository.save(badgeInstance);
    }

    @Override
    public void deleteBadgeInstance(Long id) {
        badgeInstanceRepository.deleteById(id);
    }

    @Override
    public Optional<BadgeInstance> getBadgeInstanceById(Long id) {
        return badgeInstanceRepository.findById(id);
    }

    @Override
    public List<BadgeInstance> getAllBadgeInstances() {
        return badgeInstanceRepository.findAll();
    }

    @Override
    public List<BadgeInstance> getBadgesForRecipient(Long userId) {
        return badgeInstanceRepository.findByRecipientId(userId);
    }

    @Override
    public BadgeInstance getBadgeInstanceForRecipient(Long userId, Long badgeInstanceId) {
        return badgeInstanceRepository.findByIdAndRecipientId(badgeInstanceId, userId)
            .orElseThrow(() -> new RuntimeException("BadgeInstance not found for user"));
    }

    @Override
    public BadgeInstance archiveBadgeInstance(Long id, boolean archive) {
        BadgeInstance instance = badgeInstanceRepository.findById(id).orElseThrow(() -> new RuntimeException("BadgeInstance not found"));
        instance.setArchived(archive);
        return badgeInstanceRepository.save(instance);
    }

    @Override
    public List<BadgeInstance> bulkArchiveBadgeInstances(List<Long> ids, boolean archive) {
        List<BadgeInstance> instances = badgeInstanceRepository.findAllById(ids);
        for (BadgeInstance instance : instances) {
            instance.setArchived(archive);
        }
        return badgeInstanceRepository.saveAll(instances);
    }

    @Override
    public void bulkDeleteBadgeInstances(List<Long> ids) {
        badgeInstanceRepository.deleteAllById(ids);
    }

    @Override
    public List<BadgeInstance> revokeBadgeInstances(List<Long> ids, String revocationReason) {
        List<BadgeInstance> instances = badgeInstanceRepository.findAllById(ids);
        for (BadgeInstance instance : instances) {
            instance.setRevoked(true);
            instance.setRevocationReason(revocationReason);
        }
        return badgeInstanceRepository.saveAll(instances);
    }
} 