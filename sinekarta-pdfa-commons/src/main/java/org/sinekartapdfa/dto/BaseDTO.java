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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.sinekartapdfa.utils.EncodingUtils;

public abstract class BaseDTO implements Serializable {

	private static final long serialVersionUID = -4276897678144607001L;
	
	public static boolean isEmpty ( BaseDTO dto )  {
		if ( dto == null )							return true;
		return dto.isEmpty();
	}

	public static boolean isNotEmpty ( BaseDTO dto )  {
		if ( dto == null )							return false;
		return !dto.isEmpty();
	}
	
	

	/**
	 * @deprecated ignore this field - fake field for serialization only proposes
	 */
	@SuppressWarnings("unused")
	private boolean empty;

	/**
	 * @deprecated ignore this setter - fake field for serialization only proposes
	 */
	public void setEmpty(boolean empty) {
		this.empty = empty;
	}
	
	public boolean isEmpty ( ) {
		return false;
	}
	
	
	
	public BaseDTO() {
		Class<? extends BaseDTO> dtoClass = getClass();
		
		try {
			String property;
			Class<?> fieldType;
			for ( Field field : dtoClass.getDeclaredFields() ) {
				// Ignore the static field 
				if((field.getModifiers() & java.lang.reflect.Modifier.STATIC) > 0) continue;
				
				// Set the property value to "" if it represents a String
				property = field.getName();
				fieldType = field.getType();
				if ( String.class.isAssignableFrom(fieldType) ) {
						BeanUtils.setProperty(this, property, "");
				}
			}
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}
	
	public String toString() {
		return EncodingUtils.serializeJSON ( this );
	}
	
	public boolean equals ( Object obj ) {
		boolean equals = false;
		if ( obj != null && obj instanceof BaseDTO ) {
			equals = StringUtils.equals ( 
					EncodingUtils.serializeJSON ( this ), 
					EncodingUtils.serializeJSON ( (BaseDTO)obj ) );
		}
		return equals; 
	}
	
	
	
	// -----
	// --- Base64 serialization
	// -
	
	public static <DTO extends BaseDTO> DTO fromBase64(InputStream is, Class<DTO> clazz) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		IOUtils.copy(is, baos);
		String base64 = new String(baos.toByteArray());
		return fromBase64(base64, clazz);
	}
	
	public static<DTO extends BaseDTO> DTO fromBase64(String base64, Class<DTO> clazz) {
		return EncodingUtils.deserializeBase64(clazz, base64);
	}
	
	public String toBase64() {
		return EncodingUtils.serializeBase64(this);
	}
	
	public static <DTO extends BaseDTO> String serializeBase64 ( DTO dto ) {
		return EncodingUtils.serializeBase64(dto);
	}
	
	
	
	// -----
	// --- JSON serialization
	// -
	
	public static <DTO extends BaseDTO> DTO fromJSON(Class<? extends DTO> dtoClass, String json) {
		return EncodingUtils.deserializeJSON(dtoClass, json);
	}

	public static <DTO extends BaseDTO> DTO fromJSON(InputStream is, Class<? extends DTO> dtoClass) throws IOException {
		String json = new String ( IOUtils.toByteArray(is) );
		return EncodingUtils.deserializeJSON ( dtoClass, json );
	}
	
	public void toJSON(OutputStream os) throws IOException {
		String json = toJSON(); 
		os.write ( json.getBytes() );
		os.flush();
	}

	public String toJSON() {
		return EncodingUtils.serializeJSON ( this );
	}
	
	public static <DTO extends BaseDTO> String serializeJSON ( DTO dto ) {
		return EncodingUtils.serializeJSON(dto);
	}
	
	
	
	// -----
	// --- XML serialization
	// -
	
	public static <DTO extends BaseDTO> DTO fromXML ( String xml, Class<DTO> clazz ) throws JAXBException {
		return fromXML ( new ByteArrayInputStream(xml.getBytes()), clazz );
	}
	
	@SuppressWarnings("unchecked")
	public static <DTO extends BaseDTO> DTO fromXML ( InputStream is, Class<DTO> clazz ) throws JAXBException {
		Unmarshaller u = JAXBContext.newInstance(clazz).createUnmarshaller();
		return (DTO) u.unmarshal(is);
	}
	
	public void toXML(OutputStream os) throws JAXBException {
		JAXBContext jcupr = JAXBContext.newInstance(this.getClass());
		Marshaller m = jcupr.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		m.marshal(this, os);
	}
	
	public String toXML() throws JAXBException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		toXML(baos);
		IOUtils.closeQuietly(baos);
		return new String(baos.toByteArray());
	}
	
	public static <DTO extends BaseDTO> String serializeXML ( DTO dto ) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			JAXBContext jcupr = JAXBContext.newInstance(dto.getClass());
			Marshaller m = jcupr.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			m.marshal(dto, baos);
			IOUtils.closeQuietly(baos);
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
		return new String(baos.toByteArray());
	}
	
}


