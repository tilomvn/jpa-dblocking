package com.dblocking;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Version;

@Entity
public class LockingEntity {

    public LockingEntity() {}

    public LockingEntity(Long id, String name) {
        this.id = id;
        this.name = name;
    }
    
    public LockingEntity(Long id, String name, short version) {
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

	public void setName(String name) {
        this.name = name;
	}

	public long getVersion() {
		return version;
	}

	public String getName() {
		return name;
	}
}