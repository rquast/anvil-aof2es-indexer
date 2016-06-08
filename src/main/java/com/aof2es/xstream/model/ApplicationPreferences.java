package com.aof2es.xstream.model;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("applicationPreferences")
public class ApplicationPreferences {

    private String aofFilePath = "/var/lib/redis/appendonly.aof";
    
    private String nodeAddress = "127.0.0.1";
    
    private int nodePort = 9300;

    public String getAofFilePath() {
        return aofFilePath;
    }
    
    public String getNodeAddress() {
        return nodeAddress;
    }

    public int getNodePort() {
        return nodePort;
    }

    public void setNodePort(int nodePort) {
        this.nodePort = nodePort;
    }

    public void setNodeAddress(String nodeAddress) {
        this.nodeAddress = nodeAddress;
    }

    public void setAofFilePath(String aofFilePath) {
        this.aofFilePath = aofFilePath;
    }
    
}
