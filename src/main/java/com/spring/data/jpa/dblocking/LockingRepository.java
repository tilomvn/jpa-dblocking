package com.spring.data.jpa.dblocking;

import java.util.Optional;

import javax.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;

interface LockingRepository extends JpaRepository<LockingEntity, Long>, JpaSpecificationExecutor<LockingEntity> {

    //@Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<LockingEntity> findById(Long Id);

}