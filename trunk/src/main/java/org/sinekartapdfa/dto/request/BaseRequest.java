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

package org.sinekartapdfa.dto.request;

import org.apache.commons.lang3.StringUtils;
import org.sinekartapdfa.dto.BaseDTO;

public abstract class BaseRequest extends BaseDTO {
	
	private static final long serialVersionUID = -4894748877824011215L;

	/**
	 * @deprecated ignore this field - fake field for serialization only proposes
	 */
	protected String JSONUrl;
	
	public String getJSONUrl() {
		String className = getClass().getSimpleName(); 
		String cleanName = className.substring(0, className.lastIndexOf("Request"));
		String serviceName = StringUtils.uncapitalize(cleanName);
		return "/sinekartapdfa/" + serviceName; 
	}
	
}
