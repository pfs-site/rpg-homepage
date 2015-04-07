<%@ include file="/WEB-INF/jspf/htmlTags.jspf" %>
<%--@elvariable id="document" type="org.pfs.de.beans.BannerDocument"--%>

<div class="banner">
    <div class="logo">
    </div>

    <!--<a href="#" class="headerLink login"></a>-->
    <a href="#" class="headerLink rss"></a>
    <a href="#" class="headerLink contact"></a>
    <a href="#" class="headerLink imprint"></a>
</div>

test1
<c:out value="${document}"/>
<%--<c:out value="${document.rssDocument}"/>--%>
test2: <c:out value="${count}"/>
