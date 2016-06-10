package com.aof2es;

import com.aof2es.preferences.IPreferences;
import com.aof2es.redis.model.Role;
import com.aof2es.redis.model.Scope;
import com.aof2es.redis.model.User;
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
import org.elasticsearch.common.xcontent.XContentBuilder;

import static org.elasticsearch.common.xcontent.XContentFactory.*;

public class Indexer implements ICommandProcessor {

    private static Logger LOG = Logger.getLogger(Indexer.class);
    
    public static enum Type { USERS, SCOPES, ROLES }
    
    private IPreferences preferences;
    private XStream deserialize;

    private Client client;


    public Indexer() {
	this.deserialize = XStreamUtility.getDeserialize();
	processAnnotations(this.deserialize);
    }

    private void processAnnotations(XStream xstream) {
	xstream.processAnnotations(Role.class);
	xstream.processAnnotations(Scope.class);
	xstream.processAnnotations(User.class);
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
	printArgs(args);
    }

    @Override
    public void processPexpireatCommand(String[] args) {
	printArgs(args);
    }
    
    private Type getType(String[] args) {
	
	String typeStr = args[1].toUpperCase();

	Type type;
	
	try {
	    type = Indexer.Type.valueOf(typeStr);
	} catch (IllegalArgumentException e) {
	    LOG.debug("Type not found: " + typeStr);
	    return null;
	}
	
	return type;
	
    }

    @Override
    public void processHsetCommand(String[] args) throws IOException {
	
	XContentBuilder source = jsonBuilder().startObject();
	
	IndexResponse response;

	switch (getType(args)) {
	
	case USERS:

	    User user = (User) deserialize.fromXML("{\"user\": " + args[3] + "}");
	    
	    // System.out.println("NEW USER.. KEY: " + args[2] + " DATA: " + args[3]);
	    
            source.field("email", user.getEmail())
            .field("user_id", user.get_id())
            .field("created", user.getCreated())
            .field("modified", user.getModified());
            source.endObject();
            response = client.prepareIndex("anvil", "users", user.get_id()).setSource(source).get();
            
	    break;
	    
	case ROLES:
	    
	    Role role = (Role) deserialize.fromXML("{\"role\": " + args[3] + "}");
	    
	    // System.out.println("NEW ROLE.. KEY: " + args[2] + " DATA: " + args[3]);
	    
            source.field("name", role.getName())
            .field("created", role.getCreated())
            .field("modified", role.getModified());
            source.endObject();
            response = client.prepareIndex("anvil", "roles", role.getName()).setSource(source).get();
            
	    break;
	    
	case SCOPES:
	    
	    Scope scope = (Scope) deserialize.fromXML("{\"scope\": " + args[3] + "}");
	    
	    // System.out.println("NEW SCOPE.. KEY: " + args[2] + " DATA: " + args[3]);
	    
            source.field("name", scope.getName())
            .field("restricted", scope.isRestricted() ? "true": "false")
            .field("description", scope.getDescription())
            .field("created", scope.getCreated())
            .field("modified", scope.getModified());
            source.endObject();
            response = client.prepareIndex("anvil", "scopes", scope.getName()).setSource(source).get();
    	
	    break;
	
	default:
	    return;
	    
	}

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
	
    }
    
    @Override
    public void processHdelCommand(String[] args) {
	
	switch (getType(args)) {
	
	case USERS:
	    System.out.println("DELETE USER.. KEY: " + args[2]);
	    break;
	    
	case ROLES:
	    System.out.println("DELETE ROLE.. KEY: " + args[2]);
	    break;
	    
	case SCOPES:
	    System.out.println("DELETE SCOPE.. KEY: " + args[2]);
	    break;
	
	default:
	    return;
	    
	}
	
	
    }

    @Override
    public void processZremCommand(String[] args) {
	printArgs(args);
    }

    @Override
    public void processZsetCommand(String[] args) {
	printArgs(args);
    }

    @Override
    public void processSetCommand(String[] args) {
	printArgs(args);
    }

}
