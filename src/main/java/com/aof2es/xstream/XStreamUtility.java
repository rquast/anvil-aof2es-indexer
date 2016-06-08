package com.aof2es.xstream;

import java.io.Writer;

import com.aof2es.xstream.model.ApplicationPreferences;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;
import com.thoughtworks.xstream.io.json.JsonHierarchicalStreamDriver;
import com.thoughtworks.xstream.io.json.JsonWriter;
import com.thoughtworks.xstream.mapper.MapperWrapper;

public final class XStreamUtility {

    public static XStream getSerialize() {
	final XStream xstream = new XStream(new JettisonMappedXmlDriver() {
	    @Override
	    public HierarchicalStreamWriter createWriter(final Writer writer) {
		return new JsonWriter(writer, JsonWriter.DROP_ROOT_MODE);
	    }
	}) {
	    @Override
	    protected MapperWrapper wrapMapper(MapperWrapper next) {
		return new MapperWrapper(next) {
		    @Override
		    public boolean shouldSerializeMember(@SuppressWarnings("rawtypes") Class definedIn,
			    String fieldName) {
			if (definedIn == Object.class) {
			    return false;
			}
			return super.shouldSerializeMember(definedIn, fieldName);
		    }
		};
	    }

	};
	xstream.setMode(XStream.NO_REFERENCES);

	processAnnotations(xstream);
	return xstream;
    }

    public static XStream getDeserialize() {
	final XStream xstream = new XStream(new JsonHierarchicalStreamDriver()) {
	    @Override
	    protected MapperWrapper wrapMapper(MapperWrapper next) {
		return new MapperWrapper(next) {
		    @Override
		    public boolean shouldSerializeMember(@SuppressWarnings("rawtypes") Class definedIn,
			    String fieldName) {
			if (definedIn == Object.class) {
			    return false;
			}
			return super.shouldSerializeMember(definedIn, fieldName);
		    }
		};
	    }
	};
	xstream.setMode(XStream.NO_REFERENCES);
	processAnnotations(xstream);
	return xstream;
    }

    private static void processAnnotations(XStream xstream) {
	xstream.processAnnotations(ApplicationPreferences.class);
    }

    private XStreamUtility() {
    }

}
