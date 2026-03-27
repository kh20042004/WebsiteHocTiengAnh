package com.english12smart.repository;

import com.english12smart.entity.AdminActivityLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminActivityLogRepository extends MongoRepository<AdminActivityLog, String> {
}
