package com.spring.data.jpa.dblocking;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import org.springframework.orm.jpa.JpaSystemException;

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

    Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    LockingRepository lockingRepository;

    @BeforeAll
    void init() {
        lockingRepository.save(new LockingEntity(1L, "Name1"));
        lockingRepository.save(new LockingEntity(2L, "Name2"));
        lockingRepository.save(new LockingEntity(3L, "Name3"));
    }

    /**
     * @Lock(LockModeType.PESSIMISTIC_WRITE)
     */
    boolean isDone1 = false;
//    @Test
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
//    @Test
    void testPessimisticLockException1() throws Exception {
        List<LockingEntity> entities = lockingRepository.findByIdIn(Arrays.asList(1L, 2L));
        new Thread(() -> {
            assertThrows(PessimisticLockingFailureException.class, () -> {
                LockingEntity e = lockingRepository.findById(2L).orElse(null);
                e.setName("name2");
                lockingRepository.save(e);
            });
            isDone1 = true;
        }).start();
        new Thread(() -> {
        	LockingEntity other = lockingRepository.findById(3L).orElse(null);
            log.info("Other record: " + other.getName());
        }).start();
        Thread.sleep(8000);
        while(!isDone1) {} //wait for all tasks done.
    }

    /**
     * Support to nosql or rdbms
     * @Lock None
     */
//    @Test
    void testOptimisticLockException() throws Exception {
    	log.info("Main Transaction Started...");
        LockingEntity entity = lockingRepository.findById(1L).orElse(null);
        new Thread(() -> {
        	log.info("Other Transaction Started...");
        	LockingEntity e1 = lockingRepository.findById(1L).orElse(null);
            log.info("Other Transaction's Version Before Update: " + e1.getVersion()); //0
            e1.setName("TEST1");
            e1 = lockingRepository.save(e1);
            log.info("Other Transaction's Version After Updated: " + e1.getVersion()); //1
            log.info("Other Transaction Ended...");
        }).start();
        Thread.sleep(5000);
        entity.setName("name");
        entity = lockingRepository.save(entity);
        assertThrows(ObjectOptimisticLockingFailureException.class, () -> lockingRepository.flush());//commit
        log.info("Main Transaction's Version After Tried To Commit: " + entity.getVersion()); //0
        log.info("Main Transaction Ended...");
    }
//    @Test
    void testOptimisticLockException1() throws Exception {
    	log.info("Main Transaction Started...");
        List<LockingEntity> entities = lockingRepository.findByIdIn(Arrays.asList(1L, 2L));
        entities.forEach(entity -> {
        	log.info("Main Transaction's Version Before Commit: " + entity.getId() + "-" + entity.getName() + "-" + entity.getVersion());
        });
        new Thread(() -> {
        	log.info("Other Transaction Started...");
//        	LockingEntity e1 = lockingRepository.findById(1L).orElse(null);
        	LockingEntity e1 = lockingRepository.findById(2L).orElse(null);
            log.info("Other Transaction's Version Before Update: " + e1.getVersion()); //0
            e1.setName("TEST1");
            e1 = lockingRepository.save(e1);
            log.info("Other Transaction's Version After Updated: " + e1.getVersion()); //1
            log.info("Other Transaction Ended...");
        }).start();
        Thread.sleep(5000);
        entities.forEach(entity -> {
        	entity.setName("name");
        });
        entities = lockingRepository.saveAll(entities);
        assertThrows(ObjectOptimisticLockingFailureException.class, () -> lockingRepository.flush());//commit
        entities.forEach(entity -> {
        	log.info("Main Transaction's Version After Tried To Commit: " + entity.getId() + "-" + entity.getName() + "-" + entity.getVersion());
        });
        log.info("Main Transaction Ended...");
    }
//    @Test
    void testOptimisticLockException1_Check() throws Exception {
    	log.info("Check Transaction Started...");
        LockingEntity entity1 = lockingRepository.findById(1L).orElse(null);
//        assertEquals("TEST1", entity1.getName());
//        assertEquals(1, entity1.getVersion());
        assertEquals("Name1", entity1.getName());
        assertEquals(0, entity1.getVersion());
        LockingEntity entity2 = lockingRepository.findById(2L).orElse(null);
//        assertEquals("Name2", entity2.getName());
//        assertEquals(0, entity2.getVersion());
        assertEquals("TEST1", entity2.getName());
        assertEquals(1, entity2.getVersion());
        log.info("Check Transaction Ended...");
    }
    /**
     * 	@Lock(LockModeType.OPTIMISTIC_FORCE_INCREMENT)
     */
//    @Test
    void testOptimisticLockException2() throws Exception {
    	log.info("Main Transaction Started...");
        LockingEntity entity = lockingRepository.findById(1L).orElse(null);
        log.info("Main Transaction's Version Before Update: " + entity.getId() + "-" + entity.getName() + "-" + entity.getVersion());
        new Thread(() -> assertThrows(JpaSystemException.class, () -> {
        	log.info("Other Transaction Started...");
        	LockingEntity e1 = lockingRepository.findById(2L).orElse(null);
            log.info("Other Transaction's Version Before Update: " + e1.getVersion());
            e1.setName("TEST1");
            e1 = lockingRepository.save(e1);
            log.info("Other Transaction's Version After Updated: " + e1.getVersion());
            log.info("Other Transaction Ended...");
        })).start();
        entity.setName("name");
        entity = lockingRepository.save(entity);
        lockingRepository.flush();//commit
        log.info("Main Transaction's Version After Updated: " + entity.getId() + "-" + entity.getName() + "-" + entity.getVersion());
        log.info("Main Transaction Ended...");
    }
    
    /**
     * Don't work because of effected lock on table ???
     * -> it works if enable Lock on lockingRepository.findById(...), but could not get expected result ???
     */
    boolean kickoff = false;
    @Test
    void testOptimisticLockException3() throws Exception {
    	new Thread(() -> {
    		log.info("Kickof: " + kickoff);
    	    while(!kickoff) {}
    	    log.info("Kickof: " + kickoff);
	    	assertThrows(JpaSystemException.class, () -> {
	        	log.info("Other Transaction Started...");
	        	LockingEntity e1 = lockingRepository.findById(3L).orElse(null);
	            log.info("Other Transaction's Version Before Update: " + e1.getId() + "-" + e1.getName() + "-" + e1.getVersion());
	            e1.setName("TEST1");
	            e1 = lockingRepository.save(e1);
	            log.info("Other Transaction's Version After Updated: " + e1.getId() + "-" + e1.getName() + "-" + e1.getVersion());
	            log.info("Other Transaction Ended...");
	        });
    	}).start();
    	Thread.sleep(1000);
    	log.info("Main Transaction Started...");
        List<LockingEntity> entities = lockingRepository.findByIdIn(Arrays.asList(1L, 2L));
        entities.forEach(entity -> {
        	log.info("Main Transaction's Version Before Update: " + entity.getId() + "-" + entity.getName() + "-" + entity.getVersion());
        });
        kickoff = true;
        entities.forEach(entity -> {
        	entity.setName("name");
        });
        entities = lockingRepository.saveAll(entities);
        lockingRepository.flush();//commit
        entities.forEach(entity -> {
        	log.info("Main Transaction's Version After Updated: " + entity.getId() + "-" + entity.getName() + "-" + entity.getVersion());
        });
        log.info("Main Transaction Ended...");
    }
    @Test
    void testOptimisticLockException3_Check() throws Exception {
    	log.info("Check Transaction Started...");
    	LockingEntity entity3 = lockingRepository.findById(3L).orElse(null);
        assertEquals("TEST1", entity3.getName());
        assertEquals(1, entity3.getVersion());
        
        LockingEntity entity1 = lockingRepository.findById(1L).orElse(null);
        assertEquals("name", entity1.getName());
        assertEquals(1, entity1.getVersion());
        
        LockingEntity entity2 = lockingRepository.findById(2L).orElse(null);
        assertEquals("name", entity2.getName());
        assertEquals(1, entity2.getVersion());
        log.info("Check Transaction Ended...");
    }

//    @Test
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