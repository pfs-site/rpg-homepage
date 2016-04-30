<%@ include file="/WEB-INF/jspf/htmlTags.jspf" %>
<%--@elvariable id="document" type="org.pfs.de.beans.BlogDocument"--%>
<%--@elvariable id="headTitle" type="java.lang.String"--%>

<c:choose>
  <c:when test="${empty document}">
    <!-- not found -->
    <tag:pagenotfound/>
  </c:when>
  <c:otherwise>
    
    <!-- found -->
    <c:if test="${not empty document.title}">
      <hst:element var="headTitle" name="title">
        <c:out value="${document.title}"/>
      </hst:element>
      <hst:headContribution keyHint="headTitle" element="${headTitle}"/>
    </c:if>

    <article class="well-large">
      <hst:cmseditlink hippobean="${document}"/>
      <header>
        <h1>${fn:escapeXml(document.title)}</h1>
        <p class="smallfont">
          <fmt:message key="publishedOn.text">
              <fmt:param value="${document.date}" />
              <fmt:param value="${document.author}" />
          </fmt:message>
        </p>
      </header>
      <c:if test="${hst:isReadable(document, 'image.original')}">
        <hst:link var="img" hippobean="${document.image.original}"/>
        <img src="${img}" title="${fn:escapeXml(document.image.fileName)}"
          alt="${fn:escapeXml(document.image.description)}" class="centered" />
      </c:if>

      <hst:html hippohtml="${document.html}"/>
    </article>

	<hst:include ref="commentList"/>
	<c:if test="${document.commentsAllowed}">
		<hst:include ref="commentForm"/>
	</c:if>
  </c:otherwise>
</c:choose>
