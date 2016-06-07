package com.aof2es.xstream.model;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("applicationPreferences")
public class ApplicationPreferences {

    private String aofFilePath = "/var/lib/redis/appendonly.aof";

    public String getAofFilePath() {
        return aofFilePath;
    }

    public void setAofFilePath(String aofFilePath) {
        this.aofFilePath = aofFilePath;
    }
    
}
