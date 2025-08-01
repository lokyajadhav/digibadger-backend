package com.taashee.badger.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

@Entity
@Table(name = "pathway_elements")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PathwayElement {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pathway_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Pathway pathway;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "badge_class_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private BadgeClass badgeClass;
    
    @Column(name = "element_type")
    private String elementType; // "badge", "sub_pathway"
    
    @Column(name = "order_index")
    private Integer orderIndex;
    
    @Column(name = "name")
    private String name;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
} 