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

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.sinekartapdfa.dto.BaseDTO;
import org.sinekartapdfa.dto.ToPDFADTO;
import org.sinekartapdfa.dto.request.DocumentToPDFARequest;
import org.sinekartapdfa.dto.response.DocumentToPDFAResponse;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class SkpdfaToPdfaWS extends BaseWS {

	static final Logger tracer = Logger.getLogger(SkpdfaToPdfaWS.class);
	
	public static final String DTO = "dto";
	
	public Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
		String jsonDTO = (String)getRequestParameter(req, "dto");
		ToPDFADTO dto = BaseDTO.fromJSON(ToPDFADTO.class, jsonDTO);
		
		
		try {
			DocumentToPDFARequest pdfaReq = new DocumentToPDFARequest();
			pdfaReq.setNodeRefs(dto.getSourceRef());
			DocumentToPDFAResponse pdfaResp = postJsonRequest(pdfaReq, DocumentToPDFAResponse.class);
			dto.setDestName(pdfaResp.getDestNames());
		} catch(Exception e) {
			dto.setErrorCode("skpdfa.internalError");
		}
		
		Map<String, Object> result = new HashMap<String, Object>();
		result.put(DTO, dto);
		return result;
	}
	
}
