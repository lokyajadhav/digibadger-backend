package com.taashee.badger.models;

import jakarta.persistence.*;

@Entity
public class StepRequirementRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "step_id", nullable = false)
    private PathwayStep step;

    @Enumerated(EnumType.STRING)
    private RuleType ruleType = RuleType.ALL;

    private Integer requiredCount;

    public enum RuleType {
        ALL,        // All requirements must be met
        N_OF_M      // N out of M requirements must be met
    }

    public Long getId() { return id; }
    public PathwayStep getStep() { return step; }
    public RuleType getRuleType() { return ruleType; }
    public Integer getRequiredCount() { return requiredCount; }

    public void setId(Long id) { this.id = id; }
    public void setStep(PathwayStep step) { this.step = step; }
    public void setRuleType(RuleType ruleType) { this.ruleType = ruleType; }
    public void setRequiredCount(Integer requiredCount) { this.requiredCount = requiredCount; }
}
