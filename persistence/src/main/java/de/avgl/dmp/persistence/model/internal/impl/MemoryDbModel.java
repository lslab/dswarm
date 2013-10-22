package de.avgl.dmp.persistence.model.internal.impl;

import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import de.avgl.dmp.persistence.model.internal.Model;
import de.avgl.dmp.persistence.util.DMPPersistenceUtil;

public class MemoryDbModel implements Model {
	
	private Map<String, String> keyValueMap;
	
	public MemoryDbModel(final Map<String, String> keyValueMapArg) {
		
		keyValueMap = keyValueMapArg;
	}

	@Override
	public JsonNode toJSON() {
		
		if(keyValueMap == null) {
			
			// TODO: log
			
			return null;
		}
		
		if(keyValueMap.isEmpty()) {
			
			// TODO: log
			
			return null;
		}
		
		final ObjectNode json = DMPPersistenceUtil.getJSONObjectMapper().createObjectNode();
		
		for(final Entry<String, String> keyValueEntry : keyValueMap.entrySet()) {
			
			json.put(keyValueEntry.getKey(), keyValueEntry.getValue());
		}
		
		return json;
	}
}