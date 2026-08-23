package com.training.microservices.storageservice.repository;

import com.training.microservices.storageservice.entity.StorageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StorageRepository extends JpaRepository<StorageEntity, Long> {
}
