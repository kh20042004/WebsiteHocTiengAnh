package com.english12smart.service;

import com.english12smart.entity.AdminActivityLog;
import com.english12smart.repository.AdminActivityLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminActivityLogService {

    private final AdminActivityLogRepository adminActivityLogRepository;
    private final MongoTemplate mongoTemplate;

    public void record(String adminId, String action, String targetType, String targetId, Map<String, Object> metadata) {
        if (adminId == null || action == null) {
            return;
        }
        AdminActivityLog log = AdminActivityLog.builder()
                .adminId(adminId)
                .action(action)
                .targetType(targetType)
                .targetId(targetId)
                .metadata(metadata == null ? Map.of() : metadata)
                .createdAt(System.currentTimeMillis())
                .build();
        adminActivityLogRepository.save(log);
    }

    public void recordStatusChange(String adminId, String classroomId, String classroomName, String previousStatus, String newStatus) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("classroomName", classroomName);
        metadata.put("previousStatus", previousStatus);
        metadata.put("newStatus", newStatus);
        record(adminId, "UPDATE_CLASSROOM_STATUS", "CLASSROOM", classroomId, metadata);
    }

    public Page<AdminActivityLog> getRecentActivities(Pageable pageable) {
        return adminActivityLogRepository.findAll(pageable);
    }

    public List<ActionCount> summarizeRecentActions(Duration lookback) {
        long cutoff = Math.max(0, System.currentTimeMillis() - lookback.toMillis());
        MatchOperation match = Aggregation.match(Criteria.where("createdAt").gte(cutoff));
        Aggregation aggregation = Aggregation.newAggregation(
                match,
                Aggregation.group("action").count().as("count"),
                Aggregation.project("count").and("_id").as("action")
        );
        AggregationResults<ActionCount> results = mongoTemplate.aggregate(aggregation, AdminActivityLog.class, ActionCount.class);
        return results.getMappedResults();
    }

    public record ActionCount(String action, long count) {
    }
}
