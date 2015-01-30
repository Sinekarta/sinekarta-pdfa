package org.sinekartapdfa.util;

import org.sinekartapdfa.dto.ResultCode;

public class AlfrescoException extends Exception {
	
	private static final long serialVersionUID = 3263139186098476594L;

	public AlfrescoException (String message, ResultCode resultCode) {
		this(message, null, resultCode, null);
	}
	
	public AlfrescoException (String message, Throwable cause, ResultCode resultCode) {
		this(message, cause, resultCode, null);
	}

	public AlfrescoException (String message, ResultCode resultCode, String receivedData) {
		this(message, null, resultCode, receivedData);
	}
	
	public AlfrescoException (String message, Throwable cause, ResultCode resultCode, String receivedData) {
		super(message, cause);
		this.resultCode = resultCode;
		this.receivedData = receivedData;
	}

	private final ResultCode resultCode;
	private final String receivedData;

	public ResultCode getResultCode() {
		return resultCode;
	}

	public String getReceivedData() {
		return receivedData;
	}
}