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

import org.apache.commons.lang3.StringUtils;
import org.sinekartapdfa.dto.BaseDTO;
import org.sinekartapdfa.dto.ResultCode;

public class BaseResponse extends BaseDTO {
	
	private static final long serialVersionUID = 7478526792110730390L;
	
	private String resultCode;
	private String message;
	
	public ResultCode resultCodeFromString() {
		if ( StringUtils.isBlank(resultCode) ) 				return null;
		return ResultCode.valueOf(resultCode);
	}

	public void resultCodeToString(ResultCode resultCode) {
		this.resultCode = resultCode.toString();
	}

	
	public String getResultCode() {
		return resultCode;
	}

	public void setResultCode(String resultCode) {
		this.resultCode = resultCode;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
