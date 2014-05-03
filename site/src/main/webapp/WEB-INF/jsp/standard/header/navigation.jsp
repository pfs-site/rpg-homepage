<%@ include file="/WEB-INF/jspf/htmlTags.jspf" %>
    
    <div class="navigation">
<c:if test="${not empty mainMenu}">
    
    <c:forEach var="item" items="${mainMenu}">
      
      <hst:link var="link" link="${item.hstLink}"/>
      <c:set var="name" value="${item.name}"/>
      
      <c:choose>
	<c:when test="${item.expanded}">
	  <c:set var="css" value="item active"/>
	  <c:set var="subMenu" value="${item.childMenuItems}"/>
	</c:when>
	<c:otherwise>
	  <c:set var="css" value="item"/>
	</c:otherwise>
      </c:choose>

      <a href="${link}" class="${css}">${name}</a>
	   
    </c:forEach>
  </c:if>
    </div>
    <div class="subnavigation">
  <c:if test="${not empty subMenu}">
    
      <c:forEach var="item" items="${subMenu}">

	<hst:link var="link" link="${item.hstLink}"/>
	<c:set var="name" value="${item.name}"/>
	
	<c:choose>
	  <c:when test="${item.expanded}">
	    <c:set var="css" value="item active"/>
	  </c:when>
	  <c:otherwise>
	    <c:set var="css" value="item"/>
	  </c:otherwise>
	</c:choose>

	<a href="${link}" class="${css}">${name}</a>

      </c:forEach>

  </c:if>

  </div>
