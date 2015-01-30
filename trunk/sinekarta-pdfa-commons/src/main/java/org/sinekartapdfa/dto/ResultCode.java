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
package org.sinekartapdfa.dto;

public enum ResultCode {

	SUCCESS(200),
	BAD_REQUEST(400),
	UNAUTHORIZED(401),
	REQUEST_TIMEOUT(408),
	INTERNAL_SERVER_ERROR(500),
	NOT_IMPLEMENTED(501),
	INTERNAL_CLIENT_ERROR(550);
	
	private ResultCode ( int code ) {
		this.code = code;
	}
	
	private final int code;

	public String getCode() {
		return Integer.toString(code);
	}
}
