<%@ include file="/WEB-INF/jspf/htmlTags.jspf" %>

<%--@elvariable id="result" type="org.hippoecm.hst.content.beans.query.HstQueryResult"--%>
<%--@elvariable id="crPage" type="java.lang.Integer"--%>
<%--@elvariable id="info" type="${package}.componentsinfo.GeneralListInfo" --%>
<%--@elvariable id="page" type="java.util.Collection<java.lang.Integer>"--%>
<%--@elvariable id="pages" type="java.util.Collection<java.lang.Integer>"--%>

<c:choose>
  <c:when test="${empty info}">
    <tag:pagenotfound/>
  </c:when>
  <c:otherwise>
    <c:if test="${not empty info.title}">
      <hst:element var="headTitle" name="title">
        <c:out value="${info.title}"/>
      </hst:element>
      <hst:headContribution keyHint="headTitle" element="${headTitle}"/>
    </c:if>


    <c:forEach var="item" items="${result.hippoBeans}" begin="${begin}" end="${begin + info.pageSize - 1}">
      <hst:link var="link" hippobean="${item}"/>
      <article class="well-large-noBottomPadding blackTitle">
        <hst:cmseditlink hippobean="${item}"/>

        <h2>
            <a href="${link}">${fn:escapeXml(item.title)}</a>
        </h2>

        <c:if test="${hst:isReadable(item, 'date')}">
          <p class="smallFont">
            <fmt:formatDate value="${item.date}" type="both" dateStyle="medium"
                            timeStyle="short"/>
          </p>
        </c:if>
      <c:if test="${hst:isReadable(item, 'image.original')}">
        <hst:link var="img" hippobean="${item.image.original}"/>
        <img src="${img}" title="${fn:escapeXml(item.image.fileName)}"
          alt="${fn:escapeXml(item.image.fileName)}" class="centered" />
      </c:if>
        <p>${fn:escapeXml(item.summary)}</p>
      </article>
    </c:forEach>

    <!--if there are pages on the request, they will be printed by the tag:pages -->
    <tag:pages pages="${pages}" page="${page}"/>

  </c:otherwise>
</c:choose>