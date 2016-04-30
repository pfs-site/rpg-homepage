package org.pfs.de.beans;

import org.hippoecm.hst.content.beans.Node;
import org.hippoecm.hst.content.beans.standard.HippoCompound;
import org.hippoecm.hst.content.beans.standard.HippoGalleryImageBean;
import org.hippoecm.hst.content.beans.standard.HippoHtml;

/**
 * Compound bean for event enrollment. Usually used in an
 * {@link EventDocument}. Maps to JCR compound type
 * <code>website:eventenrollment</code>.
 *
 */
@Node(jcrType="website:eventenrollment")
public class EventEnrollmentCompound extends HippoCompound {
	protected final String FIELD_PLAYER_NAME = "website:playerName";
	protected final String FIELD_CHARACTER_NAME = "website:characterName";
	protected final String FIELD_CHARACTER_INFO = "website:text";
	protected final String FIELD_ANONYMOUS = "website:showAnonymous";
	protected final String FIELD_PORTRAIT = "website:portrait";
	
	/**
	 * Get the player name.
	 * @return The player name.
	 */
	public String getPlayerName() {
		return getProperty(FIELD_PLAYER_NAME);
	}
	
	/**
	 * Get the character name. This is an optional field, so <code>null</code>
	 * may be returned.
	 * @return The character name or <code>null</code> if not set.
	 */
	public String getCharacterName() {
		return getProperty(FIELD_CHARACTER_NAME);
	}
	
	/**
	 * Get character info text.
	 * @return The character text, or <code>null</code> if not set.
	 */
	public HippoHtml getCharacterInfo() {
		return getHippoHtml(FIELD_CHARACTER_INFO);
	}
	
	/**
	 * Get the flag if the user should be anonymous on the page.
	 * @return <code>true</code> if the flag is set, <code>false</code> otherwise.
	 * Cannot be <code>null</code>.
	 */
	public Boolean isAnonymous() {
		return getProperty(FIELD_ANONYMOUS, Boolean.FALSE);
	}
	
	/**
	 * Get the portrait.
	 * @return Portrait bean, or <code>null</code> if not set.
	 */
	public HippoGalleryImageBean getPortrait() {
		return getLinkedBean(FIELD_PORTRAIT, HippoGalleryImageBean.class);
	}
}
