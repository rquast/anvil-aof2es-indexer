package com.aof2es;

import com.aof2es.elastic.model.Bool;
import com.aof2es.elastic.model.Filter;
import com.aof2es.elastic.model.Must;
import com.aof2es.elastic.model.Query;
import com.aof2es.preferences.IPreferences;
import com.aof2es.xstream.XStreamUtility;
import com.aof2es.xstream.model.ApplicationPreferences;
import com.thoughtworks.xstream.XStream;

import org.apache.log4j.Logger;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

public class Indexer implements ICommandProcessor {

    private static Logger LOG = Logger.getLogger(Indexer.class);
    
    private IPreferences preferences;
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
    
    public void setPreferences(IPreferences preferences) {
        this.preferences = preferences;
    }

    public void connect() {
	ApplicationPreferences applicationPreferences = this.preferences.getApplicationPreferences();
	this.client = new TransportClient();
	this.client.addTransportAddress(
		new InetSocketTransportAddress(
			applicationPreferences.getNodeAddress(),
			applicationPreferences.getNodePort()
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
	String json = serialize.toXML(query);
	System.out.println(json);
	
	/*
	
	IndexResponse response = null;

	try {
	    response = client
		    .prepareIndex("index", "type", "id")
		    .setSource(json).execute().actionGet();
	} catch (Exception ex) {
	    LOG.error(ex, ex);
	}
	
	*/
	
	// then do something with response...
	
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
