/**
 * <p>The {@link org.pfs.de.akismet.AkismetClient AkismetClient} is the central interface to
 * interact with the Akismet service. It wraps the Akismet REST API and exposes functions
 * to </p>
 * <ul>
 * <li>{@link org.pfs.de.akismet.AkismetClient#checkApiKey() verify the API key},</li>
 * <li>{@link org.pfs.de.akismet.AkismetClient#checkComment(AkismetCommentData) check a comment for spam}</li>
 * <li>{@link org.pfs.de.akismet.AkismetClient#reportHam(AkismetCommentData) report Ham}, and</li>
 * <li>{@link org.pfs.de.akismet.AkismetClient#reportSpam(AkismetCommentData) report Spam}</li>.
 * </ul>
 * <p>Comment data must be passed in the form of an {@link org.pfs.de.akismet.AkismetCommentData AkismetCommentData}
 * object containing all necessary information. The check result is returned in the form of
 * an {@link org.pfs.de.akismet.AkismetCheckResult AkismetCheckResult} object.</p>
 * <p>Configuration for the Aismet client is stored in nodes with the type <code>website:akismetcheck</code>.
 * It can be read by calling 
 * {@link org.pfs.de.akismet.AkismetConfiguration#readConfiguration(javax.jcr.Session, javax.jcr.Node) AkismetConfiguration.readConfiguration()}. 
 * The nodes can have the following properties:</p>
 * <ul>
 * <li><code>website:akismetApiKey</code>: The key for the Akismet API.</li>
 * <li><code>website:akismetHamAction</code>: Action to be taken for comments identified as Ham. May
 * be <code>publish</code> (set the comment up for autopubishing) or <code>request</code> (request
 * publishing of the comment by an editor)</li>
 * <li><code>website:akismetSpamAction</code>: Action to be taken for comments identified as Spam.
 * May be <code>ignore</code> (comment is saved but not published), <code>reject</code> (comment is not
 * saved) or <code>recommendation</code> (comment is only rejected if Aismet recommends it, otherwise it
 * is saved but not published).</li>
 * </ul>
 * <p>Full documentation is available at 
 * <a href="https://akismet.com/development/api/">https://akismet.com/development/api</a>.</p>
 */
package org.pfs.de.akismet;