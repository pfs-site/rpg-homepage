package org.pfs.de.akismet.rest;

/**
 * Possible replies from the Akismet REST API.
 * 
 * @author Martin Dreier <martin@martindreier.de>
 *
 */
enum AkismetReply {
	VALID("valid"),
	INVALID("invalid"),
	TRUE("true"),
	FALSE("false");
	
	private String value;

	private AkismetReply(String value) {
		this.value = value;
	}
	
	/**
	 * Check if the given value equals this reply.
	 * @param value The value to test.
	 * @return <code>true</code> if the value is this reply, 
	 * <code>false</code> otherwise.
	 */
	public boolean is(String value) {
		if (value == null) {
			return false;
		}
		return this.value.equals(value.trim());
	}
}