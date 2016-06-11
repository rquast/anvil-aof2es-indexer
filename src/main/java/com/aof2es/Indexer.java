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
import java.text.ParseException;
import java.util.concurrent.ExecutionException;

import org.apache.log4j.Logger;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.IndexNotFoundException;

import static org.elasticsearch.common.xcontent.XContentFactory.*;

public class Indexer implements ICommandProcessor {

    private static Logger LOG = Logger.getLogger(Indexer.class);
    
    public static enum Type { USERS, SCOPES, ROLES, UNKNOWN }
    
    // users:(user_id):roles
    // users:(user_id):clients
    // roles:(role_id):scopes
    // roles:(role_id):users
    // scopes:(scope_id):roles
    public static enum Relation { USERS_ROLES, USERS_CLIENTS, ROLES_SCOPES, ROLES_USERS, SCOPES_ROLES, UNKNOWN }
    
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
	
	Settings settings = Settings.settingsBuilder()
	        .put("cluster.name", applicationPreferences.getClusterName()).build();

	this.client = TransportClient.builder().settings(settings).build().addTransportAddress(new InetSocketTransportAddress(
		InetAddress.getByName(applicationPreferences.getNodeAddress()), applicationPreferences.getNodePort()));
	
	if (applicationPreferences.getPos() == 0) {
	    try {
		DeleteIndexResponse delete = client.admin().indices().delete(new DeleteIndexRequest("anvil"))
			.actionGet();
		if (delete.isAcknowledged()) {
		    LOG.error("Cleared anvil index.");
		}
	    } catch (IndexNotFoundException ex) {
		LOG.error(ex.getMessage(), ex);
	    }
	}

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
	    return Type.UNKNOWN;
	}
	
	return type;
	
    }

    @Override
    public void processHsetCommand(String[] args) throws IOException {
	
	XContentBuilder source = jsonBuilder().startObject();

	switch (getType(args)) {
	
	case USERS:

	    User user = (User) deserialize.fromXML("{\"user\": " + args[3] + "}");
	    
            source.field("email", user.getEmail())
            .field("created", user.getCreated())
            .field("modified", user.getModified());
            source.endObject();
            client.prepareIndex("anvil", "users", user.get_id()).setSource(source).execute().actionGet();
            
	    break;
	    
	case ROLES:
	    
	    Role role = (Role) deserialize.fromXML("{\"role\": " + args[3] + "}");
	    
            source.field("created", role.getCreated())
            .field("modified", role.getModified());
            source.endObject();
            client.prepareIndex("anvil", "roles", role.getName()).setSource(source).execute().actionGet();
            
	    break;
	    
	case SCOPES:
	    
	    Scope scope = (Scope) deserialize.fromXML("{\"scope\": " + args[3] + "}");
	    
            source.field("restricted", scope.isRestricted() ? "true": "false")
            .field("description", scope.getDescription())
            .field("created", scope.getCreated())
            .field("modified", scope.getModified());
            source.endObject();
            client.prepareIndex("anvil", "scopes", scope.getName()).setSource(source).execute().actionGet();
    	
	    break;
	
	case UNKNOWN:
	default:
	    return;
	    
	}
	
    }
    
    private void softDelete(String[] args) throws IOException {
	UpdateRequest updateRequest = new UpdateRequest();
	updateRequest.index("anvil");
	updateRequest.type(args[1]);
	updateRequest.id(args[2]);
	updateRequest.doc(jsonBuilder().startObject().field("deleted", "true").endObject());
	try {
	    client.update(updateRequest).get();
	} catch (InterruptedException e) {
	    throw new IOException(e);
	} catch (ExecutionException e) {
	    throw new IOException(e);
	}
    }
    
    @Override
    public void processHdelCommand(String[] args) throws IOException {
	
	switch (getType(args)) {
	
	case USERS:
	case ROLES:
	case SCOPES:
	    softDelete(args);
	    break;
	
	case UNKNOWN:
	default:
	    return;
	    
	}
	
    }

    @Override
    public void processZremCommand(String[] args) {
	printArgs(args);
    }
    
    private void parseRelation(String key, String id, Relation relation) throws IOException {
	
	// users:(user_id):roles
	// users:(user_id):clients
	// roles:(role_id):scopes
	// roles:(role_id):users
	// scopes:(scope_id):roles
	
	// TODO: This can probably done more quickly/efficiently with a regex. ^users:*:roles$
	
	String[] keyParts = key.split(":");
	if (keyParts.length != 3) {
	    // dodgy.. should throw a proper exception rather than IOEX.
	    throw new IOException("Invalid part count for relation key.");
	}
	
	id = keyParts[1].trim();
	
	if (keyParts[0].trim().equalsIgnoreCase("users")) {
	    if (keyParts[2].trim().equalsIgnoreCase("roles")) {
		
	    } else if (keyParts[2].trim().equalsIgnoreCase("clients")) {
		
		
	    } else {
		relation = Relation.UNKNOWN;
	    }
	} else if (keyParts[0].trim().equalsIgnoreCase("roles")) {
	    
	} else if (keyParts[0].trim().equalsIgnoreCase("scopes")) {
	    
	}
	
    }

    @Override
    public void processZaddCommand(String[] args) throws IOException {
	
	String id = null;
	Relation type = Relation.UNKNOWN;
	parseRelation(args[1], id, type);

	switch (type) {
	case USERS_ROLES:
	    break;
	case USERS_CLIENTS:
	    break;
	case ROLES_SCOPES:
	    break;
	case ROLES_USERS:
	    break;
	case SCOPES_ROLES:
	    break;
	case UNKNOWN:
	default:
	    return;
	}
	
    }

    @Override
    public void processSetCommand(String[] args) {
	printArgs(args);
    }

}
