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
package org.sinekartapdfa.dto.response;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.sinekartapdfa.dto.BaseDTO;

@XmlRootElement(name = "sinekarta")
@XmlAccessorType(XmlAccessType.FIELD)
public class DocumentToPDFAResponse extends BaseResponse{

	private static final long serialVersionUID = 1L;
//	private String result;
	private String destNames;

	public static DocumentToPDFAResponse fromXML(InputStream is) throws JAXBException {
		return (DocumentToPDFAResponse)BaseDTO.fromXML(is, DocumentToPDFAResponse.class);
	}

	public static DocumentToPDFAResponse fromJSON(InputStream is) throws IOException {
		return (DocumentToPDFAResponse)BaseDTO.fromJSON(is, DocumentToPDFAResponse.class);
	}


	public String getDestNames() {
		return destNames;
	}

	public void setDestNames(String destNames) {
		this.destNames = destNames;
	}

	public String[] _getDestNames() {
		return destNames.split(",");
	}

	public void _setDestNames(String[] destNames) {
		StringBuffer sb = new StringBuffer();
		for (String s : destNames) {
			sb.append(s).append(",");
		}
		this.destNames = sb.toString();
		this.destNames = this.destNames.substring(0,this.destNames.length()-1);
	}
}
