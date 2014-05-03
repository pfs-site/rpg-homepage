<%@ include file="/WEB-INF/jspf/htmlTags.jspf" %>
<%--@elvariable id="document" type="org.pfs.de.beans.TextDocument"--%>
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

    <article class="well well-large">
      <hst:cmseditlink hippobean="${document}"/>
      <header>
        <h2>${fn:escapeXml(document.title)}</h2>
      </header>
      <hst:html hippohtml="${document.html}"/>
      
	<br />
	<div class="grey">
	  <fmt:message key="lastEdited.text">
	    <fmt:param value="${document.date}" />
          </fmt:message>

	</div>

    </article>

  </c:otherwise>
</c:choose>