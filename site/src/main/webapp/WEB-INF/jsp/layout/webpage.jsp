<!doctype html>
<%@ include file="/WEB-INF/jspf/htmlTags.jspf" %>
<html lang="de">
  <head>
    <meta charset="utf-8"/>
    <hst:headContributions categoryExcludes="scripts" xhtml="true"/>
    <hst:link var="styleLink" path="/css/style.css"/>
    <link rel="stylesheet" href="${styleLink}" type="text/css"/>
    <hst:link var="pfsStyleLink" path="/css/pfs-de.css"/>
    <link rel="stylesheet" href="${pfsStyleLink}" type="text/css"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  </head>
  <body class="pfs-de">
    <div class="page-container">
      <hst:include ref="header"/>
      <hst:include ref="main"/>
    </div>
    <hst:headContributions categoryIncludes="scripts" xhtml="true"/>
  </body>
</html>