package com.taashee.badger.controllers;

import com.taashee.badger.models.BadgeClass;
import com.taashee.badger.models.Issuer;
import com.taashee.badger.models.BadgeInstance;
import com.taashee.badger.models.ApiResponse;
import com.taashee.badger.models.BadgeInstanceCollection;
import com.taashee.badger.models.TermsOfService;
import com.taashee.badger.models.UserTermsAgreement;
import com.taashee.badger.models.User;
import com.taashee.badger.repositories.TermsOfServiceRepository;
import com.taashee.badger.repositories.UserTermsAgreementRepository;
import com.taashee.badger.repositories.UserRepository;
import com.taashee.badger.services.BadgeClassService;
import com.taashee.badger.services.IssuerService;
import com.taashee.badger.services.BadgeInstanceService;
import com.taashee.badger.services.BadgeInstanceCollectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Tag(name = "Public APIs", description = "Public APIs for badge, issuer, assertion, and collection discovery/validation. Author: Lokya Naik")
@RestController
@RequestMapping("/api/public")
public class PublicController {
    @Autowired
    private BadgeClassService badgeClassService;
    @Autowired
    private IssuerService issuerService;
    @Autowired
    private BadgeInstanceService badgeInstanceService;
    @Autowired
    private BadgeInstanceCollectionService badgeInstanceCollectionService;
    @Autowired
    private TermsOfServiceRepository termsOfServiceRepository;
    @Autowired
    private UserTermsAgreementRepository userTermsAgreementRepository;
    @Autowired
    private UserRepository userRepository;

    @Operation(summary = "Get public badge info", description = "Get public info for a badge class.")
    @GetMapping("/badges/{id}")
    public ResponseEntity<ApiResponse<BadgeClass>> getPublicBadge(@PathVariable Long id) {
        return badgeClassService.getBadgeClassById(id)
            .map(bc -> ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Success", bc, null)))
            .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(HttpStatus.NOT_FOUND.value(), "Badge not found", null, "Badge not found")));
    }

    @Operation(summary = "Get public issuer info", description = "Get public info for an issuer.")
    @GetMapping("/issuers/{id}")
    public ResponseEntity<ApiResponse<Issuer>> getPublicIssuer(@PathVariable Long id) {
        return issuerService.getIssuerById(id)
            .map(issuer -> ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Success", issuer, null)))
            .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(HttpStatus.NOT_FOUND.value(), "Issuer not found", null, "Issuer not found")));
    }

    @Operation(summary = "Get public assertion info", description = "Get public info for a badge assertion.")
    @GetMapping("/assertions/{id}")
    public ResponseEntity<ApiResponse<BadgeInstance>> getPublicAssertion(@PathVariable Long id) {
        return badgeInstanceService.getBadgeInstanceById(id)
            .map(bi -> ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Success", bi, null)))
            .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(HttpStatus.NOT_FOUND.value(), "Assertion not found", null, "Assertion not found")));
    }

    // Placeholder for collections (implement if collection entity exists)
    @Operation(summary = "Get public collection info", description = "Get public info for a badge collection.")
    @GetMapping("/collections/{id}")
    public ResponseEntity<ApiResponse<BadgeInstanceCollection>> getPublicCollection(@PathVariable Long id) {
        return badgeInstanceCollectionService.getCollection(id)
            .filter(BadgeInstanceCollection::isPublic)
            .map(col -> ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Success", col, null)))
            .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(HttpStatus.NOT_FOUND.value(), "Collection not found or not public", null, "Collection not found or not public")));
    }

    @Operation(summary = "Validate public badge", description = "Validate a badge class (dummy implementation).")
    @GetMapping("/badges/{id}/validate")
    public ResponseEntity<ApiResponse<String>> validatePublicBadge(@PathVariable Long id) {
        // Implement real validation logic as needed
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Badge is valid", "VALID", null));
    }

    @Operation(summary = "Get share URL for a badge", description = "Get a provider-specific share URL for a badge public page (Facebook, LinkedIn supported).")
    @GetMapping("/share/badge/{id}")
    public ResponseEntity<ApiResponse<String>> getBadgeShareUrl(@PathVariable Long id, @RequestParam String provider) {
        String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
        String badgeUrl = baseUrl + "/public/badges/" + id;
        String shareUrl;
        switch (provider.toLowerCase()) {
            case "facebook":
                shareUrl = "https://www.facebook.com/sharer/sharer.php?u=" + java.net.URLEncoder.encode(badgeUrl, java.nio.charset.StandardCharsets.UTF_8);
                break;
            case "linkedin":
                shareUrl = "https://www.linkedin.com/shareArticle?mini=true&url=" + java.net.URLEncoder.encode(badgeUrl, java.nio.charset.StandardCharsets.UTF_8) + "&title=Check%20out%20this%20badge";
                break;
            default:
                return ResponseEntity.badRequest().body(new ApiResponse<>(400, "Unsupported provider", null, "Provider not supported"));
        }
        // Optionally, check if badge exists
        if (!badgeClassService.getBadgeClassById(id).isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(404, "Badge not found", null, "Badge not found"));
        }
        return ResponseEntity.ok(new ApiResponse<>(200, "Success", shareUrl, null));
    }

    @Operation(summary = "Get latest Terms of Service", description = "Get the latest Terms of Service by type. Author: Lokya Naik")
    @GetMapping("/public/terms")
    public ResponseEntity<ApiResponse<TermsOfService>> getLatestTerms(@RequestParam(defaultValue = "GENERAL") String type) {
        TermsOfService.Type tosType;
        try {
            tosType = TermsOfService.Type.valueOf(type.toUpperCase());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(400, "Invalid type", null, null));
        }
        TermsOfService tos = termsOfServiceRepository.findTopByTypeOrderByVersionDesc(tosType);
        if (tos == null) {
            return ResponseEntity.status(404).body(new ApiResponse<>(404, "No ToS found", null, null));
        }
        return ResponseEntity.ok(new ApiResponse<>(200, "Success", tos, null));
    }

    @Operation(summary = "Get user's latest ToS agreement", description = "Get the latest ToS agreement for a user. Author: Lokya Naik")
    @GetMapping("/public/terms/agreement")
    public ResponseEntity<ApiResponse<UserTermsAgreement>> getUserLatestAgreement(@RequestParam Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return ResponseEntity.status(404).body(new ApiResponse<>(404, "User not found", null, null));
        }
        UserTermsAgreement agreement = userTermsAgreementRepository.findTopByUserOrderByAgreedAtDesc(user).orElse(null);
        if (agreement == null) {
            return ResponseEntity.status(404).body(new ApiResponse<>(404, "No agreement found", null, null));
        }
        return ResponseEntity.ok(new ApiResponse<>(200, "Success", agreement, null));
    }
} 