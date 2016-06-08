package com.aof2es;

import com.aof2es.xstream.model.ApplicationPreferences;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

public class Indexer implements CommandProcessor {

    private ApplicationPreferences applicationPreferences;
    private TransportClient client;

    public Indexer() {
    }

    public void setApplicationPreferences(ApplicationPreferences applicationPreferences) {
        this.applicationPreferences = applicationPreferences;
    }
    
    public void connect() {
	this.client = new TransportClient();
	this.client.addTransportAddress(
		new InetSocketTransportAddress(
			this.applicationPreferences.getNodeAddress(),
			this.applicationPreferences.getNodePort()
			)
		);
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
