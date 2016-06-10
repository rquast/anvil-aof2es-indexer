package com.aof2es.redis.model;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("scope")
public class Scope {

    private String name;
    
    private String description;
    
    private boolean restricted;
    
    private long created;
    
    private long modified;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isRestricted() {
        return restricted;
    }

    public void setRestricted(boolean restricted) {
        this.restricted = restricted;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public long getModified() {
        return modified;
    }

    public void setModified(long modified) {
        this.modified = modified;
    }
    
}
