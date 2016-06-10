package com.aof2es;

import java.io.IOException;

public interface ICommandProcessor {
    
    public void processDelCommand(String[] args) throws IOException;

    public void processPexpireatCommand(String[] args) throws IOException;

    public void processHsetCommand(String[] args) throws IOException;

    public void processZremCommand(String[] args) throws IOException;

    public void processZsetCommand(String[] args) throws IOException;

    public void processSetCommand(String[] args) throws IOException;

    public void processHdelCommand(String[] args) throws IOException;
}
