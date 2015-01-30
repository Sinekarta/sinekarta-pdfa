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
 * Part of this code come from 
 * FirmaPdf version 0.0.x Copyright (C) 2006 Antonino Iacono (ant_iacono@tin.it)
 * and Roberto Resoli
 * See method description for more details
 * 
 * Part of this code come from 
 * com.itextpdf.text.pdf.security.MakeSignature
 * Paulo Soares
 * 
 * See method description for more details
 * 
 */
package org.sinekartapdfa.alfresco.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.log4j.Logger;
import org.sinekartapdfa.alfresco.exception.PDFException;

import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfReader;

public class PDFTools {

	private static Logger tracer = Logger.getLogger(PDFTools.class);

	public static final String PDFA_SUFFIX = "_pdfa";
	public static final String PDF = ".pdf";


	/**
	 * metodo di utilita' che verifica se il pdf in input e' gia' firmato
	 * 
	 * @param reader
	 * @return
	 */
	public static boolean isPdfSigned(InputStream is) {
		if (tracer.isDebugEnabled())
			tracer.debug("chacking if PDF/A is signed");
		try {
			PdfReader reader = new PdfReader(is);
			boolean ret = false;
			if (PDFTools.isPdfSigned(reader)) {
				ret = true;
			}
			reader.close();
			return ret;
		} catch (Exception e) {
			tracer.error("Unable to read PDF. Unable to check if the pdf is signed.",e);
			throw new PDFException("Unable to read PDF. Unable to check if the pdf is signed.",e);
		} finally {
			try {
				if (is != null)
					is.close();
			} catch (IOException e) {
			}
		}
	}

	/**
	 * metodo di utilita' che verifica se il pdf in input e' gia' firmato
	 * 
	 * @param reader
	 * @return
	 */
	public static boolean isPdfSigned(PdfReader reader) {
		if (tracer.isDebugEnabled())
			tracer.debug("chacking if PDF/A is signed");
		try {
			AcroFields af = reader.getAcroFields();

			// Search of the whole signature
			ArrayList<String> names = af.getSignatureNames();

			// For every signature :
			if (names.size() > 0) {
				if (tracer.isDebugEnabled())tracer.debug("yes, it is");
				return true;
			} else {
				if (tracer.isDebugEnabled())tracer.debug("no, it isn't");
				return false;
			}
		} catch (Exception e) {
			tracer.error("Unable to read PDF. Unable to check if the pdf is signed.",e);
			throw new PDFException("Unable to read PDF. Unable to check if the pdf is signed.",e);
		}
	}

	/**
	 * metodo di utilita' che verifica se il pdf in input e' un PDF/A
	 * 
	 * @param reader
	 * @return
	 */
	public static boolean isPdfa(InputStream is) {
		if (tracer.isDebugEnabled()) tracer.debug("checking if PDF is PDF/A");
		PdfReader reader = null;
		ByteArrayInputStream bais = null;
		XMLStreamReader sr = null;
		try {
			reader = new PdfReader(is);
			byte[] metadata = reader.getMetadata();
			if (metadata == null || metadata.length == 0)
				return false;
			bais = new ByteArrayInputStream(metadata);
			sr = XMLInputFactory.newInstance().createXMLStreamReader(bais);
			boolean isConformanceTag = false;
			int eventCode;
			while (sr.hasNext()) {
				eventCode = sr.next();
				String val = null;
				switch (eventCode) {
				case 1:
					val = sr.getLocalName();
					if (val.equals("conformance") && sr.getNamespaceURI().equals("http://www.aiim.org/pdfa/ns/id/"))
						isConformanceTag = true;
					break;
				case 4:
					val = sr.getText();
					if (isConformanceTag) {
						if (val.equals("A") || val.equals("B")) {
							if (tracer.isDebugEnabled()) tracer.debug("yes, it is");
							return true;
						} else {
							if (tracer.isDebugEnabled()) tracer.debug("no, it isn't");
							return false;
						}
					}
					break;
				}
			}
		} catch (Exception e) {
			tracer.error("Unable to read PDF. Unable to check if the pdf is a pdf/a.",e);
			throw new PDFException("Unable to read PDF. Unable to check if the pdf is a pdf/a.",e);
		} finally {
			try {
				if (reader != null)
					reader.close();
			} catch (Exception e) {
				tracer.error("error on pdf reader", e);
			}
			try {
				if (sr != null)
					sr.close();
			} catch (Exception e) {
				tracer.error("error on stax reader", e);
			}
			try {
				if (bais != null)
					bais.close();
			} catch (Exception e) {
				tracer.error("error on input stream", e);
			}
			try {
				if (is != null)
					is.close();
			} catch (Exception e) {
				tracer.error("error on input stream", e);
			}
		}
		if (tracer.isDebugEnabled())
			tracer.debug("no, it isn't");
		return false;
	}


	public static String calculatePdfName(NodeService nodeService, NodeRef actionedUponNodeRef) {
		String fileName = (String) nodeService.getProperty(actionedUponNodeRef, ContentModel.PROP_NAME);
		NodeRef parentRef = NodeTools.getParentRef(nodeService, actionedUponNodeRef); 
		
//		String destName;
//		if (dotIdx >= 0) {
//			return fileName.substring(0, dotIdx)
//					+ Configuration.getInstance().getPdfaSuffix()
//					+ PDFTools.PDF;
//		} else {
//			return fileName + Configuration.getInstance().getPdfaSuffix()
//					+ PDFTools.PDF;
//		}
		
		Pattern ptt = Pattern.compile("^(\\.*[^\\.]+)(\\..*)$");
		Matcher mtc = ptt.matcher(fileName);
		String destName = fileName;
		if ( mtc.find() ) {
			destName = String.format("%s%s%s", mtc.group(1), PDFA_SUFFIX, PDF);
		} else {
			destName = String.format("%s%s%s", fileName, PDFA_SUFFIX, PDF);
		}
		
		int attempt = 0;
		while ( nodeService.getChildByName(parentRef, ContentModel.ASSOC_CONTAINS, destName) != null) {
			attempt++;
			mtc = Pattern.compile("^(\\.*[^\\.]+)(\\..*)$").matcher(fileName);
			if ( mtc.find() ) {
				destName = String.format("%s%s_%d%s", mtc.group(1), PDFA_SUFFIX, attempt, PDF);
			} else {
				destName = String.format("%s%s_%d%s", fileName, PDFA_SUFFIX, attempt, PDF);
			}
		}
		
		return destName;
	}

	public static Calendar _getSignDate(String signDate) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSSZ");
		Date d = null;
		try {
			d = sdf.parse(signDate);
		} catch (ParseException e) {
			// not possible
		}
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		return c;
	}

}
