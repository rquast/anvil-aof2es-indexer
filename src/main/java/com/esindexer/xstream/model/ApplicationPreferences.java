package com.esindexer.xstream.model;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("applicationPreferences")
public class ApplicationPreferences {

    private String aofFilePath = "";

    public String getAofFilePath() {
        return aofFilePath;
    }

    public void setAofFilePath(String aofFilePath) {
        this.aofFilePath = aofFilePath;
    }
    
}
