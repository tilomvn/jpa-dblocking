package com.spring.data.jpa.dblocking;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PessimisticLockException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import com.spring.data.jpa.dblocking.LockingEntity;
import com.spring.data.jpa.dblocking.LockingRepository;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.PessimisticLockingFailureException;

@DataJpaTest
@TestInstance(Lifecycle.PER_CLASS)
class DbLockingTests {

    Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    LockingRepository lockingRepository;

    @PersistenceContext
    EntityManager entityManager;

    boolean isDone1 = false;

    @BeforeAll
    void init() {
        lockingRepository.save(new LockingEntity(1L, "Name1"));
        lockingRepository.save(new LockingEntity(2L, "Name2"));
    }

    /**
     * https://blog.mimacom.com/testing-pessimistic-locking-handling-spring-boot-jpa/
     * Must use @Lock on method
     */
    //@Test
    void testPessimisticLockException() throws Exception {
        LockingEntity entity = lockingRepository.findById(1L).orElse(null);
        new Thread(() -> {
            assertThrows(PessimisticLockingFailureException.class, () -> {
                LockingEntity e = lockingRepository.findById(1L).orElse(null);
                e.setName("name1");
                lockingRepository.save(e);
            });
            isDone1 = true;
        }).start();
        Thread.sleep(8000);
        while(!isDone1) {} //wait for all tasks done.
    }

    /**
     * Support to nosql or rdbms
     */
    @Test
    void testOptimisticLockException() throws Exception {
        LockingEntity entity = lockingRepository.findById(1L).orElse(null);
        new Thread(() -> {
            try {
                LockingEntity e1 = lockingRepository.findById(1L).orElse(null);
                e1.setName("TEST1");
                lockingRepository.save(e1);
                e1 = lockingRepository.findById(1L).orElse(null);
                log.info("Second Transaction Version After Updated: " + e1.getVersion()); //1
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();
        Thread.sleep(5000);
        entity.setName("name");
        lockingRepository.save(entity);
        assertThrows(ObjectOptimisticLockingFailureException.class, () -> lockingRepository.flush());//commit
        log.info("First Transaction Version: " + entity.getVersion()); //0
    }

    //@Test
    void testExampleMatcherList() {
        List<LockingEntity> list = lockingRepository.findAll(valueIn("name", Arrays.asList("Name1")));
        list.forEach(e1 -> System.out.println(e1.getName()));
    }

    Specification<LockingEntity> valueIn(String field, Collection<String> values) {
        return new Specification<LockingEntity>() {
            @Override
            public Predicate toPredicate(Root<LockingEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                if (values.size() == 0) {
                    return null;
                }
                return cb.in(root.get(field)).value(values);
            }
        };
    }
}