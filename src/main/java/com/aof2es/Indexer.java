package com.aof2es;

import com.aof2es.xstream.model.ApplicationPreferences;

public class Indexer implements CommandProcessor {

    private ApplicationPreferences ap;

    public Indexer(ApplicationPreferences ap) {
	this.ap = ap;
    }

    @Override
    public void processDelCommand(String[] args) {
	// TODO Auto-generated method stub
	
    }

    @Override
    public void processPexpireatCommand(String[] args) {
	// TODO Auto-generated method stub
	
    }

    @Override
    public void processHsetCommand(String[] args) {
	// TODO Auto-generated method stub
	
    }

    @Override
    public void processZremCommand(String[] args) {
	// TODO Auto-generated method stub
	
    }

    @Override
    public void processZsetCommand(String[] args) {
	// TODO Auto-generated method stub
	
    }

    @Override
    public void processSetCommand(String[] args) {
	// TODO Auto-generated method stub
	
    }

}
