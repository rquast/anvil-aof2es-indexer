package com.aof2es.redis.model;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("role")
public class Role {
    
    private String name;
    
    private long created;
    
    private long modified;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(int created) {
        this.created = created;
    }

    public long getModified() {
        return modified;
    }

    public void setModified(int modified) {
        this.modified = modified;
    }
    
}
