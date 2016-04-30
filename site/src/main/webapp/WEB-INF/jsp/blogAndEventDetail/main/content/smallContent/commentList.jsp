<%@ include file="/WEB-INF/jspf/htmlTags.jspf" %>
<%--@elvariable id="comments" type="org.hippoecm.hst.content.beans.query.HstQueryResult"--%>

  <c:if test="${empty comments }">
  	NO COMMENTS!!!!
  </c:if>

  <div id="commentContainer">
	<c:forEach var="comment" items="${comments.hippoBeans}">
      <article class="well well-large">
        <h4>${fn:escapeXml(comment.author)}</h4>
        <p class="badge badge-info">
           <fmt:formatDate value="${comment.date}" type="both" dateStyle="medium"
                timeStyle="short"/>
        </p>
     	<c:if test="${not empty comment.link}">
     		<p>
     			<a href="${fn:escapeXml(comment.link)}">${fn:escapeXml(comment.link)}</a>
     		</p>
     	</c:if>
     	
        <p>${fn:escapeXml(comment.text)}</p>
      </article>
    </c:forEach>
  </div>