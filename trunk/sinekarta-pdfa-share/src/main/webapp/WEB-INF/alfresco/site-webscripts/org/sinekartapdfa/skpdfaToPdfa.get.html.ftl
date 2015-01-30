<#--
/*
 * Copyright (C) 2010 - 2012 Jenia Software.
 *
 * This file is part of Sinekarta
 *
 * Sinekarta is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Sinekarta is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 */
-->
<#include "/org/alfresco/include/alfresco-macros.lib.ftl" />
<#assign el=htmlid?js_string>

<#attempt>
	<#if dto.errorCode?has_content >
		${msg(dto.errorCode)}
	<#else>
		<p>
			<label>${msg("label.success")}</label>
		</p>
		<p>
			<label>${msg("label.convertedDocument")}</label><br/>
			<a id="${htmlid}-result" href="${page.url.context}/page/context/mine/document-details?nodeRef=${dto.sourceRef}">${dto.destName}</a>
		</p>
		<p>
			<br/><br/>
			<button onClick="window.location.href='${dto.backUrl}'">${msg("label.back")}</button>
		</p>
	</#if>
<#recover>
	${.error}
</#attempt>