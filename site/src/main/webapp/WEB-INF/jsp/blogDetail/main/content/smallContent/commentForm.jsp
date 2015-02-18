<%@ include file="/WEB-INF/jspf/htmlTags.jspf" %>
<%--@elvariable id="document" type="org.pfs.de.beans.BlogDocument"--%>

<%-- Include jQuery JavaScript from CDN --%>
<hst:element name="script" var="jQueryScript">
	<hst:attribute name="type" value="text/javascript" />
	<hst:attribute name="src" value="https://code.jquery.com/jquery-2.1.1.min.js" />
</hst:element>
<hst:headContribution element="${jQueryScript}" />

<%-- Java script for the comments form --%>
<hst:link var="commentsJsLink" path="/js/comment.js"/>
<hst:element name="script" var="commentsJs">
	<hst:attribute name="type" value="text/javascript" />
	<hst:attribute name="src" value="${commentsJsLink}" />
</hst:element>
<hst:headContribution element="${commentsJs}" />

<hst:link var="commentsPostLink" path="/restapi/documents/${referenceDocument}/comments"/>

<c:if test="${commentsAllowed}" >
	<form id="website-comments-form">
		<input type="hidden" name="comment-post-url" id="comment-post-url" value="${commentsPostLink}" />
		<table>
			<tr><td><fmt:message key="comment.author"/>*</td><td><input type="text" id="comment-author" /></td></tr>
			<tr><td><fmt:message key="comment.link"/></td><td><input type="text" id="comment-link" /></td></tr>
			<tr><td><fmt:message key="comment.text"/>*</td><td><textarea rows="5" cols="30" id="comment-text"></textarea></td></tr>
			<tr><td colspan="2">* Mandatory fields</td></tr>
			<tr><td colspan="2">
				<input type="submit" id="comment-submit" value="<fmt:message key="comment.post" />" />
				<input type="reset" value="<fmt:message key="comment.resetForm" />" />
			</td></tr>
		</table>
	</form>
	<div id="website-comments-instructions"><fmt:message key="comment.enableJavaScript" /></div>
</c:if>