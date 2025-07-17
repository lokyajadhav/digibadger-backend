package com.taashee.badger.services;

import com.taashee.badger.models.Issuer;
import java.util.List;
import java.util.Optional;

public interface IssuerService {
    Issuer createIssuer(Issuer issuer);
    Issuer updateIssuer(Long id, Issuer issuer);
    void deleteIssuer(Long id);
    Optional<Issuer> getIssuerById(Long id);
    List<Issuer> getAllIssuers();
    Issuer archiveIssuer(Long id, boolean archive);
    List<Issuer> bulkArchiveIssuers(List<Long> ids, boolean archive);
    void bulkDeleteIssuers(List<Long> ids);
    // Add more methods as needed (e.g., archive, search, etc.)
} 