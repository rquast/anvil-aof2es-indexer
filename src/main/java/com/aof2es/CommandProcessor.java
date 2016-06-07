package com.aof2es;

public interface CommandProcessor {
    
    public void processDelCommand(String[] args);

    public void processPexpireatCommand(String[] args);

    public void processHsetCommand(String[] args);

    public void processZremCommand(String[] args);

    public void processZsetCommand(String[] args);

    public void processSetCommand(String[] args);
}
