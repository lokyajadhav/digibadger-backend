package com.taashee.badger.services;

import com.taashee.badger.models.Endorsement;
import java.util.List;
import java.util.Optional;

public interface EndorsementService {
    Endorsement createEndorsement(Endorsement endorsement);
    List<Endorsement> getAllEndorsements();
    Optional<Endorsement> getEndorsementById(Long id);
    Endorsement updateEndorsement(Long id, Endorsement endorsement);
    void deleteEndorsement(Long id);
    Endorsement acceptEndorsement(Long id);
    Endorsement revokeEndorsement(Long id, String reason);
    Endorsement rejectEndorsement(Long id, String reason);
    Endorsement archiveEndorsement(Long id, boolean archive);
    List<Endorsement> bulkArchiveEndorsements(List<Long> ids, boolean archive);
    void bulkDeleteEndorsements(List<Long> ids);
} 