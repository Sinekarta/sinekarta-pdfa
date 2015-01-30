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

/**
 * sinekarta-ds Document library actions
 * 
 * @namespace Alfresco
 * @class Alfresco.doclib.Actions
 */
(function() {

   /**
    * YUI Library aliases
    */
	var Dom = YAHOO.util.Dom;
   	var Event = YAHOO.util.Event;
   	var $html = Alfresco.util.encodeHTML;
   	var $combine = Alfresco.util.combinePaths;
   	var $siteURL = Alfresco.util.siteURL;
  
   
   
   	YAHOO.Bubbling.fire("registerAction",  { 
		actionName: "onActionSkpdfaToPdfa", 
		fn: function DL_onActionSkpdfaToPdfa(asset) {
			var url = Alfresco.constants.URL_CONTEXT+"page/skpdfaToPdfa";
			
			var toPdfDto = {};
			toPdfDto.sourceRef = asset.nodeRef;
			toPdfDto.backUrl = window.location.href;

			var parameters = {};
			parameters.dto = JSON.stringify(toPdfDto);
			location.href = buildUrl(url, parameters);
		}
   	});
   	
   	
   	/**
   	 * Build an URL with queryString which can be redirected with a get call
   	 * @param url the full URL to be redirected
   	 * @param parameters a JavaScript object with the queryString parameters
   	 * @returns the composed URL
   	 */
   	function buildUrl ( url, parameters ) {
   		var href = "";
   		var qs = "";
   		for(var key in parameters) {
   			var value = parameters[key];
   			qs += encodeURIComponent(key) + "=" + encodeURIComponent(value) + "&";
   		}
   		if (qs.length > 0) {
   			qs = qs.substring(0, qs.length-1); 
   			href = url + "?" + qs;
   		}
   		return href;
   	}


})();
