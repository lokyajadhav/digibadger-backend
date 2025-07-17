package com.taashee.badger.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "issuer_staff")
public class IssuerStaff {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issuer_id")
    @JsonIgnore
    private Issuer issuer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    @Column(nullable = false)
    private String staffRole; // e.g., owner, editor, staff

    @Column(nullable = false)
    private boolean isSigner = false;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Issuer getIssuer() { return issuer; }
    public void setIssuer(Issuer issuer) { this.issuer = issuer; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getStaffRole() { return staffRole; }
    public void setStaffRole(String staffRole) { this.staffRole = staffRole; }
    public boolean isSigner() { return isSigner; }
    public void setSigner(boolean isSigner) { this.isSigner = isSigner; }
} 