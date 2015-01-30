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
package org.sinekartapdfa.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SerializationUtils;

public final class EncodingUtils {
		
		// --- Simple serialization
		
		public static Serializable toSerializable ( Object item) {
			if( !(item instanceof Serializable) ) {
				throw new UnsupportedOperationException(String.format ( "not serializable object - %s", item ));
			}
			return (Serializable) item;
		}
		
		public static byte[] serialize ( Serializable item ) {
			return SerializationUtils.serialize ( item );
		}
		
		public static <T> T deserialize ( Class<T> tClass, byte[] bytes ) {
			return (T)SerializationUtils.deserialize(bytes);
		}
		
		
		// --- Hex encoding
		
		public static String serializeHex ( Serializable item ) {
			byte[] bytes = SerializationUtils.serialize ( item );
			return HexUtils.encodeHex ( bytes );
		}
		
		public static <T> T deserializeHex ( Class<T> tClass, String hex ) {
			byte[] bytes = HexUtils.decodeHex(hex);
			return deserialize(tClass, bytes);
		}
		
		public static String serializeHex ( Serializable item, OutputStream os ) throws IOException {
			String hexEnc = serializeHex ( item );
			IOUtils.write ( hexEnc.getBytes(), os );
			return hexEnc;
		}
		
		public static <T> T deserializeHex ( Class<T> tClass, InputStream is ) throws IOException {
			String hexEnc = new String ( IOUtils.toByteArray(is) );
			return deserializeJSON(tClass, hexEnc);
		}
		
		
		// --- Base64 encoding
		
		public static String serializeBase64 ( Serializable item ) {
			byte[] bytes = SerializationUtils.serialize ( item );
			return Base64.encodeBase64String ( bytes );
		}
		
		public static <T> T deserializeBase64 ( Class<T> tClass, String base64 ) {
			byte[] bytes = Base64.decodeBase64 ( base64 );
			return deserialize ( tClass, bytes );
		}
		
		public static String serializeBase64 ( Serializable item, OutputStream os ) throws IOException {
			String base64Enc = serializeBase64 ( item );
			IOUtils.write ( base64Enc.getBytes(), os );
			return base64Enc;
		}
		
		public static <T> T deserializeBase64 ( Class<T> tClass, InputStream is ) throws IOException {
			String base64Enc = new String ( IOUtils.toByteArray(is) );
			return deserializeJSON(tClass, base64Enc);
		}
		
		
		// --- JSON encoding
		
		public static String serializeJSON ( Object item, OutputStream os ) throws IOException {
			String jsonEnc = serializeJSON ( item, false );
			IOUtils.write ( jsonEnc.getBytes(), os );
			return jsonEnc;
		}
		
		public static String serializeJSON ( Object item ) {
			return serializeJSON ( item, false );
		}
		
		public static String serializeJSON ( Object item, boolean prettify ) {
			if ( item == null )													return "";
			String jsonEnc;
			try {
				Class<?> tClass = item.getClass();
				JsonConfig jsonConfig = standardJsonConfig ( tClass );
				JSON json;
				if ( Object[].class.isAssignableFrom(tClass) ) {
					json = JSONArray.fromObject ( item, jsonConfig );
				} else {
					json = JSONObject.fromObject ( item, jsonConfig );
				}
				if ( prettify ) {
					jsonEnc = json.toString(4);
				} else {
					jsonEnc = json.toString();
				}
			} catch(RuntimeException e) {
				throw e;
			} catch(Exception e) {
				throw new RuntimeException(e);
			}
			return jsonEnc;
		}

		public static <T> T deserializeJSON ( Class<T> tClass, InputStream is ) throws IOException {
			String jsonEnc = new String ( IOUtils.toByteArray(is) );
			return deserializeJSON(tClass, jsonEnc);
		}
		
		public static <T> T deserializeJSON ( Class<T> tClass, String jsonEnc ) {
			JsonConfig jsonConfig = standardJsonConfig ( tClass );
			T item;
			if ( Object[].class.isAssignableFrom(tClass) ) {
				JSONArray jsonArray = JSONArray.fromObject ( jsonEnc, jsonConfig );
				item = (T)JSONArray.toArray(jsonArray);
			} else {
				JSONObject jsonObject = JSONObject.fromObject ( jsonEnc, jsonConfig );
				item = (T)JSONObject.toBean(jsonObject, jsonConfig);
			}
			return item;
		}

		
		public static String prettifyJSON ( String json ) {
			String prettyJSON;
			try {
				JSONObject jsonobj = JSONObject.fromObject(json);
				prettyJSON = jsonobj.toString(4);
			} catch(RuntimeException e) {
				throw e;
			} catch(Exception e) {
				throw new RuntimeException(e);
			}
			return prettyJSON;
		}
		
		public static JsonConfig standardJsonConfig ( Class<?> tClass) {
			JsonConfig jsonConfig = new JsonConfig();
			jsonConfig.setRootClass(tClass);
//			jsonConfig.setIgnoreTransientFields(true);
			return jsonConfig;
		}
	}