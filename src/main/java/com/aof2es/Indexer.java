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
    
    private void addField(XContentBuilder source, String key, Object value) throws IOException {
	if (value != null) {
	    source.field(key, value);
	}
    }

    @Override
    public void processHsetCommand(String[] args) throws IOException {
	
	XContentBuilder source = jsonBuilder().startObject();

	Type type = getType(args);
	String id = null;
	
	switch (type) {
	
	case USERS:

	    User user = (User) deserialize.fromXML("{\"user\": " + args[3] + "}");
	    id = user.get_id();
	    
	    addField(source, "email", user.getEmail());
            addField(source, "name", user.getName());
            addField(source, "givenName", user.getGivenName());
            addField(source, "middleName", user.getMiddleName());
            addField(source, "familyName", user.getFamilyName());
            addField(source, "nickname", user.getNickname());
            addField(source, "preferredUsername", user.getPreferredUsername());
            addField(source, "profile", user.getProfile());
            addField(source, "picture", user.getPicture());
            addField(source, "website", user.getWebsite());
            addField(source, "email", user.getEmail());
            addField(source, "emailVerified", user.isEmailVerified());
            addField(source, "gender", user.getGender());
            addField(source, "birthdate", user.getBirthdate());
            addField(source, "zoneinfo", user.getZoneinfo());
            addField(source, "locale", user.getLocale());
            addField(source, "phoneNumber", user.getPhoneNumber());
            addField(source, "phoneNumberVerified", user.isPhoneNumberVerified());
            addField(source, "created", user.getCreated());
            addField(source, "modified", user.getModified());

	    break;
	    
	case ROLES:
	    
	    Role role = (Role) deserialize.fromXML("{\"role\": " + args[3] + "}");
	    id = role.getName();
	    
	    addField(source, "created", role.getCreated());
	    addField(source, "modified", role.getModified());

	    break;
	    
	case SCOPES:
	    
	    Scope scope = (Scope) deserialize.fromXML("{\"scope\": " + args[3] + "}");
	    id = scope.getName();
	    
	    addField(source, "restricted", scope.isRestricted());
	    addField(source, "description", scope.getDescription());
	    addField(source, "created", scope.getCreated());
	    addField(source, "modified", scope.getModified());
    	
	    break;
	
	case UNKNOWN:
	default:
	    return;
	    
	}
	
	source.endObject();
	client.prepareIndex("anvil", type.toString().toLowerCase(), id).setSource(source).get();
	
    }
    
    private void softDelete(String[] args) throws IOException {
	UpdateRequest updateRequest = new UpdateRequest();
	updateRequest.index("anvil");
	updateRequest.type(args[1]);
	updateRequest.id(args[2]);
	updateRequest.doc(jsonBuilder().startObject().field("deleted", true).endObject());
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
	updateRequest.doc(jsonBuilder().startObject().field("deleted", true).endObject());
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
	case USERS_CLIENTS:
	case ROLES_SCOPES:
	case ROLES_USERS:
	case SCOPES_ROLES:
	    break;
	case UNKNOWN:
	default:
	    return;
	}

	source.startObject(args[3]).field("created", Long.parseLong(args[2])).endObject();
	source.endObject();

	client.prepareIndex("anvil", parsedRelation.relation.toString().toLowerCase(), parsedRelation.id)
		.setSource(source).get();

    }

    @Override
    public void processSetCommand(String[] args) {
	// Do not index session information.
    }

}
