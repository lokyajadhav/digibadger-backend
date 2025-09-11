package com.taashee.badger.models;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.taashee.badger.models.PathwayStep;

@Entity
public class StepPrerequisite {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "step_id", nullable = false)
    @JsonBackReference
    private PathwayStep step;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prerequisite_step_id", nullable = false)
    @JsonBackReference
    private PathwayStep prerequisiteStep;

    public Long getId() { return id; }
    public PathwayStep getStep() { return step; }
    public PathwayStep getPrerequisiteStep() { return prerequisiteStep; }

    public void setId(Long id) { this.id = id; }
    public void setStep(PathwayStep step) { this.step = step; }
    public void setPrerequisiteStep(PathwayStep prerequisiteStep) { this.prerequisiteStep = prerequisiteStep; }
}


