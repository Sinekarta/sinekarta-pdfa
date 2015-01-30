/*
 * Copyright (C) 2014 - 2015 Jenia Software.
 *
 * This file is part of Sinekarta-ds
 *
 * Sinekarta-pdfa is Open SOurce Software: you can redistribute it and/or modify
 * it under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sinekartapdfa.share.webscript;

import java.io.Serializable;
import java.util.ResourceBundle;

import org.sinekartapdfa.dto.request.BaseRequest;
import org.sinekartapdfa.dto.response.BaseResponse;
import org.sinekartapdfa.util.AlfrescoException;
import org.sinekartapdfa.util.JavaWebscriptTools;
import org.springframework.extensions.surf.RequestContext;
import org.springframework.extensions.surf.support.ThreadLocalRequestContext;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.connector.ConnectorService;

public abstract class BaseWS extends DeclarativeWebScript {
	
	protected ConnectorService connectorService;
    
	public void setConnectorService(ConnectorService connectorService) {
		this.connectorService = connectorService;
	}
	
	// ----- 
	// --- Webscript protocol
	// -
	
	public static Serializable getRequestParameter(WebScriptRequest req, String key) {
        RequestContext rc = ThreadLocalRequestContext.getRequestContext();
        return rc.getParameter(key);
	}
	
	public <SkdsResponse extends BaseResponse> SkdsResponse postJsonRequest (
			BaseRequest request, 
			Class<SkdsResponse> responseClass) throws AlfrescoException {
		return JavaWebscriptTools.postJsonRequest(request, responseClass, connectorService); 
	}
	
	protected String getMessage(String messageId) {
		String message = null;
		try {
			if(getResources().containsKey(messageId)) {
				message = getResources().getString(messageId);
			} else {
				message = ResourceBundle.getBundle("alfresco/messages/skds-commons").getString(messageId);
			}
		} catch(Exception e) {
			message = null;
		}
		if(message == null) {
			message = messageId;
		}
		return message;
	}

	protected String getParameter ( WebScriptRequest req, String parameter ) {
		return (String) JavaWebscriptTools.getRequestParameter(req, parameter);
	}
	
	protected <T> T getParameter ( WebScriptRequest req, String parameter, Class<T> tClass ) {
		return tClass.cast ( JavaWebscriptTools.getRequestParameter(req, parameter) );
	}
}