package de.avgl.dmp.controller.resources;

import de.avgl.dmp.controller.resources.utils.BasicIDResourceUtils;
import de.avgl.dmp.controller.status.DMPStatus;
import de.avgl.dmp.persistence.model.BasicDMPJPAObject;
import de.avgl.dmp.persistence.service.BasicDMPJPAService;

/**
 * A generic resource (controller service) implementation for {@link BasicDMPJPAObject}s, i.e., objects where the identifier will
 * be generated by the database and that can have a name.
 * 
 * @author tgaengler
 * @param <POJOCLASSPERSISTENCESERVICE> the concrete persistence service of the resource that is related to the concrete POJO
 *            class
 * @param <POJOCLASS> the concrete POJO class of the resource
 */
public abstract class BasicDMPResource<POJOCLASSRESOURCEUTILS extends BasicIDResourceUtils<POJOCLASSPERSISTENCESERVICE, POJOCLASS>, POJOCLASSPERSISTENCESERVICE extends BasicDMPJPAService<POJOCLASS>, POJOCLASS extends BasicDMPJPAObject>
		extends BasicIDResource<POJOCLASSRESOURCEUTILS, POJOCLASSPERSISTENCESERVICE, POJOCLASS> {

	private static final org.apache.log4j.Logger	LOG	= org.apache.log4j.Logger.getLogger(BasicDMPResource.class);

	/**
	 * Creates a new resource (controller service) for the given concrete POJO class with the provider of the concrete persistence
	 * service, the object mapper and metrics registry.
	 * 
	 * @param clasz a concrete POJO class
	 * @param persistenceServiceProviderArg the concrete persistence service that is related to the concrete POJO class
	 * @param objectMapperArg an object mapper
	 * @param dmpStatusArg a metrics registry
	 */
	public BasicDMPResource(final POJOCLASSRESOURCEUTILS pojoClassResourceUtilsArg, final DMPStatus dmpStatusArg) {

		super(pojoClassResourceUtilsArg, dmpStatusArg);
	}

	/**
	 * {@inheritDoc}<br/>
	 * Updates the name of the object.
	 */
	@Override
	protected POJOCLASS prepareObjectForUpdate(final POJOCLASS objectFromJSON, final POJOCLASS object) {

		object.setName(objectFromJSON.getName());

		return object;
	}
}
