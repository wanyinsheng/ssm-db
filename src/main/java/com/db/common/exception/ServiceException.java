package com.db.common.exception;

public class ServiceException extends RuntimeException {
	private static final long serialVersionUID = 8000361684975719031L;

	public ServiceException() {
		super();
	}

	public ServiceException(String message) {
		super(message);
	}

	public ServiceException(Throwable cause) {
		super(cause);
	}

}
