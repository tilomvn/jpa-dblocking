# Spring Data JPA OptimisticLock and PessimisticLock Demostration
* OptimisticLock can work on NoSql or RDBMS with using @Version. The exception ObjectOptimisticLockingFailureException would be threw.
* PessimisticLock can work by using @Lock annotation. The exception PessimisticLockingFailureException would be threw.