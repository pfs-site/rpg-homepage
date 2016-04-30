package org.pfs.de.beans;

import javax.jcr.RepositoryException;

import org.hippoecm.hst.content.beans.Node;
import org.hippoecm.hst.content.beans.standard.HippoGalleryImageBean;
import org.onehippo.forge.feed.api.FeedType;
import org.onehippo.forge.feed.api.annot.SyndicationElement;
import org.pfs.de.services.model.BaseDocumentRepresentation;

/**
 * Bean representing game masters, maps to JCR type
 * <code>website:gmdocument</code>.
 */
@Node(jcrType="website:gmdocument")
public class GameMasterDocument extends BaseDocument {

	protected static final String FIELD_NAME = "website:name";
	protected static final String FIELD_EMAIL = "website:emailaddress";
	protected static final String FIELD_PORTRAIT = "website:portrait";
	
	/**
	 * Get the name of the game master.
	 * @return The GM name.
	 */
	@SyndicationElement(type = FeedType.RSS, name="gmName")
	public String getGameMasterName() {
		return getProperty(FIELD_NAME);
	}
	
	/**
	 * Get the email address.
	 * @return The e-mail address.
	 */
	@SyndicationElement(type = FeedType.RSS, name="emailAddress")
	public String getEMailAddress() {
		return getProperty(FIELD_EMAIL);
	}
	
	/**
	 * Get the game master portrait.
	 * @return The portrait, or <code>null</code> if not set.
	 */
	public HippoGalleryImageBean getPortrait() {
		return getLinkedBean(FIELD_PORTRAIT, HippoGalleryImageBean.class);
	}
	
	/**
	 * @see org.pfs.de.beans.BaseDocument#update(org.pfs.de.services.model.BaseDocumentRepresentation)
	 */
	@Override
	public void update(BaseDocumentRepresentation representation) throws RepositoryException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

}
