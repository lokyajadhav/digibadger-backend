package com.taashee.badger.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

@Entity
@Table(name = "pathway_subscriptions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PathwaySubscription {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pathway_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Pathway pathway;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_group_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private RecipientGroup recipientGroup;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
} 