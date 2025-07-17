package com.taashee.badger.services;

import com.taashee.badger.models.IssuerStaff;
import java.util.List;

/**
 * Service for managing issuer-specific staff.
 * Author: Lokya Naik
 */
public interface IssuerStaffService {
    List<IssuerStaff> getStaffByIssuerId(Long issuerId);
    IssuerStaff addStaffToIssuer(Long issuerId, IssuerStaff staff);
    IssuerStaff updateStaff(Long staffId, IssuerStaff staff);
    void removeStaff(Long staffId);
    boolean userExists(String email);
    void removeStaffCompletely(Long issuerId, Long staffId, boolean fullDelete);
} 