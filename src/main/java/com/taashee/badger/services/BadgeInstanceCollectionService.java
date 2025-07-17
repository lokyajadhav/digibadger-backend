package com.taashee.badger.services;

import com.taashee.badger.models.BadgeInstanceCollection;
import com.taashee.badger.models.BadgeInstance;
import com.taashee.badger.models.User;
import com.taashee.badger.repositories.BadgeInstanceCollectionRepository;
import com.taashee.badger.repositories.BadgeInstanceRepository;
import com.taashee.badger.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class BadgeInstanceCollectionService {
    @Autowired
    private BadgeInstanceCollectionRepository collectionRepository;
    @Autowired
    private BadgeInstanceRepository badgeInstanceRepository;
    @Autowired
    private UserRepository userRepository;

    public List<BadgeInstanceCollection> getCollectionsForUser(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        return userOpt.map(collectionRepository::findByUser).orElse(List.of());
    }

    public Optional<BadgeInstanceCollection> getCollection(Long id) {
        return collectionRepository.findById(id);
    }

    public BadgeInstanceCollection createCollection(BadgeInstanceCollection collection) {
        return collectionRepository.save(collection);
    }

    public BadgeInstanceCollection updateCollection(BadgeInstanceCollection collection) {
        return collectionRepository.save(collection);
    }

    public void deleteCollection(Long id) {
        collectionRepository.deleteById(id);
    }

    public BadgeInstanceCollection addBadgesToCollection(Long collectionId, Set<Long> badgeInstanceIds) {
        BadgeInstanceCollection collection = collectionRepository.findById(collectionId).orElseThrow();
        Set<BadgeInstance> badges = badgeInstanceRepository.findAllById(badgeInstanceIds).stream().collect(java.util.stream.Collectors.toSet());
        collection.getBadgeInstances().addAll(badges);
        return collectionRepository.save(collection);
    }

    public BadgeInstanceCollection removeBadgesFromCollection(Long collectionId, Set<Long> badgeInstanceIds) {
        BadgeInstanceCollection collection = collectionRepository.findById(collectionId).orElseThrow();
        collection.getBadgeInstances().removeIf(bi -> badgeInstanceIds.contains(bi.getId()));
        return collectionRepository.save(collection);
    }
} 