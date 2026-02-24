<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>Fast Action, Slow View Test</title>
</head>

<body>
<div class="container">
    <h1>Fast Action, Slow View Test</h1>

    <p>The action executed quickly, but the view is slow...</p>
    <%
        // Simulate slow view rendering
        Thread.sleep(viewDelay ?: 150)
    %>
    <p>View delay was: <strong>${viewDelay ?: 150}ms</strong></p>

    <p class="text-muted">The Server Timing header should show a fast action time and a slow view time.</p>
</div>
</body>
</html>

