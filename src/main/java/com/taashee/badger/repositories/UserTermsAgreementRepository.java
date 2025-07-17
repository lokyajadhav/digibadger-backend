package com.taashee.badger.repositories;

import com.taashee.badger.models.UserTermsAgreement;
import com.taashee.badger.models.User;
import com.taashee.badger.models.TermsOfService;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserTermsAgreementRepository extends JpaRepository<UserTermsAgreement, Long> {
    Optional<UserTermsAgreement> findByUserAndTerms(User user, TermsOfService terms);
    Optional<UserTermsAgreement> findTopByUserOrderByAgreedAtDesc(User user);
} 