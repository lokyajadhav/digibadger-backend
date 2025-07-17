package com.taashee.badger.repositories;

import com.taashee.badger.models.IssuerStaff;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface IssuerStaffRepository extends JpaRepository<IssuerStaff, Long> {
    List<IssuerStaff> findByIssuerId(Long issuerId);
    List<IssuerStaff> findByUserId(Long userId);
    void deleteByIssuerId(Long issuerId);
} 