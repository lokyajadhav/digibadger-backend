package com.taashee.badger.repositories;

import com.taashee.badger.models.BadgeInstanceCollection;
import com.taashee.badger.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BadgeInstanceCollectionRepository extends JpaRepository<BadgeInstanceCollection, Long> {
    List<BadgeInstanceCollection> findByUser(User user);
} 