package com.aof2es;

import com.aof2es.preferences.IPreferences;
import com.aof2es.xstream.XStreamUtility;
import com.aof2es.xstream.model.ApplicationPreferences;
import com.rethinkdb.RethinkDB;
import com.rethinkdb.net.Connection;
import com.thoughtworks.xstream.XStream;

import org.apache.log4j.Logger;


public class Indexer implements ICommandProcessor {

    private static Logger LOG = Logger.getLogger(Indexer.class);
    
    private IPreferences preferences;
    private RethinkDB r;
    private XStream serialize;

    private Connection conn;


    public Indexer() {
	this.serialize = XStreamUtility.getSerialize();
	processAnnotations(this.serialize);
	r = RethinkDB.r;
    }

    private void processAnnotations(XStream xstream) {
	// xstream.processAnnotations(Query.class);
    }
    
    public void setPreferences(IPreferences preferences) {
        this.preferences = preferences;
    }

    public void connect() {

	ApplicationPreferences applicationPreferences = this.preferences.getApplicationPreferences();

	this.conn = r.connection().hostname(applicationPreferences.getNodeAddress())
		.port(applicationPreferences.getNodePort()).connect();
	
	r.db("anvil").tableCreate("users").run(conn);
	r.db("anvil").tableCreate("roles").run(conn);
	r.db("anvil").tableCreate("scopes").run(conn);

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
    public void processHsetCommand(String[] args) {
	
	printArgs(args);
	
	// TODO Auto-generated method stub
	// Query query = new Query();
	// String json = serialize.toXML(query);
	// System.out.println(json);
	
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
