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
package org.sinekartapdfa.alfresco.utils;

import java.util.Iterator;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.Path.ChildAssocElement;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.util.ISO9075;
import org.apache.log4j.Logger;

/**
 * utility class for working with nodes
 * 
 * @author andrea.tessaro
 *
 */
public class NodeTools {
	
	private static Logger tracer = Logger.getLogger(NodeTools.class);
	
	/**
	 * verify that a filename or folder name contains only valid character
	 * @param name
	 * @return
	 */
	public static String validateFileFolderName(String name) {
		if (name!=null) {
			name = name.replaceAll("~", "-");
			name = name.replaceAll("&", "-");
			name = name.replaceAll("#", "-");
			name = name.replaceAll("\\*", "-");
			name = name.replaceAll("\\?", "-");
			name = name.replaceAll(":", "-");
			
			name = name.replaceAll("\"", "'");
			
			name = name.replaceAll("\\|", "-");
			name = name.replaceAll("\\\\", "-");
			name = name.replaceAll("/", "-");
			
			name = name.replaceAll("<", "(");
			name = name.replaceAll(">", ")");
			name = name.replaceAll("\\{", "(");
			name = name.replaceAll("}", ")");
		}
		return name.trim();
	}

	/**
	 * convert a path into a lucene compliant String
	 * all namespace are converted to corresponding prefix
	 * 
	 * @param namespaceService the namespace service used to translate namespace into prefix 
	 * @param path the path to translate
	 * @return a string representation of the path with prefixes 
	 */
	public static String translateNamespacePath(NamespaceService namespaceService, Path path) {
		StringBuffer ret = new StringBuffer();
		Iterator<Path.Element> iter = path.iterator();
		iter.next(); // skipping first "/" path
		while (iter.hasNext()) {
			ret.append("/");
			Path.Element el = iter.next();
			ret.append(el.getPrefixedString(namespaceService));
		}
		if (tracer.isDebugEnabled()) tracer.debug("translateNamespacePath : " + path + " = " + ret);
		return ret.toString();
	}
	
	/**
	 * convert a path into a human readable String
	 * 
	 * @param nodeService used to ask to each node his readable name
	 * @param path the path to translate
	 * @return a human readable string representation of the path
	 */
	public static String translatePath(NodeService nodeService, Path path) {
		StringBuffer ret = new StringBuffer();
		Iterator<Path.Element> iter = path.iterator();
		iter.next(); // skipping first "/" path
		while (iter.hasNext()) {
			ret.append("/");
			Path.Element el = iter.next();
			ChildAssociationRef elementRef = ((ChildAssocElement)el).getRef();
			ret.append(nodeService.getProperty(elementRef.getChildRef(), ContentModel.PROP_NAME));
		}
		if (tracer.isDebugEnabled()) tracer.debug("translatePath : " + path + " = " + ret);
		return ret.toString();
	}
	
	/**
	 * 	calculate the NodeRef of a given noderef id
	 *  
	 * @param searchService needed to search for the lucene path
	 * @param storeRef needed to know the soreRef into where search for the path
	 * @param lucenePath the path to find and 
	 * @return the nodeRef of the requested lucene path
	 */
	public static NodeRef getNodeByID(SearchService searchService, NodeService nodeService, StoreRef storeRef, String id) {
		if (tracer.isDebugEnabled()) tracer.debug("getNodeByID, searching noderef of this id : " + id);
		// execute the lucene query (MUST BE a PATH: query format)
		ResultSet rs = null;
		try {
			rs = searchService.query(storeRef, SearchService.LANGUAGE_LUCENE, "ID:\"" + storeRef.getProtocol() + "://" + storeRef.getIdentifier()+"/"+id+"\"");
			// if no nodes found, retur null
			if (rs.length()==0)
				return null;
			else {
				NodeRef ret =  rs.getNodeRef(0);
				if (!nodeService.exists(ret)) return null;
				// if 1 node found, it's good!
				else return ret;
			}
		} finally {
			if (rs!=null) rs.close();
		}
	}
	
	/**
	 * 	ISO9075 encoding of a lucene path
	 *  lucenePath must be a path: query format (without PATH:)
	 *  
	 * @param lucenePath the path to endoce 
	 * @return the lucenePath encoded
	 */
	public static String encodeLucenePath(String lucenePath) {
		if (tracer.isDebugEnabled()) tracer.debug("encodeLucenePath, encoding path : " + lucenePath);
		// if lucenePath is null or empty, nothing to do
		if (lucenePath==null || lucenePath.equals("")) return lucenePath;
		// find first / for deep recursive encoding
		int i = lucenePath.lastIndexOf('/');
		// calculate last spacePrefix and spaceName
		String completeSpaceName = lucenePath.substring(i+1);
		String spaceName = completeSpaceName.substring(completeSpaceName.indexOf(':')+1);
		String spacePrefix = completeSpaceName.substring(0,completeSpaceName.indexOf(':'));
		// deep recursive encoding
		String spaceParent = encodeLucenePath(lucenePath.substring(0,i));
		if (tracer.isDebugEnabled()) tracer.debug("encodeLucenePath, path encoded : " + spaceParent + "/" + spacePrefix + ":" + ISO9075.encode(spaceName));
		return spaceParent + "/" + spacePrefix + ":" + ISO9075.encode(spaceName);
	}
	
	public static NodeRef getParentRef(NodeService nodeService, NodeRef nodeRef) {
		Iterator<ChildAssociationRef> it = nodeService.getParentAssocs(nodeRef).iterator();
		NodeRef parentRef = null;
		ChildAssociationRef childAssoc;
		while (it.hasNext() && parentRef == null) {
			childAssoc = it.next();
			if (childAssoc.getTypeQName().equals(ContentModel.ASSOC_CONTAINS)) {
				parentRef = childAssoc.getParentRef();
			}
		}
		return parentRef;
	}
}
