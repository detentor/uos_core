/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unbiquitous.uos.core.ontologyEngine.exception;

/**
 *
 * @author anaozaki
 */
public class ReasonerNotDefinedException extends Exception {
	private static final long serialVersionUID = -1148156680804824595L;

	public ReasonerNotDefinedException() {
		super();
	}

	public ReasonerNotDefinedException(String message, Throwable cause) {
		super(message, cause);
	}

	public ReasonerNotDefinedException(String message) {
		super(message);
	}

	public ReasonerNotDefinedException(Throwable cause) {
		super(cause);
	}
}
