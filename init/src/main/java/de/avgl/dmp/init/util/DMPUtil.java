package de.avgl.dmp.init.util;

import java.io.IOException;
import java.net.URL;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import de.avgl.dmp.init.DMPException;

public class DMPUtil {
	
	private static final JsonNodeFactory factory = JsonNodeFactory.instance;
	private static final ObjectMapper mapper;

	static {
		mapper = new ObjectMapper();
		final JaxbAnnotationModule module = new JaxbAnnotationModule();
		mapper.registerModule(module);
	}

	public static String getResourceAsString(String resource) throws IOException {
		URL url = Resources.getResource(resource);
		return Resources.toString(url, Charsets.UTF_8);
	}
	
	public static ObjectNode getJSON(final String jsonString) throws DMPException {
		
		try {
			return mapper.readValue(jsonString, ObjectNode.class);
		} catch (JsonParseException e) {
			
			throw new DMPException("something went wrong while parsing the JSON string '" + jsonString + "'\n" + e.getMessage());
		} catch (JsonMappingException e) {
			
			throw new DMPException("something went wrong while parsing the JSON string '" + jsonString + "'\n" + e.getMessage());
		} catch (IOException e) {
			
			throw new DMPException("something went wrong while parsing the JSON string '" + jsonString + "'\n" + e.getMessage());
		}
	}
	
	public static ObjectMapper getJSONObjectMapper() {
		
		return mapper;
	}
	
	public static JsonNodeFactory getJSONFactory() {
		
		return factory;
	}
}