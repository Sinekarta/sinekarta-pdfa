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
package org.sinekartapdfa.alfresco.action;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.log4j.Logger;
import org.sinekartapdfa.alfresco.exception.DocumentToPDFAException;
import org.sinekartapdfa.alfresco.exception.PDFException;
import org.sinekartapdfa.alfresco.utils.Constants;
import org.sinekartapdfa.alfresco.utils.NodeTools;
import org.sinekartapdfa.alfresco.utils.PDFAConverter;
import org.sinekartapdfa.alfresco.utils.PDFTools;
/*
import org.sinekarta.alfresco.configuration.dao.SinekartaDao;
import org.sinekarta.alfresco.exception.DocumentToPDFAException;
import org.sinekarta.alfresco.pdf.util.PDFAConverter;
import org.sinekarta.alfresco.pdf.util.PDFTools;
import org.sinekarta.alfresco.util.Constants;
import org.sinekarta.alfresco.util.NodeTools;
*/
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.DefaultResourceLoader;

import com.artofsolving.jodconverter.DocumentFormatRegistry;
import com.artofsolving.jodconverter.XmlDocumentFormatRegistry;
import com.artofsolving.jodconverter.openoffice.connection.AbstractOpenOfficeConnection;
import com.artofsolving.jodconverter.openoffice.converter.StreamOpenOfficeDocumentConverter;

/**
 * document conversion to PDF/A action.
 * this action will convert a document added to archive into a PDF/A document
 * 
 * - no input parameter needed
 * - no output parameter returned
 * 
 * @author andrea.tessaro
 *
 */
public class DocumentToPDFA extends ActionExecuterAbstractBase implements InitializingBean {

	private static Logger tracer = Logger.getLogger(DocumentToPDFA.class);
	public static final String ACTION_NAME_DOCUMENT_TO_PDFA = "sinekartaDocumentToPDFA";
	
	private NodeService nodeService;
	private ContentService contentService;
	private AbstractOpenOfficeConnection connection;
	private StreamOpenOfficeDocumentConverter converter;
	private String documentFormatsConfiguration;
	private DocumentFormatRegistry formatRegistry;

	@Override
	protected void executeImpl(Action action, NodeRef actionedUponNodeRef) {
		if (tracer.isDebugEnabled()) tracer.debug("DocumentToPDFA action, execution init");
		
		// first of all : converting document to PDF/A using jodconverter
		if (!isAvailable()) {
			tracer.error("Openoffice connection not available");
			throw new DocumentToPDFAException("Openoffice connection not available");
		}

		// TODO, determinare dpi!!!
		// per ora assumiamo fissi 300 dpi, minimo indispensabile per fare un OCR decente
			
		String destName;
		try {
			destName = doWork(actionedUponNodeRef);
		} catch(PDFException e) {
			tracer.error("error on document to pdfa conversion : " + e.getMessage());
			throw new DocumentToPDFAException("error on document to pdfa conversion : " + e.getMessage());
		}
		action.setParameterValue(PARAM_RESULT, destName);

		if (tracer.isDebugEnabled()) tracer.debug("DocumentAcquiring action, execution end");
	}
	
	public String doWork(NodeRef actionedUponNodeRef) throws PDFException {
		if (tracer.isDebugEnabled()) tracer.debug("DocumentToPDFA action.doWork, execution init");

		String fileName;
		try {
			ContentReader contentReader = contentService.getReader(actionedUponNodeRef, ContentModel.PROP_CONTENT);
	
			String documentMimetype = (String) contentReader.getMimetype();
			if (documentMimetype.equals(Constants.APPLICATION_PDF) && 
				(PDFTools.isPdfa(contentService.getReader(actionedUponNodeRef, ContentModel.PROP_CONTENT).getContentInputStream()) || 
				 PDFTools.isPdfSigned(contentService.getReader(actionedUponNodeRef, ContentModel.PROP_CONTENT).getContentInputStream()))) {
				// is a PDF/A, this will not be converted
				if (tracer.isDebugEnabled()) tracer.debug("DocumentToPDFA action.doWork, the input document is PDF/A, no conversion needed");
				fileName = (String) nodeService.getProperty(actionedUponNodeRef, ContentModel.PROP_NAME);
			} else {
				// preparing the new document content, the pdf/a content 
				ContentWriter contentWriter = contentService.getWriter(actionedUponNodeRef, ContentModel.PROP_CONTENT, true);
				contentWriter.setMimetype(Constants.APPLICATION_PDF);
				contentWriter.setEncoding("UTF-8");
				contentWriter.setLocale(contentReader.getLocale());

				String sourceExtension = (String) contentReader.getMimetype();
				// download the content from the source reader
				InputStream is = contentReader.getContentInputStream();
				OutputStream os = contentWriter.getContentOutputStream();

				// is not a PDF/A, the document need to be converted
				if (tracer.isDebugEnabled()) tracer.debug("DocumentToPDFA action.doWork, pdf document conversion");

				PDFAConverter conv = new PDFAConverter(formatRegistry, converter, 
									is, os, sourceExtension, 0, 
									true);

				conv.convert();// renaming the file, after conversion the file is a .pdf
				fileName = PDFTools.calculatePdfName(nodeService, actionedUponNodeRef);
				fileName = NodeTools.validateFileFolderName(fileName);
				nodeService.setProperty(actionedUponNodeRef, ContentModel.PROP_NAME, fileName);
				
			}
		} catch (Throwable e) {
			tracer.error("DocumentToPDFA action.doWork, unable to execute work", e);
			throw new PDFException("DocumentToPDFA action.doWork, unable to execute work : " + e.getMessage(), e);
		}

		if (tracer.isDebugEnabled()) tracer.debug("DocumentToPDFA action.doWork, finished OK");
		return fileName;
	}
	
	@Override
	protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
	}

    public void afterPropertiesSet() throws Exception {

		DefaultResourceLoader resourceLoader = new DefaultResourceLoader();
		try {
			InputStream is = resourceLoader.getResource(documentFormatsConfiguration).getInputStream();
			formatRegistry = new XmlDocumentFormatRegistry(is);
		} catch (IOException e) {
			tracer.error("Unable to load document formats configuration file: "+ documentFormatsConfiguration, e);
			throw new AlfrescoRuntimeException("Unable to load document formats configuration file: " + documentFormatsConfiguration,e);
		}

		// set up the converter
		if (converter == null) {
			converter = new StreamOpenOfficeDocumentConverter(connection);
		}
	}

	public boolean isAvailable() {
		if (!connection.isConnected()) {
			try {
				connection.connect();
			} catch (ConnectException e) {
				return false;
			}
			return connection.isConnected();
		} else 
			return true;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}

	public void setConnection(AbstractOpenOfficeConnection connection) {
		this.connection = connection;
	}

	public void setDocumentFormatsConfiguration(String path) {
		this.documentFormatsConfiguration = path;
	}
}
//class DocumentToPDFAWorker implements RunAsWork<Boolean> {
//
//	private static Logger tracer = Logger.getLogger(DocumentToPDFA.class);
//
//	private NodeService nodeService;
//	private ContentService contentService;
//	private NodeRef actionedUponNodeRef;
//	private StreamOpenOfficeDocumentConverter  converter;
//	private DocumentFormatRegistry formatRegistry;
//	private int resolution;
//	private String koReason;
//
//	public DocumentToPDFAWorker(NodeService nodeService,
//			ContentService contentService, NodeRef actionedUponNodeRef,
//			StreamOpenOfficeDocumentConverter converter,
//			DocumentFormatRegistry formatRegistry, int resolution) {
//		super();
//		this.nodeService = nodeService;
//		this.contentService = contentService;
//		this.actionedUponNodeRef = actionedUponNodeRef;
//		this.converter = converter;
//		this.formatRegistry = formatRegistry;
//		this.resolution = resolution;
//	}
//
//	@Override
//	public Boolean doWork() {
//		if (tracer.isDebugEnabled()) tracer.debug("DocumentToPDFA action.doWork, execution init");
//
//		try {
//			ContentReader contentReader = contentService.getReader(actionedUponNodeRef, ContentModel.PROP_CONTENT);
//	
//			String documentMimetype = (String) contentReader.getMimetype();
//			
//			if (documentMimetype.equals(Constants.APPLICATION_PDF) && 
//				(PDFTools.isPdfa(contentService.getReader(actionedUponNodeRef, ContentModel.PROP_CONTENT).getContentInputStream()) || 
//				 PDFTools.isPdfSigned(contentService.getReader(actionedUponNodeRef, ContentModel.PROP_CONTENT).getContentInputStream()))) {
//				// is a PDF/A, this will not be converted
//				if (tracer.isDebugEnabled()) tracer.debug("DocumentToPDFA action.doWork, the input document is PDF/A, no conversion needed");
//			} else {
//				// preparing the new document content, the pdf/a content 
//				ContentWriter contentWriter = contentService.getWriter(actionedUponNodeRef, ContentModel.PROP_CONTENT, true);
//				contentWriter.setMimetype(Constants.APPLICATION_PDF);
//				contentWriter.setEncoding("UTF-8");
//				contentWriter.setLocale(contentReader.getLocale());
//
//				String sourceExtension = (String) contentReader.getMimetype();
//				// download the content from the source reader
//				InputStream is = contentReader.getContentInputStream();
//				OutputStream os = contentWriter.getContentOutputStream();
//
//				// is not a PDF/A, the document need to be converted
//				if (tracer.isDebugEnabled()) tracer.debug("DocumentToPDFA action.doWork, pdf document conversion");
//
//				PDFAConverter conv = new PDFAConverter(formatRegistry, converter, 
//									is, os, sourceExtension, resolution, 
//									true);
//
//				conv.convert();
//			}
//			
//			// renaming the file, after conversion the file is a .pdf
//			String fileName = PDFTools.calculatePdfName((String) nodeService.getProperty(actionedUponNodeRef, ContentModel.PROP_NAME));
//			fileName = NodeTools.validateFileFolderName(fileName);
//			nodeService.setProperty(actionedUponNodeRef, ContentModel.PROP_NAME, fileName);
//		} catch (Throwable e) {
//			tracer.error("DocumentToPDFA action.doWork, unable to execute work", e);
//			koReason="DocumentToPDFA action.doWork, unable to execute work : " + e.getMessage();
//			return false;
//		}
//
//		if (tracer.isDebugEnabled()) tracer.debug("DocumentToPDFA action.doWork, finished OK");
//
//		return true;
//	}
//	
//	public String getKoReason() {
//		return koReason;
//	}
//
//}
