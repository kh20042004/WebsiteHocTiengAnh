package com.english12smart.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

@Document(collection = "admin_activity_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminActivityLog {

    @Id
    private String id;

    private String adminId;
    private String action;
    private String targetType;
    private String targetId;
    private Map<String, Object> metadata;
    private Long createdAt;
}
