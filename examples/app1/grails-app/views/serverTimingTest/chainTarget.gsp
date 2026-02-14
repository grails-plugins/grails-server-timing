<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>Chain Target Test</title>
</head>

<body>
<div class="container">
    <h1>Chain Target Test</h1>
    <p>${message}</p>
    <g:if test="${origin}">
        <p>Chained from: <strong>${origin}</strong></p>
    </g:if>
    <p class="text-muted">This action was reached via a Grails chain.</p>
</div>
</body>
</html>
