<%@ include file="/WEB-INF/jspf/htmlTags.jspf" %>
<%--@elvariable id="document" type="org.pfs.de.beans.BannerDocument"--%>

<div class="banner">
    <div class="logo">
    </div>

    <!--<a href="#" class="headerLink login"></a>-->
    <hst:link var="rssLink" hippobean="${document.rssDocument}" />
    <a href="<c:out value="${rssLink}"/>" class="headerLink rss"></a>
    <hst:link var="contactLink" hippobean="${document.contactDocument}" />
    <a href="<c:out value="${contactLink}"/>" class="headerLink contact"></a>
    <hst:link var="imprintLink" hippobean="${document.imprintDocument}" />
    <a href="<c:out value="${imprintLink}"/>" class="headerLink imprint"></a>
</div>

