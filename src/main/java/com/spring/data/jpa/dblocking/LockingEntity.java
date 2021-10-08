package com.spring.data.jpa.dblocking;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Version;

@Entity
class LockingEntity {

    LockingEntity() {}

    LockingEntity(Long id, String name) {
        this.id = id;
        this.name = name;
    }
    
    LockingEntity(Long id, String name, short version) {
        this.id = id;
        this.name = name;
        this.version = version;
	}

	@Id
    Long id;

    @Column(length = 10, nullable = false)
    String name;

    @Version
    long version;

	void setName(String name) {
        this.name = name;
	}

	long getVersion() {
		return version;
	}

	String getName() {
		return name;
	}

	Long getId() {
		return id;
	}
}