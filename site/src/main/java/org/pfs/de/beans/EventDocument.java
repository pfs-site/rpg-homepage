package org.pfs.de.beans;

import java.util.Date;
import java.util.List;

import org.hippoecm.hst.content.beans.Node;
import org.onehippo.forge.feed.api.FeedType;
import org.onehippo.forge.feed.api.annot.SyndicationElement;

/**
 * Bean providing access to event documents.
 */
@Node(jcrType="website:eventdocument")
public class EventDocument extends BlogDocument {
	protected static final String FIELD_ADVENTURE = "website:adventureName";
	protected static final String FIELD_LOCATION = "website:location";
	protected static final String FIELD_START_DATE = "website:startdate";
	protected static final String FIELD_END_DATE = "website:enddate";
	protected static final String FIELD_GAME_MASTER = "website:gamemaster";
	protected static final String FIELD_ENROLLED_PLAYERS = "website:enrolledPlayers";
	
	/**
	 * Get the adventure name.
	 * @return The adventure name.
	 */
	@SyndicationElement(type = FeedType.RSS, name = "adventureName")
	public String getAdventureName() {
		return getProperty(FIELD_ADVENTURE);
	}
	
	/**
	 * Get the event location.
	 * @return The event location.
	 */
	@SyndicationElement(type = FeedType.RSS, name="location")
	public String getEventLocation() {
		return getProperty(FIELD_LOCATION);
	}
	
	/**
	 * Get the list of enrolled players. The list may be empty, but never <code>null</code>.
	 * @return Beans for enrolled players.
	 */
	public List<EventEnrollmentCompound> getEnrolledPlayers() {
		return getChildBeansByName(FIELD_ENROLLED_PLAYERS);
	}
	
	/**
	 * Get the event start date.
	 * @return The start date.
	 */
	@SyndicationElement(type = FeedType.RSS, name="startDate")
	public Date getStartDate() {
		return getProperty(FIELD_START_DATE);
	}
	
	/**
	 * Get the event end date. Optional field, may be <code>null</code>.
	 * @return The end date, or <code>null</code> if not set.
	 */
	@SyndicationElement(type = FeedType.RSS, name="endDate")
	public Date getEndDate() {
		return getProperty(FIELD_END_DATE);
	}
	
	/**
	 * Get the game master for this event.
	 * @return The game master bean.
	 */
	@SyndicationElement(type = FeedType.RSS, name="gameMaster")
	public GameMasterDocument getGameMaster() {
		return getLinkedBean(FIELD_GAME_MASTER, GameMasterDocument.class);
	}
}
