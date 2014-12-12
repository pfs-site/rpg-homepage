/**
 * 
 */
package org.pfs.de.akismet;

/**
 * An exception from the Akismet client.
 * 
 * @author Martin Dreier <martin@martindreier.de>
 *
 */
public class AkismetException extends Exception {

	/**
	 * For serialization.
	 */
	private static final long serialVersionUID = -684529295251677585L;

	/**
	 * Create a new Akismet exception.
	 * @param message The error message.
	 * @param cause The exception which caused the error.
	 */
	public AkismetException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Create a new Akismet exception.
	 * @param message The error message.
	 */
	public AkismetException(String message) {
		super(message);
	}

	/**
	 * Create a new Akismet exception.
	 * @param cause The exception which caused the error.
	 */
	public AkismetException(Throwable cause) {
		super(cause);
	}
}
