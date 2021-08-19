package com.dblocking;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
@TestInstance(Lifecycle.PER_CLASS)
public class DbLockingTests {

    @Autowired
    LockingRepository lockingRepository;

    @PersistenceContext
    EntityManager entityManager;

    @BeforeAll
    void init() {
        lockingRepository.save(new LockingEntity(1L, "Name1"));
        lockingRepository.save(new LockingEntity(2L, "Name2"));
    }

    @Test
    void testPessimiticLockException() throws Exception {
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

    //@Test
    void testOptimisticLockException() throws Exception {
        LockingEntity entity = lockingRepository.findById(1L).get();
        new Thread(() -> {
            try {
                LockingEntity e1 = lockingRepository.findById(1L).get();
                e1.setName("TEST1");
                e1.setVersion((short)0); //OptimisticLock using record version to detect locking failure
                System.out.println("First Transaction Version: " + e1.getVersion());

                lockingRepository.save(e1);
                System.out.println("DONE");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();
        Thread.sleep(3000);
        System.out.println("First Transaction Version: " + entity.getVersion());
    }
}