package org.sinekartapdfa.alfresco.utils;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.apache.log4j.Logger;
import org.sinekartapdfa.alfresco.exception.PDFException;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.artofsolving.jodconverter.DocumentFamily;
import com.artofsolving.jodconverter.DocumentFormat;
import com.artofsolving.jodconverter.DocumentFormatRegistry;
import com.artofsolving.jodconverter.openoffice.connection.OpenOfficeException;
import com.artofsolving.jodconverter.openoffice.converter.StreamOpenOfficeDocumentConverter;
import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.RectangleReadOnly;
import com.itextpdf.text.pdf.PdfAConformanceLevel;
import com.itextpdf.text.pdf.PdfAWriter;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.RandomAccessFileOrArray;
import com.itextpdf.text.pdf.codec.TiffImage;
import com.sun.star.beans.PropertyValue;

public class PDFAConverter {

	public PDFAConverter(DocumentFormatRegistry formatRegistry,
			StreamOpenOfficeDocumentConverter converter, InputStream is,
			OutputStream os, String sourceMimeType, int resolution,
			boolean forceConversionOfPDF) {
		super();
		this.formatRegistry = formatRegistry;
		this.converter = converter;
		this.is = is;
		this.os = os;
		this.sourceMimeType = sourceMimeType;
		this.resolution = resolution;
		this.forceConversionOfPDF = forceConversionOfPDF;
	}

	private static Logger tracer = Logger.getLogger(PDFAConverter.class);
	
	private DocumentFormatRegistry formatRegistry;
	private StreamOpenOfficeDocumentConverter converter;

	private InputStream is;
	private OutputStream os;
	private String sourceMimeType;
	private int resolution;
	
	private boolean forceConversionOfPDF;
	
	public void convert() throws IOException {
		if (sourceMimeType.equals(Constants.APPLICATION_PDF)) {
			if (forceConversionOfPDF) {
				// is not a PDF/A, the document need to be converted
				if (tracer.isDebugEnabled()) tracer.debug("PDFAConverter : converting document using openoffice (pdf-import plugin)");
				// need to reload contetReader, the contetReader stream was read to understand pdf type
				convertUsingOpenOffice();
			} else {
				// the document is already a pdf and no conversion is needed, copying content...
				Util.inputStreamToOutputStream(is, os);
			}
		} else {
			if (sourceMimeType.startsWith("image")
					/*sourceMimeType.equals(Constants.IMAGE_GIF) ||
				sourceMimeType.equals(Constants.IMAGE_PNG) ||
				sourceMimeType.equals(Constants.IMAGE_JPG) ||
				sourceMimeType.equals(Constants.IMAGE_TIF)*/) {
				if (tracer.isDebugEnabled()) tracer.debug("PDFAConverter : converting image(s) using Itext");
				// if the document it's an image, i will create a PDF/A using itext
				convertUsingItext();
			} else {
				if (tracer.isDebugEnabled()) tracer.debug("PDFAConverter : unknown mimetype converting document using openoffice");
				// in all other formats i will try with openoffice
				convertUsingOpenOffice();
			}
		}
	}

	/**
	 * 
	 * metodo di utilita' per convertire immagini in pdf/A
	 * 
	 * @param actionedUponNodeRef
	 * @param contentReader
	 * @param contentWriter
	 */
	private void convertUsingItext() {
		Document document=null;
		PdfWriter writer=null;
		
		try {
			if (sourceMimeType.equals(Constants.IMAGE_TIF)) {
				
				RandomAccessFileOrArray ra = new RandomAccessFileOrArray(is);
				int comps = TiffImage.getNumberOfPages(ra);
				PdfContentByte cb = null;
				for (int c = 0; c < comps; ++c) {
					Image img = TiffImage.getTiffImage(ra, c + 1);
					if (img != null) {
			  			img.setAbsolutePosition(0, 0);
						// calcolo la dimensione della pagina 
//						e' necessario riadattare la dimensione della pagina in base alla risoluzione dell''immagine
//						se l''immagine e' 100x100 px e la risoluzione dell''immagine e' 300x300 dpi
//						contando che il pdf e' 72x72 dpi, e' necessario riadattare width e height
//						ribaltare questi ragionamenti su tutte le creazioni dei pdf a partire dall''immagine
//						
//						esempio pratico : 
//							ho scannerizzato un A4 in bassa risoluzione (da verificare i dpi)
//							mi ha creato un immagine con una certa dimensione (da verificare i pixel)
//							l''immagine e' stata riportata nel PDF creando una pagina da 580,7 x 822,3 mm che non corrisponde a nessun formato carta
//							Stampando successivamente questo documento scannerizzato la spampante si incarta perche' non sa che carta usare.
//						
//						Sapendo che il PDF lavora su 72 dpi e che la misura del foglio derivata e' di 580,7 x 822,3 mm capiamo quanto era grande l''immagine in pixel : 
//						1 inch = 25.4 mm
//						72 dpi = 72 * 25.4 = 1828,8 dpmm
//						dimensione in px dell''immagine = (580,7 * 1828,8) x  (822,3 * 1828,8) = 1061984 x 1503822 px
//						la risoluzione era (essendo il foglio un A4 = 210 x 297 mm): (72 * 580,7)/210 x (72 * 822,3)/297 = 200 x 200 risoluzione
	//
//						
//						La dimensione della pagina del pdf (da calcolare) deve essere rapportata a 72 DPI, quindi sapendo la risoluzione originaria : 
//						
//							dpiw/dimw = 72/x
//							dimw/dpiw = x/72
//							(dimw * 72)/dpiw = 72
			  			
			  			float width = 0; 
			  			if (img.getDpiX()!=0) {
			  				width = (img.getWidth() * 72)/img.getDpiX();
			  			} else if (resolution!=0) {
			  				width = (img.getWidth() * 72)/resolution;
			  			} else {
			  				width = img.getWidth();
			  			}
			  			float height = 0;
			  			if (img.getDpiY()!=0) {
			  				height = (img.getHeight() * 72)/img.getDpiY();
			  			} else if (resolution!=0) {
			  				height = (img.getHeight() * 72)/resolution;
			  			} else {
			  				height = img.getHeight();
			  			}
						
//	 					Eseguo resize dell'immagine per adeguarla alla dimensione del pdf					
						img.scaleToFit(width, height);
						
						RectangleReadOnly pageSize = new RectangleReadOnly(width, height);
						if ( document==null ){
							// creo il PDF/A che andra' a contenere il documento scannerizzato
							document = new Document(pageSize,0,0,0,0);
							try {
								writer = PdfAWriter.getInstance(document, os, PdfAConformanceLevel.PDF_A_1B);
							} catch (Exception e) {
								tracer.error("Unable to convert from "
										+ sourceMimeType
										+ " to PDF/A. Prolem creating PDF/A document.",e);
								throw new PDFException("Unable to convert from "
										+ sourceMimeType
										+ " to PDF/A. Prolem creating PDF/A document.",e);
							}
							writer.createXmpMetadata();
							document.open();
							cb = writer.getDirectContent();
						}else{
							document.setPageSize(pageSize);
							document.newPage();
						}
						cb.addImage(img);
					}
				}
				ra.close();
			} else {
				ImageReader reader = determineImageReader(is);
				BufferedImage image = reader.read(0);
				
				// calcolo la dimensione della pagina 
//				e' necessario riadattare la dimensione della pagina in base alla risoluzione dell''immagine
//				se l''immagine e' 100x100 px e la risoluzione dell''immagine e' 300x300 dpi
//				contando che il pdf e' 72x72 dpi, e' necessario riadattare width e height
//				ribaltare questi ragionamenti su tutte le creazioni dei pdf a partire dall''immagine
//				
//				esempio pratico : 
//					ho scannerizzato un A4 in bassa risoluzione (da verificare i dpi)
//					mi ha creato un immagine con una certa dimensione (da verificare i pixel)
//					l''immagine e' stata riportata nel PDF creando una pagina da 580,7 x 822,3 mm che non corrisponde a nessun formato carta
//					Stampando successivamente questo documento scannerizzato la spampante si incarta perche' non sa che carta usare.
//				
//				Sapendo che il PDF lavora su 72 dpi e che la misura del foglio derivata e' di 580,7 x 822,3 mm capiamo quanto era grande l''immagine in pixel : 
//				1 inch = 25.4 mm
//				72 dpi = 72 * 25.4 = 1828,8 dpmm
//				dimensione in px dell''immagine = (580,7 * 1828,8) x  (822,3 * 1828,8) = 1061984 x 1503822 px
//				la risoluzione era (essendo il foglio un A4 = 210 x 297 mm): (72 * 580,7)/210 x (72 * 822,3)/297 = 200 x 200 risoluzione
//
//				
//				La dimensione della pagina del pdf (da calcolare) deve essere rapportata a 72 DPI, quindi sapendo la risoluzione originaria : 
//				
//					dpiw/dimw = 72/x
//					dimw/dpiw = x/72
//					(dimw * 72)/dpiw = 72
				
				int[] res = getResolution(reader);
				int dpix=res[0];
				int dpiy=res[1];

				float width = 0; 
	  			if (dpix!=0) {
	  				width = (image.getWidth() * 72)/dpix;
	  			} else if (resolution!=0) {
	  				width = (image.getWidth() * 72)/resolution;
	  			} else {
	  				width = image.getWidth();
	  			}
	  			float height = 0;
	  			if (dpiy!=0) {
	  				height = (image.getHeight() * 72)/dpiy;
	  			} else if (resolution!=0) {
	  				height = (image.getHeight() * 72)/resolution;
	  			} else {
	  				height = image.getHeight();
	  			}

				RectangleReadOnly pageSize = new RectangleReadOnly(width, height);
				document = new Document(pageSize, 0,0,0,0);
				try {
					writer = PdfAWriter.getInstance(document, os, PdfAConformanceLevel.PDF_A_1B);
				} catch (Exception e) {
					tracer.error("Unable to convert from "
							+ sourceMimeType
							+ " to PDF/A. Prolem creating PDF/A document.",e);
					throw new PDFException("Unable to convert from "
							+ sourceMimeType
							+ " to PDF/A. Prolem creating PDF/A document.",e);
				}
				writer.createXmpMetadata();
				document.open();
	  			
				Image imagePdf = Image.getInstance(writer, image, 0.7f);
				imagePdf.setAbsolutePosition(0, 0);
				imagePdf.setCompressionLevel(9);

//					Eseguo resize dell'immagine per adeguarla alla dimensione del pdf					
				imagePdf.scaleToFit(width, height);
				

				document.add(imagePdf); 	
			}
		} catch (Exception e) {
			tracer.error("Unable to convert from "
					+ sourceMimeType
					+ " to PDF/A. Prolem reading/writing image.",e);
			throw new PDFException("Unable to convert from "
					+ sourceMimeType
					+ " to PDF/A. Prolem reading/writing image.",e);
		}
		document.close();
		// this should be already done, but ...
		// closing streams
		try {
			is.close();
		} catch (IOException e) {
			tracer.error("error on input stream", e);
		}
	    try {
	    	os.flush();
		} catch (IOException e) {
			tracer.error("error on output stream",e);
		}
	    try {
	    	os.close();
		} catch (IOException e) {
			tracer.error("error on output stream",e);
		}

	}
	
    public static int[] getResolution(ImageReader r) throws IOException {
        int hdpi=0, vdpi=0;
        double mm2inch = 25.4;

        NodeList lst;
        Element node = (Element)r.getImageMetadata(0).getAsTree("javax_imageio_1.0");
        lst = node.getElementsByTagName("HorizontalPixelSize");
        if(lst != null && lst.getLength() == 1) hdpi = (int)(mm2inch/Float.parseFloat(((Element)lst.item(0)).getAttribute("value")));

        lst = node.getElementsByTagName("VerticalPixelSize");
        if(lst != null && lst.getLength() == 1) vdpi = (int)(mm2inch/Float.parseFloat(((Element)lst.item(0)).getAttribute("value")));

        return new int[]{hdpi, vdpi};
    }

	private ImageReader determineImageReader(InputStream is) throws Exception {
		ImageReader reader;
		ImageInputStream iis = ImageIO.createImageInputStream(is);
        if (iis != null) {
            iis.mark();
        }
		reader = ImageIO.getImageReaders(iis).next();
		reader.setInput(iis, false);
		return reader;
	}

	/**
	 * 
	 * metodo di utilita' per convertire documenti in pdf/A
	 * 
	 * @param actionedUponNodeRef
	 * @param contentReader
	 * @param contentWriter
	 */
	private void convertUsingOpenOffice() {
		// query the registry for the source format
		DocumentFormat sourceFormat = formatRegistry.getFormatByMimeType(sourceMimeType);
		if (sourceFormat == null) {
			tracer.error("Unable to convert from "
					+ sourceMimeType
					+ " to PDF/A. Input document format unknow");
			throw new PDFException("Unable to convert from "
					+ sourceMimeType
					+ " to PDF/A. Input document format unknow");
		}

		// setting mediatype (mimetype) of source format
		sourceFormat.setImportOption("MediaType", sourceMimeType);
		
		// query the registry for the target format
		DocumentFormat targetFormat = formatRegistry.getFormatByMimeType(Constants.APPLICATION_PDF);
		if (targetFormat == null) {
			tracer.error("Unable to convert from "
					+ sourceMimeType
					+ " to PDF/A. OpenOffice does not support PDF??");
			throw new PDFException("Unable to convert from "
					+ sourceMimeType
					+ " to PDF/A. OpenOffice does not support PDF??");
		}
        List<PropertyValue> pdfFilterDataList = new ArrayList<PropertyValue>();
        
		// setting parameters to produce a good pdf/a

        
        // Filter data comments origin:
        // http://www.openoffice.org/nonav/issues/showattachment.cgi/37895/draft-doc-pdf-security.odt
        // http://specs.openoffice.org/appwide/pdf_export/PDFExportDialog.odt

        // Set the password that a user will need to change the permissions
        // of the exported PDF. The password should be in clear text.
        // Must be used with the "RestrictPermissions" property
        PropertyValue pdfFilterDataElement = new PropertyValue();
        pdfFilterDataElement.Name = "PermissionPassword";
        pdfFilterDataElement.Value = "nopermission";
        pdfFilterDataList.add(pdfFilterDataElement);

        // Specify that PDF related permissions of this file must be
        // restricted. It is meaningfull only if the "PermissionPassword"
        // property is not empty
        pdfFilterDataElement = new PropertyValue();
        pdfFilterDataElement.Name = "RestrictPermissions";
        pdfFilterDataElement.Value = Boolean.FALSE;
        pdfFilterDataList.add(pdfFilterDataElement);

        // Specifies that the PDF document should be encrypted while
        // exporting it, meanifull only if the "DocumentOpenPassword"
        // property is not empty
        pdfFilterDataElement = new PropertyValue();
        pdfFilterDataElement.Name = "EncryptFile";
        pdfFilterDataElement.Value = Boolean.FALSE;
        pdfFilterDataList.add(pdfFilterDataElement);

        // Specifies printing of the document:
        //   0: PDF document cannot be printed
        //   1: PDF document can be printed at low resolution only
        //   2: PDF document can be printed at maximum resolution.
        pdfFilterDataElement = new PropertyValue();
        pdfFilterDataElement.Name = "Printing";
        pdfFilterDataElement.Value = new Integer(2);
        pdfFilterDataList.add(pdfFilterDataElement);

        // Specifies the changes allowed to the document:
        //   0: PDF document cannot be changed
        //   1: Inserting, deleting and rotating pages is allowed
        //   2: Filling of form field is allowed
        //   3: Filling of form field and commenting is allowed
        //   4: All the changes of the previous selections are permitted,
        //      with the only exclusion of page extraction
        pdfFilterDataElement = new PropertyValue();
        pdfFilterDataElement.Name = "Changes";
        pdfFilterDataElement.Value = new Integer(0);
        pdfFilterDataList.add(pdfFilterDataElement);

        // Specifies that the pages and the PDF document content can be
        // extracted to be used in other documents: Copy from the PDF
        // document and paste eleswhere
        pdfFilterDataElement = new PropertyValue();
        pdfFilterDataElement.Name = "EnableCopyingOfContent";
        pdfFilterDataElement.Value = Boolean.TRUE;
        pdfFilterDataList.add(pdfFilterDataElement);

        // Specifies that the PDF document content can be extracted to
        // be used in accessibility applications
        pdfFilterDataElement = new PropertyValue();
        pdfFilterDataElement.Name = "EnableTextAccessForAccessibilityTools";
        pdfFilterDataElement.Value = Boolean.TRUE;
        pdfFilterDataList.add(pdfFilterDataElement);
        
        // Specifies which pages are exported to the PDF document.
        // To export a range of pages, use the format 3-6.
        // To export single pages, use the format 7;9;11.
        // Specify a combination of page ranges and single pages
        // by using a format like 2-4;6.
        // If the document has less pages than defined in the range,
        // the result might be the exception
        // "com.sun.star.task.ErrorCodeIOException".
        // This exception occured for example by using an ODT file with
        // only one page and a page range of "2-4;6;8-10". Changing the
        // page range to "1" prevented this exception.
        // For no apparent reason the exception didn't occure by using
        // an ODT file with two pages and a page range of "2-4;6;8-10".
        pdfFilterDataElement = new PropertyValue();
        pdfFilterDataElement.Name = "Pages";
        pdfFilterDataElement.Value = "All";
        pdfFilterDataList.add(pdfFilterDataElement);
        
        // Specifies if graphics are exported to PDF using a
        // lossless compression. If this property is set to true,
        // it overwrites the "Quality" property
        pdfFilterDataElement = new PropertyValue();
        pdfFilterDataElement.Name = "UseLosslessCompression";
        pdfFilterDataElement.Value = Boolean.TRUE;
        pdfFilterDataList.add(pdfFilterDataElement);
        
        // Specifies the quality of the JPG export in a range from 0 to 100.
        // A higher value results in higher quality and file size.
        // This property affects the PDF document only, if the property
        // "UseLosslessCompression" is false
        pdfFilterDataElement = new PropertyValue();
        pdfFilterDataElement.Name = "Quality";
        pdfFilterDataElement.Value = new Integer(80);
        pdfFilterDataList.add(pdfFilterDataElement);

        // Specifies if the resolution of each image is reduced to the
        // resolution specified by the property "MaxImageResolution".
        // If the property "ReduceImageResolution" is set to true and
        // the property "MaxImageResolution" is set to a DPI value, the
        // exported PDF document is affected by this settings even if
        // the property "UseLosslessCompression" is set to true, too
        pdfFilterDataElement = new PropertyValue();
        pdfFilterDataElement.Name = "ReduceImageResolution";
        pdfFilterDataElement.Value = Boolean.FALSE;
        pdfFilterDataList.add(pdfFilterDataElement);
        
//        // If the property "ReduceImageResolution" is set to true
//        // all images will be reduced to the given value in DPI
//        pdfFilterData[12] = new PropertyValue();
//        pdfFilterData[12].Name = "MaxImageResolution";
//        pdfFilterData[12].Value = new Integer(100);

        // Specifies whether form fields are exported as widgets or
        // only their fixed print representation is exported
        pdfFilterDataElement = new PropertyValue();
        pdfFilterDataElement.Name = "ExportFormFields";
        pdfFilterDataElement.Value = Boolean.FALSE;
        pdfFilterDataList.add(pdfFilterDataElement);

        // Specifies that the PDF viewer window is centered to the
        // screen when the PDF document is opened
        pdfFilterDataElement = new PropertyValue();
        pdfFilterDataElement.Name = "CenterWindow";
        pdfFilterDataElement.Value = Boolean.FALSE;
        pdfFilterDataList.add(pdfFilterDataElement);

        // Specifies the action to be performed when the PDF document
        // is opened:
        //   0: Opens with default zoom magnification
        //   1: Opens magnified to fit the entire page within the window
        //   2: Opens magnified to fit the entire page width within
        //      the window
        //   3: Opens magnified to fit the entire width of its boundig
        //      box within the window (cuts out margins)
        //   4: Opens with a zoom level given in the "Zoom" property
        pdfFilterDataElement = new PropertyValue();
        pdfFilterDataElement.Name = "Magnification";
        pdfFilterDataElement.Value = new Integer(0);
        pdfFilterDataList.add(pdfFilterDataElement);

        // Specifies that automatically inserted empty pages are
        // suppressed. This option only applies for storing Writer
        // documents.
        pdfFilterDataElement = new PropertyValue();
        pdfFilterDataElement.Name = "IsSkipEmptyPages";
        pdfFilterDataElement.Value = Boolean.FALSE;
        pdfFilterDataList.add(pdfFilterDataElement);

        // Specifies the PDF version that should be generated:
        //   0: PDF 1.4 (default selection)
        //   1: PDF/A-1 (ISO 19005-1:2005)
        pdfFilterDataElement = new PropertyValue();
        pdfFilterDataElement.Name = "SelectPdfVersion";
        pdfFilterDataElement.Value = new Integer(1);
        pdfFilterDataList.add(pdfFilterDataElement);
        
        // Specifies the change allowed to the document. 
        // Possible values: 
        //	0 = The document cannot be changed. 
        //	1 = Inserting deleting and rotating pages is allowed. 
        //	2 = Filling of form field is allowed. 
        //	3 = Both filling of form field and commenting is allowed. 
        //	4 = All the changes of the previous selections are permitted, with the only exclusion of page extraction (copy). 

        pdfFilterDataElement = new PropertyValue();
        pdfFilterDataElement.Name = "Changes";
        pdfFilterDataElement.Value = new Integer(0);
        pdfFilterDataList.add(pdfFilterDataElement);

        PropertyValue pdfFilterDataArray[] = new PropertyValue[pdfFilterDataList.size()];
        pdfFilterDataList.toArray(pdfFilterDataArray);
        
        targetFormat.setExportOption(sourceFormat.getFamily(), "FilterData", pdfFilterDataArray);

		// get the family of the target document
		DocumentFamily sourceFamily = sourceFormat.getFamily();
		
		// does the format support the conversion ?
		if (!targetFormat.isExportableFrom(sourceFamily)) {
			tracer.error("Unable to convert from "
					+ sourceMimeType
					+ " to PDF/A. OpenOffice does not support this type of transformation.");
			throw new PDFException(
					"Unable to convert from "
							+ sourceMimeType
							+ " to PDF/A. OpenOffice does not support this type of transformation.");
		}

		// this is the real conversion!!! call to jodconverter 
		try {
			converter.convert(is, sourceFormat, os, targetFormat);
			// conversion success
		} catch (OpenOfficeException e) {
			tracer.error("OpenOffice server conversion failed", e);
			throw new PDFException(
					"OpenOffice server conversion failed", e);
		}

		// this should be already done, but ...
		// closing streams
		try {
			is.close();
		} catch (IOException e) {
			tracer.error("error on input stream", e);
		}
	    try {
	    	os.flush();
		} catch (IOException e) {
			tracer.error("error on output stream",e);
		}
	    try {
	    	os.close();
		} catch (IOException e) {
			tracer.error("error on output stream",e);
		}

	}

}
