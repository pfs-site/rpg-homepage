/**
 * <p>This package contains classes abstracting the Akismet REST API. It is intended strictly
 * for internal usage of the Akismet API.</p>
 * 
 * <p>The {@link org.pfs.de.akismet.rest.AkismetRestClient} implements the Akismet REST API.
 * {@link org.pfs.de.akismet.rest.AkismetUrls} is an Enum listing available URLs of the API,
 * while {@link org.pfs.de.akismet.rest.AkismetUrlParameters} lists parameters understood by
 * the API. The possible replies are listed in {@link org.pfs.de.akismet.rest.AkismetReply}.</p>
 * 
 * <p>The API interface is abstracted in {@link org.pfs.de.akismet.rest.AkismetApi}, which can
 * be implemented to mock the API and its responses for testing purposes.</p>
 */
package org.pfs.de.akismet.rest;