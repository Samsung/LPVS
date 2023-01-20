package com.lpvs.repository;

import com.lpvs.entity.config.WebhookConfig;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface QueueRepository extends CrudRepository<WebhookConfig, Long> {

    @Query(value = "SELECT * FROM soshub.lpvs_bot_queue", nativeQuery = true)
    List<WebhookConfig> getQueueList();
}
