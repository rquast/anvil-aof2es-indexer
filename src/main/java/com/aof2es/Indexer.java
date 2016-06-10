package com.aof2es;

import com.aof2es.preferences.IPreferences;
import com.aof2es.xstream.XStreamUtility;
import com.aof2es.xstream.model.ApplicationPreferences;
import com.thoughtworks.xstream.XStream;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

import static org.elasticsearch.common.xcontent.XContentFactory.*;

public class Indexer implements ICommandProcessor {

    private static Logger LOG = Logger.getLogger(Indexer.class);
    
    private IPreferences preferences;
    private XStream serialize;

    private Client client;


    public Indexer() {
	this.serialize = XStreamUtility.getSerialize();
	processAnnotations(this.serialize);
    }

    private void processAnnotations(XStream xstream) {
	// xstream.processAnnotations(Query.class);
    }
    
    public void setPreferences(IPreferences preferences) {
        this.preferences = preferences;
    }

    public void connect() throws UnknownHostException {

	ApplicationPreferences applicationPreferences = this.preferences.getApplicationPreferences();

	this.client = TransportClient.builder().build().addTransportAddress(new InetSocketTransportAddress(
		InetAddress.getByName(applicationPreferences.getNodeAddress()), applicationPreferences.getNodePort()));

    }

    public static void printArgs(String[] args) {

	StringBuffer sb = new StringBuffer();
	sb.append("Command: " + args[0]);
	for (int i = 1; i < args.length; i++) {
	    sb.append(" arg" + i + ": " + args[i]);
	}
	System.out.println(sb.toString());

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
    public void processHsetCommand(String[] args) throws IOException {
	
	printArgs(args);
	
	// TODO Auto-generated method stub
	// Query query = new Query();
	// String json = serialize.toXML(query);
	// System.out.println(json);
	
	IndexResponse response = client.prepareIndex("twitter", "tweet", "1")
	        .setSource(jsonBuilder()
	                    .startObject()
	                        .field("user", "kimchy")
	                        // .field("postDate", new Date())
	                        .field("message", "trying out Elasticsearch")
	                    .endObject()
	                  )
	        .get();
	
	// Index name
	String _index = response.getIndex();
	// Type name
	String _type = response.getType();
	// Document ID (generated or not)
	String _id = response.getId();
	// Version (if it's the first time you index this document, you will get: 1)
	long _version = response.getVersion();
	// isCreated() is true if the document is a new one, false if it has been updated
	boolean created = response.isCreated();
	
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
    public void processHdelCommand(String[] args) {
	
	printArgs(args);
	
	// TODO: set a delete flag for the key
	
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
	// printArgs(args);
	
    }

}
