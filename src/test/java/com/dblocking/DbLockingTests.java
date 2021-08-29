package com.dblocking;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.RequestParam;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;


@DataJpaTest
@TestInstance(Lifecycle.PER_CLASS)
public class DbLockingTests {

    Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    LockingRepository lockingRepository;

    @PersistenceContext
    EntityManager entityManager;

    @BeforeAll
    void init() {
        lockingRepository.save(new LockingEntity(1L, "Name1"));
        lockingRepository.save(new LockingEntity(2L, "Name2"));
    }

    // @Test
    void testPessimisticLockException() throws Exception {
        LockingEntity entity = lockingRepository.findById(1L).get();
        // lockingRepository.flush();
        // entityManager.refresh(entity);
        new Thread(() -> {
            try {
                LockingEntity e = lockingRepository.findById(1L).get();
                e.setName("name1");
                lockingRepository.save(e);
                System.out.println("DONE");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();
        Thread.sleep(8000);
    }

    @Test
    void testOptimisticLockException() throws Exception {
        LockingEntity entity = lockingRepository.findById(1L).get();
        new Thread(() -> {
            try {
                LockingEntity e1 = lockingRepository.findById(1L).get();
                e1.setName("TEST1");
                e1.setVersion((short) 2); // OptimisticLock using record version to detect locking failure
                log.info("Second Transaction Version: " + e1.getVersion());

                lockingRepository.save(e1);
                log.info("DONE");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();
        Thread.sleep(3000);
        entity.setName("name");
        lockingRepository.save(entity);
        log.info("First Transaction Version: " + entity.getVersion());
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