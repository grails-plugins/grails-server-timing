<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>Multiple Operations Test</title>
</head>

<body>
<div class="container">
    <h1>Multiple Operations Test</h1>

    <p>Multiple operations were performed in the action.</p>

    <h3>Results:</h3>
    <ul>
        <g:each in="${results}" var="result">
            <li>${result}</li>
        </g:each>
    </ul>

    <p>Total simulated delay: <strong>${totalDelay}ms</strong></p>

    <p class="text-muted">The Server Timing header should show the cumulative action time.</p>
</div>
</body>
</html>

