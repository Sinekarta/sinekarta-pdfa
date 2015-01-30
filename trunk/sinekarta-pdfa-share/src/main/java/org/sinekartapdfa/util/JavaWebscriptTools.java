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
package org.sinekartapdfa.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONObject;

import org.alfresco.web.site.SlingshotUserFactory;
import org.apache.axiom.attachments.utils.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.sinekartapdfa.dto.ResultCode;
import org.sinekartapdfa.dto.request.BaseRequest;
import org.sinekartapdfa.dto.response.BaseResponse;
import org.sinekartapdfa.utils.EncodingUtils;
import org.springframework.extensions.surf.RequestContext;
import org.springframework.extensions.surf.support.ThreadLocalRequestContext;
import org.springframework.extensions.surf.util.URLEncoder;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.connector.Connector;
import org.springframework.extensions.webscripts.connector.ConnectorContext;
import org.springframework.extensions.webscripts.connector.ConnectorService;
import org.springframework.extensions.webscripts.connector.Credentials;
import org.springframework.extensions.webscripts.connector.HttpMethod;
import org.springframework.extensions.webscripts.connector.Response;

public class JavaWebscriptTools {
	
	static final Logger tracer = Logger.getLogger(JavaWebscriptTools.class);
	

	
	public static void updateProperty(String nodeRef, String propertyName, String newValue, ConnectorService connectorService) throws Exception {
		JavaWebscriptTools.executePostRequest("/sinekarta/internal/theTool", 
				 "{ \"result\" : eval(\"function doo() {" +
				"var n = search.findNode(\\\""+nodeRef+"\\\");" +
				"n.properties[\\\""+propertyName+"\\\"] = \\\""+newValue.replaceAll("\\\"", "\\\\\\\\\\\\\"")+"\\\";" +
				"n.save();" +
				"return true;"+
				"}" +
				"doo();\")}", 
				 "text/plain", 
				 connectorService);
	}
	
	public static void updatePersonProperty(String propertyName, String newValue, ConnectorService connectorService) throws Exception {
		JavaWebscriptTools.executePostRequest("/sinekarta/internal/theTool", 
				 "{ \"result\" : eval(\"function doo() {" +
				"person.properties[\\\""+propertyName+"\\\"] = \\\""+newValue.replaceAll("\\\"", "\\\\\\\\\\\\\"")+"\\\";" +
				"person.save();" +
				"return true;"+
				"}" +
				"doo();\")}", 
				 "text/plain", 
				 connectorService);
	}
	
	public static String getErrorMessage(Response resp) {
		try {
			JSONObject inputJson=JSONObject.fromObject(resp.getResponse());
			return inputJson.getString("message");
		} catch (Throwable t) {
			try {
				return "Impossibile risalire al messaggio di errore, status risposta = " + resp.getStatus();
			} catch (Throwable tt) {
				return "Impossibile risalire al messaggio di errore.";
			}				
		}
		
	}
	
	public static Serializable getRequestParameter(WebScriptRequest req, String key) {
        RequestContext rc = ThreadLocalRequestContext.getRequestContext();
        return rc.getParameter(key);
	}
	
	
	
	
	
	
	
	public static <SkdsResponse extends BaseResponse> SkdsResponse postJsonRequest (
			BaseRequest request, 
			Class<SkdsResponse> responseClass, ConnectorService connectorService ) throws AlfrescoException {
		
		String message = null;
		ResultCode code = null;
		Throwable cause = null;
		String receivedData = null;
		
		try {
			Response resp = null;
			SkdsResponse response = null;
			
			String url = request.getJSONUrl();
			// Perform the remote call to the Alfresco-tier services
			resp = JavaWebscriptTools.executeJsonPostRequest(
					url+".json?requestType=json", request.toJSON(), connectorService);
			// Accept the response only if the http connection status is 200
			if ( resp.getStatus().getCode() != Status.STATUS_OK ) {
				message = "communication failure, see the data received with the alfresco response";
				receivedData = new String ( IOUtils.getStreamAsByteArray(resp.getResponseStream()) );
				code = ResultCode.BAD_REQUEST; 
				tracer.error(String.format("%s \nrespContent:\n%s", message, receivedData));
				throw new AlfrescoException(message, code, receivedData);
			}
			// Accept the response only if the operation succeeds
			response = (SkdsResponse)EncodingUtils.deserializeJSON ( responseClass, resp.getResponseStream() );
			code = response.resultCodeFromString();
			if ( code != null && code != ResultCode.SUCCESS ) {
				if ( StringUtils.isBlank(message) ) {
					message = response.getMessage();
				}
				throw new AlfrescoException(message, code);
			}
			return response;
		} catch(AlfrescoException e) {
			throw e;
		} catch(Exception e) {
			message = e.getMessage();
			if ( StringUtils.isBlank(message) ) {
				message = e.toString();
			}
			cause = e;
			try {
				code = ResultCode.valueOf(e.getMessage());
			} catch(Exception e1) {
				code = ResultCode.INTERNAL_CLIENT_ERROR;
			}
			tracer.error(message, e);
			throw new AlfrescoException(message, cause, code);
		}
	}
	
	
	
	
	
	
	

	public static Response executeJsonPostRequest(String url, String body, ConnectorService connectorService) {
		Map<String,String> params = new HashMap<String,String>();
		Map<String,String> header = new HashMap<String,String>();
		header.put("Content-Type", "application/json");
		return executePostRequest(url, new ByteArrayInputStream(body.getBytes()), HttpMethod.POST, params, header, connectorService);
	}
	
	public static Response executePostRequest(String url, String body, String contentType, ConnectorService connectorService) throws Exception {
		Map<String,String> params = new HashMap<String,String>();
		Map<String,String> header = new HashMap<String,String>();
		header.put("Content-Type", contentType);
		return executePostRequest(url, new ByteArrayInputStream(body.getBytes()), HttpMethod.POST, params, header, connectorService);
	}
	
	private static Response executePostRequest(String url, InputStream is, HttpMethod method, Map<String,String> params, Map<String,String> header, ConnectorService connectorService) {
		Connector connector = getConnector(connectorService);
		ConnectorContext cc = new ConnectorContext(method, params, header);
		return connector.call(url, cc, is);
	}
	
	public static Response executeGetRequest(String url, Map<String,String> params, ConnectorService connectorService) {
		Connector connector = getConnector(connectorService);
		Map<String,String> header = new HashMap<String,String>();
		Map<String,String> pp = new HashMap<String,String>();
		if (params!=null && params.size()>0) {
			StringBuffer query = new StringBuffer(); 
			if (url.indexOf('?')==-1) {
				query.append("?");
			} else {
				if (!url.endsWith("&")) {
					query.append("&");
				}
			}
			for (String key : params.keySet()) {
				query.append(key);
				query.append("=");
				if (params.get(key)!=null) 
					query.append(URLEncoder.encode(params.get(key)));
				query.append("&");
			}
			String q = query.toString();
			q.substring(0, q.length()-2);
			url=url+q;
		}
		ConnectorContext cc = new ConnectorContext(HttpMethod.GET, pp, header);
		return connector.call(url, cc);
	}
	
	public static Connector getConnector(ConnectorService connectorService) {
		try {
			Connector connector = connectorService.getConnector(SlingshotUserFactory.ALFRESCO_ENDPOINT_ID);
	        RequestContext rc = ThreadLocalRequestContext.getRequestContext();
	        Credentials creds = rc.getCredentialVault().retrieve(SlingshotUserFactory.ALFRESCO_ENDPOINT_ID);
	        connector.setCredentials(creds);
	        return connector;
		} catch(RuntimeException e) {
			throw e;
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

}
