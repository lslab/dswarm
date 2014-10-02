/**
 * Copyright (C) 2013, 2014 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dswarm.controller.providers.handler;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import org.dswarm.controller.providers.BaseExceptionHandler;

/**
 * A default exception handler for providing exceptions at client side of the backend API.
 * 
 * @author phorn
 */
@Provider
public class ExceptionHandler extends BaseExceptionHandler<Exception> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Response toResponse(final Exception exception) {

		final String[] clientSegments = errorMessage(exception).split(":");
		final String message = clientSegments[clientSegments.length - 1];

		return createResponse(message);
	}
}
