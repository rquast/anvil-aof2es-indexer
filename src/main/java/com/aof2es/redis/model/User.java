package com.aof2es.redis.model;

import java.util.ArrayList;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("user")
public class User {
    
    private String email;
    
    private String hash;
    
    private ArrayList<String> providers;
    
    private String _id;
    
    private long created;
    
    private long modified;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public ArrayList<String> getProviders() {
        return providers;
    }

    public void setProviders(ArrayList<String> providers) {
        this.providers = providers;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
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
