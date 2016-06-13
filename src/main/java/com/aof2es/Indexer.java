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
    
    private class ParsedRelation {
	public Relation relation = Relation.UNKNOWN;
	public String id = null;
    }


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
		    LOG.info("Cleared anvil index.");
		}
	    } catch (IndexNotFoundException ex) {
		LOG.info("Index not found, not clearing.");
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
	// Do not index session information.
    }

    @Override
    public void processPexpireatCommand(String[] args) {
	// Do not index session information.
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
    
    private void softDeleteRelation(ParsedRelation parsedRelation) throws IOException {
	UpdateRequest updateRequest = new UpdateRequest();
	updateRequest.index("anvil");
	updateRequest.type(parsedRelation.relation.toString().toLowerCase());
	updateRequest.id(parsedRelation.id);
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
    public void processZremCommand(String[] args) throws IOException {
	
	ParsedRelation parsedRelation = new ParsedRelation();
	parseRelation(args[1], parsedRelation);
	
	switch (parsedRelation.relation) {
	
	case USERS_ROLES:
	case USERS_CLIENTS:
	case ROLES_SCOPES:
	case ROLES_USERS:
	case SCOPES_ROLES:
	    softDeleteRelation(parsedRelation);
	    break;
	
	case UNKNOWN:
	default:
	    return;
	    
	}
	
    }
    
    private void parseRelation(String key, ParsedRelation parsedRelation) throws IOException {
	
	// users:(user_id):roles
	// users:(user_id):clients
	// roles:(role_id):scopes
	// roles:(role_id):users
	// scopes:(scope_id):roles
	
	// TODO: This can probably done more quickly/efficiently with a regex. ^users:*:roles$
	
	String[] keyParts = key.split(":");
	if (keyParts.length != 3) {
	    return; // ignore anything that's not 3 parts.
	}
	
	parsedRelation.id = keyParts[1].trim();
	
	if (keyParts[0].trim().equalsIgnoreCase("users")) {
	    if (keyParts[2].trim().equalsIgnoreCase("roles")) {
		parsedRelation.relation = Relation.USERS_ROLES;
	    } else if (keyParts[2].trim().equalsIgnoreCase("clients")) {
		parsedRelation.relation = Relation.USERS_CLIENTS;
	    } else {
		parsedRelation.relation = Relation.UNKNOWN;
	    }
	} else if (keyParts[0].trim().equalsIgnoreCase("roles")) {
	    if (keyParts[2].trim().equalsIgnoreCase("scopes")) {
		parsedRelation.relation = Relation.ROLES_SCOPES;
	    } else if (keyParts[2].trim().equalsIgnoreCase("roles")) {
		parsedRelation.relation = Relation.ROLES_USERS;
	    } else {
		parsedRelation.relation = Relation.UNKNOWN;
	    }
	} else if (keyParts[0].trim().equalsIgnoreCase("scopes")) {
	    if (keyParts[2].trim().equalsIgnoreCase("roles")) {
		parsedRelation.relation = Relation.SCOPES_ROLES;
	    } else {
		parsedRelation.relation = Relation.UNKNOWN;
	    }
	}
	
	if (parsedRelation.relation == Relation.UNKNOWN) {
	    LOG.debug(
		    "Relation not found: " + keyParts[0].trim().toUpperCase() + "_" + keyParts[2].trim().toUpperCase());
	}
	
    }

    @Override
    public void processZaddCommand(String[] args) throws IOException {

	ParsedRelation parsedRelation = new ParsedRelation();
	parseRelation(args[1], parsedRelation);

	XContentBuilder source = jsonBuilder().startObject();

	switch (parsedRelation.relation) {
	case USERS_ROLES:
	    
	    // TODO: this is one to many relationship.
	    // Should update the field to be "roles" and an array (that gets updated).
	    
	    // 1. select an existing record
	    
	    // if exists, append
	    
	    // else, create a new array with the role.
	    source.array("roles", new String[]{args[3]});
	    
	    break;
	    
	case USERS_CLIENTS:
	    source.field("client_id", args[3]);
	    break;
	case ROLES_SCOPES:
	    // TODO: this is one to many.
	    source.field("scope", args[3]);
	    break;
	case ROLES_USERS:
	    // TODO: this is one to many.
	    source.field("user_id", args[3]);
	    break;
	case SCOPES_ROLES:
	    source.field("role", args[3]);
	    break;
	case UNKNOWN:
	default:
	    return;
	}

	source.endObject();
	client.prepareIndex("anvil", parsedRelation.relation.toString().toLowerCase(), parsedRelation.id)
		.setSource(source).execute().actionGet();

    }

    @Override
    public void processSetCommand(String[] args) {
	// Do not index session information.
    }

}
