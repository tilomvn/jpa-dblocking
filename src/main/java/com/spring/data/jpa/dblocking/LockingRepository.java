package com.spring.data.jpa.dblocking;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.persistence.LockModeType;
import javax.persistence.QueryHint;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.QueryHints;

interface LockingRepository extends JpaRepository<LockingEntity, Long>, JpaSpecificationExecutor<LockingEntity> {

//    @Lock(LockModeType.PESSIMISTIC_WRITE)
//    @QueryHints({@QueryHint(name = "javax.persistence.lock.timeout", value = "3000")})
//	@Lock(LockModeType.OPTIMISTIC_FORCE_INCREMENT)
    Optional<LockingEntity> findById(Long id);

//    @Lock(LockModeType.PESSIMISTIC_WRITE)
//    @QueryHints({@QueryHint(name = "javax.persistence.lock.timeout", value = "3000")})
	@Lock(LockModeType.OPTIMISTIC_FORCE_INCREMENT)
    List<LockingEntity> findByIdIn(Collection<Long> ids);
}