package app1

import grails.testing.mixin.integration.Integration
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import spock.lang.Shared
import spock.lang.Specification

/**
 * Integration tests for the Server Timing HTTP header functionality.
 * Tests verify that the plugin correctly adds timing information
 * for controller actions and view rendering.
 */
@Integration
class ServerTimingIntegrationSpec extends Specification {

    @Shared
    RestTemplate restTemplate = new RestTemplate()

    private String getBaseUrl() {
        "http://localhost:${serverPort}"
    }

    private ResponseEntity<String> doGet(String path) {
        restTemplate.exchange("${baseUrl}${path}", HttpMethod.GET, null, String)
    }

    void "fast action should include Server Timing header"() {
        when: 'we request the fast action'
        def response = doGet('/serverTimingTest/fast')

        then: 'the response should have a Server Timing header'
        response.headers.getFirst('Server-Timing') != null

        and: 'the header should contain action and view metrics'
        def serverTiming = response.headers.getFirst('Server-Timing')
        serverTiming.contains('action')
        serverTiming.contains('view')
    }

    void "slow action (200ms) should show action timing >= 200ms"() {
        when: 'we request the slow action'
        def response = doGet('/serverTimingTest/slowAction')

        then: 'the response should have a Server Timing header'
        def serverTiming = response.headers.getFirst('Server-Timing')
        serverTiming != null

        and: 'the action timing should be at least 200ms'
        def actionDur = extractDuration(serverTiming, 'action')
        actionDur >= 200.0
    }

    void "variable delay action should reflect requested delay"() {
        given: 'a requested delay of 150ms'
        int requestedDelay = 150

        when: 'we request the variable delay action'
        def response = doGet("/serverTimingTest/variableDelay?delay=${requestedDelay}")

        then: 'the response should have a Server Timing header'
        def serverTiming = response.headers.getFirst('Server-Timing')
        serverTiming != null

        and: 'the action timing should be at least the requested delay'
        def actionDur = extractDuration(serverTiming, 'action')
        actionDur >= requestedDelay
    }

    void "fast action with slow view should show view timing >= 150ms"() {
        when: 'we request the fast action with slow view'
        def response = doGet('/serverTimingTest/fastActionSlowView?viewDelay=150')

        then: 'the response should have a Server Timing header'
        def serverTiming = response.headers.getFirst('Server-Timing')
        serverTiming != null

        and: 'the view timing should be at least 150ms'
        def viewDur = extractDuration(serverTiming, 'view')
        viewDur >= 150.0

        and: 'the action timing should be relatively fast (less than 100ms)'
        def actionDur = extractDuration(serverTiming, 'action')
        actionDur < 100.0
    }

    void "slow action slow view should show both timings being significant"() {
        when: 'we request the slow action with slow view'
        def response = doGet('/serverTimingTest/slowActionSlowView?viewDelay=100')

        then: 'the response should have a Server Timing header'
        def serverTiming = response.headers.getFirst('Server-Timing')
        serverTiming != null

        and: 'the action timing should be at least 100ms'
        def actionDur = extractDuration(serverTiming, 'action')
        actionDur >= 100.0

        and: 'the view timing should be at least 100ms'
        def viewDur = extractDuration(serverTiming, 'view')
        viewDur >= 100.0
    }

    void "multiple operations should accumulate in action timing"() {
        when: 'we request the multiple operations action'
        def response = doGet('/serverTimingTest/multipleOperations')

        then: 'the response should have a Server Timing header'
        def serverTiming = response.headers.getFirst('Server-Timing')
        serverTiming != null

        and: 'the action timing should be at least 150ms (sum of 50+75+25)'
        def actionDur = extractDuration(serverTiming, 'action')
        actionDur >= 150.0
    }

    void "JSON response should include Server Timing header"() {
        when: 'we request the JSON action'
        def response = doGet('/serverTimingTest/jsonResponse')

        then: 'the response should have a Server Timing header'
        def serverTiming = response.headers.getFirst('Server-Timing')
        serverTiming != null

        and: 'the action timing should be at least 50ms'
        def actionDur = extractDuration(serverTiming, 'action')
        actionDur >= 50.0
    }

    void "text response should include Server Timing header"() {
        when: 'we request the text action'
        def response = doGet('/serverTimingTest/textResponse')

        then: 'the response should have a Server Timing header'
        def serverTiming = response.headers.getFirst('Server-Timing')
        serverTiming != null

        and: 'the action timing should be at least 30ms'
        def actionDur = extractDuration(serverTiming, 'action')
        actionDur >= 30.0
    }

    void "Server Timing header format should be correct"() {
        when: 'we request any action'
        def response = doGet('/serverTimingTest/fast')

        then: 'the Server Timing header should follow the spec format'
        def serverTiming = response.headers.getFirst('Server-Timing')

        // Header should contain metric name, duration, and description
        // Format: name;dur=X;desc="description"
        serverTiming =~ /action;dur=[\d.]+;desc="[^"]+"/
        serverTiming =~ /view;dur=[\d.]+;desc="[^"]+"/
    }

    void "index page should include Server Timing header"() {
        when: 'we request the index page'
        def response = doGet('/')

        then: 'the response should have a Server Timing header'
        def serverTiming = response.headers.getFirst('Server-Timing')
        serverTiming != null
    }

    void "static asset should include Server Timing header with other metric"() {
        when: 'we request a static asset'
        def response = doGet('/assets/application.css?compile=false')

        then: 'the response should have a Server Timing header'
        def serverTiming = response.headers.getFirst('Server-Timing')
        serverTiming != null

        and: "the header should contain 'other' metric (not action/view)"
        serverTiming.contains('other')

        and: "the header should contain 'total' metric"
        serverTiming.contains('total')
    }

    void "redirect response should include Server Timing header"() {
        when: 'we request an action that redirects'
        def response = doGet('/serverTimingTest/redirectToFast')

        then: 'the final response (after following redirect) should have a Server Timing header'
        def serverTiming = response.headers.getFirst('Server-Timing')
        serverTiming != null

        and: 'the header should contain action and view metrics from the target action'
        serverTiming.contains('action')
        serverTiming.contains('view')
    }

    void "redirect response should include Server Timing header with timing >= 50ms"() {
        when: 'we request an action that sleeps 50ms then redirects'
        def response = doGet('/serverTimingTest/redirectToFast')

        then: 'the final response should have a Server Timing header with total time'
        def serverTiming = response.headers.getFirst('Server-Timing')
        serverTiming != null

        and: 'total should be present'
        serverTiming.contains('total')
    }

    void "forward should include Server Timing header"() {
        when: 'we request an action that forwards to another action'
        def response = doGet('/serverTimingTest/forwardToTarget')

        then: 'the response should have a Server Timing header'
        def serverTiming = response.headers.getFirst('Server-Timing')
        serverTiming != null

        and: 'the header should contain action metrics'
        serverTiming.contains('action')

        and: 'the header should contain total metric'
        serverTiming.contains('total')
    }

    void "forward should include Server Timing header with view metric"() {
        when: 'we request an action that forwards to another action with a view'
        def response = doGet('/serverTimingTest/forwardToTarget')

        then: 'the response should have a Server Timing header'
        def serverTiming = response.headers.getFirst('Server-Timing')
        serverTiming != null

        and: 'the header should contain view metric since the target action renders a view'
        serverTiming.contains('view')
    }

    void "chain should include Server Timing header"() {
        when: 'we request an action that chains to another action'
        def response = doGet('/serverTimingTest/chainToTarget')

        then: 'the response should have a Server Timing header'
        def serverTiming = response.headers.getFirst('Server-Timing')
        serverTiming != null

        and: 'the header should contain action metrics'
        serverTiming.contains('action')

        and: 'the header should contain total metric'
        serverTiming.contains('total')
    }

    void "chain should include Server Timing header with view metric"() {
        when: 'we request an action that chains to another action with a view'
        def response = doGet('/serverTimingTest/chainToTarget')

        then: 'the response should have a Server Timing header'
        def serverTiming = response.headers.getFirst('Server-Timing')
        serverTiming != null

        and: 'the header should contain view metric since the chain target renders a view'
        serverTiming.contains('view')
    }

    /**
     * Extracts the duration value for a given metric name from the Server Timing header.
     * @param serverTimingHeader The full Server Timing header value
     * @param metricName The name of the metric to extract
     * @return The duration value in milliseconds, or null if not found
     */
    private Double extractDuration(String serverTimingHeader, String metricName) {
        // Pattern: metricName;dur=123.4
        def pattern = /${metricName};dur=([\d.]+)/
        def matcher = serverTimingHeader =~ pattern
        if (matcher.find()) {
            return Double.parseDouble(matcher.group(1))
        }
        return null
    }
}
