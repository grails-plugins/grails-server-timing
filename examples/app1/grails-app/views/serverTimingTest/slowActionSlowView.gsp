<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>Slow Action, Slow View Test</title>
</head>

<body>
<div class="container">
    <h1>Slow Action, Slow View Test</h1>

    <p>Both the action and view are slow.</p>

    <p>Action delay was: <strong>${actionDelay}ms</strong></p>

    <%
        // Simulate slow view rendering
        Thread.sleep(viewDelay ?: 100)
    %>

    <p>View delay was: <strong>${viewDelay ?: 100}ms</strong></p>

    <p class="text-muted">The Server-Timing header should show significant time for both action and view.</p>
</div>
</body>
</html>

