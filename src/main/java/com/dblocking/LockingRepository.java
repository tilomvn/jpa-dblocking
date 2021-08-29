package com.dblocking;

import java.util.Optional;

import javax.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;

public interface LockingRepository extends JpaRepository<LockingEntity, Long>, JpaSpecificationExecutor<LockingEntity> {

    // @Lock(LockModeType.OPTIMISTIC)
    Optional<LockingEntity> findById(Long id);

    // @Lock(LockModeType.OPTIMISTIC)
    // Optional<LockingEntity> findById(Long Id);

}