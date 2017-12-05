package com.obi1.videorecorder.exception;

public class VideoRecorderException extends Exception {

	private static final long serialVersionUID = 1L;
	
	private String message;
	
	/**
	 * Construtor padr�o recebendo mensagem de erro.
	 * @param message mensagem
	 */
	public VideoRecorderException(String message) {
		this.message = message;
	}

	/**
	 * Construtor padr�o.
	 * @param cause erro causado
	 */
	public VideoRecorderException(Throwable cause) {
		super(cause);
	}
	
	/**
	 * Construtor padr�o recebendo causa e mensagem de erro.
	 * @param cause causa
	 * @param message mensagem de erro
	 */
	public VideoRecorderException(Throwable cause, String message) {
		super(cause);
		this.message = message;
	}
	
	/**
	 * Retorna a mensagem do erro.
	 * @return mensagem de erro
	 */
	public String getMessage() {
		return message;
	}
}
