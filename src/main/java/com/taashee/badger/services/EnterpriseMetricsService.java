package com.taashee.badger.services;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Gauge;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Slf4j
public class EnterpriseMetricsService {

    private final MeterRegistry meterRegistry;
    
    // Pathway Metrics
    private final Counter pathwayCreatedCounter;
    private final Counter pathwayPublishedCounter;
    private final Counter pathwayCompletedCounter;
    private final Timer pathwayCreationTimer;
    private final Timer pathwayPublishingTimer;
    private final AtomicInteger activePathwaysGauge;
    private final AtomicInteger publishedPathwaysGauge;
    
    // API Integration Metrics
    private final Counter apiSyncSuccessCounter;
    private final Counter apiSyncFailureCounter;
    private final Timer apiSyncTimer;
    private final Counter apiConnectionTestCounter;
    private final Counter apiConnectionFailureCounter;
    private final AtomicInteger activeApiConnectionsGauge;
    
    // Progress Tracking Metrics
    private final Counter progressUpdateCounter;
    private final Counter progressSyncSuccessCounter;
    private final Counter progressSyncFailureCounter;
    private final Timer progressSyncTimer;
    private final AtomicLong totalProgressUpdatesGauge;
    
    // Badge Integration Metrics
    private final Counter badgeIssuedCounter;
    private final Counter badgeVerificationSuccessCounter;
    private final Counter badgeVerificationFailureCounter;
    private final Timer badgeIssuanceTimer;
    private final Timer badgeVerificationTimer;
    
    // Real-time Update Metrics
    private final Counter realtimeUpdateSentCounter;
    private final Counter realtimeUpdateReceivedCounter;
    private final Counter realtimeUpdateFailureCounter;
    private final Timer realtimeUpdateTimer;
    
    // Course/Assignment Integration Metrics
    private final Counter courseSyncSuccessCounter;
    private final Counter courseSyncFailureCounter;
    private final Counter assignmentSyncSuccessCounter;
    private final Counter assignmentSyncFailureCounter;
    private final Timer courseSyncTimer;
    private final Timer assignmentSyncTimer;
    
    // Error and Performance Metrics
    private final Counter errorCounter;
    private final Counter timeoutCounter;
    private final Timer requestProcessingTimer;
    private final AtomicInteger concurrentRequestsGauge;

    @Autowired
    public EnterpriseMetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        
        // Initialize Pathway Metrics
        this.pathwayCreatedCounter = Counter.builder("badger.pathway.created")
            .description("Number of pathways created")
            .register(meterRegistry);
            
        this.pathwayPublishedCounter = Counter.builder("badger.pathway.published")
            .description("Number of pathways published")
            .register(meterRegistry);
            
        this.pathwayCompletedCounter = Counter.builder("badger.pathway.completed")
            .description("Number of pathways completed by students")
            .register(meterRegistry);
            
        this.pathwayCreationTimer = Timer.builder("badger.pathway.creation.time")
            .description("Time taken to create pathways")
            .register(meterRegistry);
            
        this.pathwayPublishingTimer = Timer.builder("badger.pathway.publishing.time")
            .description("Time taken to publish pathways")
            .register(meterRegistry);
            
        this.activePathwaysGauge = new AtomicInteger(0);
        Gauge.builder("badger.pathway.active.count", activePathwaysGauge, AtomicInteger::get)
            .description("Number of active pathways")
            .register(meterRegistry);
            
        this.publishedPathwaysGauge = new AtomicInteger(0);
        Gauge.builder("badger.pathway.published.count", publishedPathwaysGauge, AtomicInteger::get)
            .description("Number of published pathways")
            .register(meterRegistry);
        
        // Initialize API Integration Metrics
        this.apiSyncSuccessCounter = Counter.builder("badger.api.sync.success")
            .description("Number of successful API sync operations")
            .register(meterRegistry);
            
        this.apiSyncFailureCounter = Counter.builder("badger.api.sync.failure")
            .description("Number of failed API sync operations")
            .register(meterRegistry);
            
        this.apiSyncTimer = Timer.builder("badger.api.sync.time")
            .description("Time taken for API sync operations")
            .register(meterRegistry);
            
        this.apiConnectionTestCounter = Counter.builder("badger.api.connection.test")
            .description("Number of API connection tests")
            .register(meterRegistry);
            
        this.apiConnectionFailureCounter = Counter.builder("badger.api.connection.failure")
            .description("Number of API connection failures")
            .register(meterRegistry);
            
        this.activeApiConnectionsGauge = new AtomicInteger(0);
        Gauge.builder("badger.api.connection.active.count", activeApiConnectionsGauge, AtomicInteger::get)
            .description("Number of active API connections")
            .register(meterRegistry);
        
        // Initialize Progress Tracking Metrics
        this.progressUpdateCounter = Counter.builder("badger.progress.update")
            .description("Number of progress updates")
            .register(meterRegistry);
            
        this.progressSyncSuccessCounter = Counter.builder("badger.progress.sync.success")
            .description("Number of successful progress sync operations")
            .register(meterRegistry);
            
        this.progressSyncFailureCounter = Counter.builder("badger.progress.sync.failure")
            .description("Number of failed progress sync operations")
            .register(meterRegistry);
            
        this.progressSyncTimer = Timer.builder("badger.progress.sync.time")
            .description("Time taken for progress sync operations")
            .register(meterRegistry);
            
        this.totalProgressUpdatesGauge = new AtomicLong(0);
        Gauge.builder("badger.progress.total.updates", totalProgressUpdatesGauge, AtomicLong::get)
            .description("Total number of progress updates")
            .register(meterRegistry);
        
        // Initialize Badge Integration Metrics
        this.badgeIssuedCounter = Counter.builder("badger.badge.issued")
            .description("Number of badges issued")
            .register(meterRegistry);
            
        this.badgeVerificationSuccessCounter = Counter.builder("badger.badge.verification.success")
            .description("Number of successful badge verifications")
            .register(meterRegistry);
            
        this.badgeVerificationFailureCounter = Counter.builder("badger.badge.verification.failure")
            .description("Number of failed badge verifications")
            .register(meterRegistry);
            
        this.badgeIssuanceTimer = Timer.builder("badger.badge.issuance.time")
            .description("Time taken to issue badges")
            .register(meterRegistry);
            
        this.badgeVerificationTimer = Timer.builder("badger.badge.verification.time")
            .description("Time taken to verify badges")
            .register(meterRegistry);
        
        // Initialize Real-time Update Metrics
        this.realtimeUpdateSentCounter = Counter.builder("badger.realtime.update.sent")
            .description("Number of real-time updates sent")
            .register(meterRegistry);
            
        this.realtimeUpdateReceivedCounter = Counter.builder("badger.realtime.update.received")
            .description("Number of real-time updates received")
            .register(meterRegistry);
            
        this.realtimeUpdateFailureCounter = Counter.builder("badger.realtime.update.failure")
            .description("Number of failed real-time updates")
            .register(meterRegistry);
            
        this.realtimeUpdateTimer = Timer.builder("badger.realtime.update.time")
            .description("Time taken for real-time updates")
            .register(meterRegistry);
        
        // Initialize Course/Assignment Integration Metrics
        this.courseSyncSuccessCounter = Counter.builder("badger.course.sync.success")
            .description("Number of successful course sync operations")
            .register(meterRegistry);
            
        this.courseSyncFailureCounter = Counter.builder("badger.course.sync.failure")
            .description("Number of failed course sync operations")
            .register(meterRegistry);
            
        this.assignmentSyncSuccessCounter = Counter.builder("badger.assignment.sync.success")
            .description("Number of successful assignment sync operations")
            .register(meterRegistry);
            
        this.assignmentSyncFailureCounter = Counter.builder("badger.assignment.sync.failure")
            .description("Number of failed assignment sync operations")
            .register(meterRegistry);
            
        this.courseSyncTimer = Timer.builder("badger.course.sync.time")
            .description("Time taken for course sync operations")
            .register(meterRegistry);
            
        this.assignmentSyncTimer = Timer.builder("badger.assignment.sync.time")
            .description("Time taken for assignment sync operations")
            .register(meterRegistry);
        
        // Initialize Error and Performance Metrics
        this.errorCounter = Counter.builder("badger.error.total")
            .description("Total number of errors")
            .register(meterRegistry);
            
        this.timeoutCounter = Counter.builder("badger.timeout.total")
            .description("Total number of timeouts")
            .register(meterRegistry);
            
        this.requestProcessingTimer = Timer.builder("badger.request.processing.time")
            .description("Time taken to process requests")
            .register(meterRegistry);
            
        this.concurrentRequestsGauge = new AtomicInteger(0);
        Gauge.builder("badger.request.concurrent.count", concurrentRequestsGauge, AtomicInteger::get)
            .description("Number of concurrent requests")
            .register(meterRegistry);
    }

    // Pathway Metrics Methods
    public void incrementPathwayCreated() {
        pathwayCreatedCounter.increment();
        activePathwaysGauge.incrementAndGet();
    }

    public void incrementPathwayPublished() {
        pathwayPublishedCounter.increment();
        publishedPathwaysGauge.incrementAndGet();
    }

    public void incrementPathwayCompleted() {
        pathwayCompletedCounter.increment();
    }

    public Timer.Sample startPathwayCreationTimer() {
        return Timer.start(meterRegistry);
    }

    public void stopPathwayCreationTimer(Timer.Sample sample) {
        sample.stop(pathwayCreationTimer);
    }

    public Timer.Sample startPathwayPublishingTimer() {
        return Timer.start(meterRegistry);
    }

    public void stopPathwayPublishingTimer(Timer.Sample sample) {
        sample.stop(pathwayPublishingTimer);
    }

    public void setActivePathwaysCount(int count) {
        activePathwaysGauge.set(count);
    }

    public void setPublishedPathwaysCount(int count) {
        publishedPathwaysGauge.set(count);
    }

    // API Integration Metrics Methods
    public void incrementApiSyncSuccess() {
        apiSyncSuccessCounter.increment();
    }

    public void incrementApiSyncFailure() {
        apiSyncFailureCounter.increment();
    }

    public Timer.Sample startApiSyncTimer() {
        return Timer.start(meterRegistry);
    }

    public void stopApiSyncTimer(Timer.Sample sample) {
        sample.stop(apiSyncTimer);
    }

    public void incrementApiConnectionTest() {
        apiConnectionTestCounter.increment();
    }

    public void incrementApiConnectionFailure() {
        apiConnectionFailureCounter.increment();
    }

    public void setActiveApiConnectionsCount(int count) {
        activeApiConnectionsGauge.set(count);
    }

    // Progress Tracking Metrics Methods
    public void incrementProgressUpdate() {
        progressUpdateCounter.increment();
        totalProgressUpdatesGauge.incrementAndGet();
    }

    public void incrementProgressSyncSuccess() {
        progressSyncSuccessCounter.increment();
    }

    public void incrementProgressSyncFailure() {
        progressSyncFailureCounter.increment();
    }

    public Timer.Sample startProgressSyncTimer() {
        return Timer.start(meterRegistry);
    }

    public void stopProgressSyncTimer(Timer.Sample sample) {
        sample.stop(progressSyncTimer);
    }

    // Badge Integration Metrics Methods
    public void incrementBadgeIssued() {
        badgeIssuedCounter.increment();
    }

    public void incrementBadgeVerificationSuccess() {
        badgeVerificationSuccessCounter.increment();
    }

    public void incrementBadgeVerificationFailure() {
        badgeVerificationFailureCounter.increment();
    }

    public Timer.Sample startBadgeIssuanceTimer() {
        return Timer.start(meterRegistry);
    }

    public void stopBadgeIssuanceTimer(Timer.Sample sample) {
        sample.stop(badgeIssuanceTimer);
    }

    public Timer.Sample startBadgeVerificationTimer() {
        return Timer.start(meterRegistry);
    }

    public void stopBadgeVerificationTimer(Timer.Sample sample) {
        sample.stop(badgeVerificationTimer);
    }

    // Real-time Update Metrics Methods
    public void incrementRealtimeUpdateSent() {
        realtimeUpdateSentCounter.increment();
    }

    public void incrementRealtimeUpdateReceived() {
        realtimeUpdateReceivedCounter.increment();
    }

    public void incrementRealtimeUpdateFailure() {
        realtimeUpdateFailureCounter.increment();
    }

    public Timer.Sample startRealtimeUpdateTimer() {
        return Timer.start(meterRegistry);
    }

    public void stopRealtimeUpdateTimer(Timer.Sample sample) {
        sample.stop(realtimeUpdateTimer);
    }

    // Course/Assignment Integration Metrics Methods
    public void incrementCourseSyncSuccess() {
        courseSyncSuccessCounter.increment();
    }

    public void incrementCourseSyncFailure() {
        courseSyncFailureCounter.increment();
    }

    public void incrementAssignmentSyncSuccess() {
        assignmentSyncSuccessCounter.increment();
    }

    public void incrementAssignmentSyncFailure() {
        assignmentSyncFailureCounter.increment();
    }

    public Timer.Sample startCourseSyncTimer() {
        return Timer.start(meterRegistry);
    }

    public void stopCourseSyncTimer(Timer.Sample sample) {
        sample.stop(courseSyncTimer);
    }

    public Timer.Sample startAssignmentSyncTimer() {
        return Timer.start(meterRegistry);
    }

    public void stopAssignmentSyncTimer(Timer.Sample sample) {
        sample.stop(assignmentSyncTimer);
    }

    // Error and Performance Metrics Methods
    public void incrementError() {
        errorCounter.increment();
    }

    public void incrementTimeout() {
        timeoutCounter.increment();
    }

    public Timer.Sample startRequestProcessingTimer() {
        return Timer.start(meterRegistry);
    }

    public void stopRequestProcessingTimer(Timer.Sample sample) {
        sample.stop(requestProcessingTimer);
    }

    public void incrementConcurrentRequests() {
        concurrentRequestsGauge.incrementAndGet();
    }

    public void decrementConcurrentRequests() {
        concurrentRequestsGauge.decrementAndGet();
    }

    // Utility Methods
    public void recordApiSyncMetrics(boolean success, long durationMs) {
        if (success) {
            incrementApiSyncSuccess();
        } else {
            incrementApiSyncFailure();
        }
        
        Timer.Sample sample = startApiSyncTimer();
        sample.stop(Timer.builder("badger.api.sync.duration")
            .tag("success", String.valueOf(success))
            .register(meterRegistry));
    }

    public void recordProgressSyncMetrics(boolean success, long durationMs) {
        if (success) {
            incrementProgressSyncSuccess();
        } else {
            incrementProgressSyncFailure();
        }
        
        Timer.Sample sample = startProgressSyncTimer();
        sample.stop(Timer.builder("badger.progress.sync.duration")
            .tag("success", String.valueOf(success))
            .register(meterRegistry));
    }

    public void recordBadgeOperationMetrics(String operation, boolean success, long durationMs) {
        if ("issue".equals(operation)) {
            if (success) {
                incrementBadgeIssued();
            }
        } else if ("verify".equals(operation)) {
            if (success) {
                incrementBadgeVerificationSuccess();
            } else {
                incrementBadgeVerificationFailure();
            }
        }
        
        Timer.Sample sample = startRequestProcessingTimer();
        sample.stop(Timer.builder("badger.badge.operation.duration")
            .tag("operation", operation)
            .tag("success", String.valueOf(success))
            .register(meterRegistry));
    }
} 