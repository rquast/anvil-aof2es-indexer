package com.aof2es;

import com.aof2es.elastic.model.Bool;
import com.aof2es.elastic.model.Filter;
import com.aof2es.elastic.model.Must;
import com.aof2es.elastic.model.Query;
import com.aof2es.xstream.XStreamUtility;
import com.aof2es.xstream.model.ApplicationPreferences;
import com.thoughtworks.xstream.XStream;

import java.io.File;
import java.io.FileOutputStream;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

public class Indexer implements CommandProcessor {

    private ApplicationPreferences applicationPreferences;
    private TransportClient client;
    private XStream serialize;

    public Indexer() {
	this.serialize = XStreamUtility.getSerialize();
	processAnnotations(this.serialize);
    }

    private void processAnnotations(XStream xstream) {
	xstream.processAnnotations(Query.class);
	xstream.processAnnotations(Must.class);
	xstream.processAnnotations(Bool.class);
	xstream.processAnnotations(Filter.class);
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
	Query query = new Query();
	String hsetString = serialize.toXML(query);
	System.out.println(hsetString);
	
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
