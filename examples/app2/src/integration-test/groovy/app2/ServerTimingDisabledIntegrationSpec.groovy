package app2

import grails.testing.mixin.integration.Integration
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import spock.lang.Shared
import spock.lang.Specification

/**
 * Integration tests verifying that the Server Timing HTTP header is NOT present
 * when the plugin is explicitly disabled via configuration
 * (grails.plugins.servertiming.enabled: false).
 */
@Integration
class ServerTimingDisabledIntegrationSpec extends Specification {

    @Shared
    RestTemplate restTemplate = new RestTemplate()

    private String getBaseUrl() {
        "http://localhost:${serverPort}"
    }

    private ResponseEntity<String> doGet(String path) {
        restTemplate.exchange("${baseUrl}${path}", HttpMethod.GET, null, String)
    }

    void "fast action should NOT include Server Timing header when plugin is disabled"() {
        when: 'we request the fast action'
        ResponseEntity<String> response = doGet('/serverTimingDisabledTest/fast')

        then: 'the response should NOT have a Server Timing header'
        response.headers.getFirst('Server-Timing') == null
    }

    void "slow action should NOT include Server Timing header when plugin is disabled"() {
        when: 'we request the slow action'
        ResponseEntity<String> response = doGet('/serverTimingDisabledTest/slowAction')

        then: 'the response should NOT have a Server Timing header'
        response.headers.getFirst('Server-Timing') == null
    }

    void "JSON response should NOT include Server Timing header when plugin is disabled"() {
        when: 'we request the JSON action'
        ResponseEntity<String> response = doGet('/serverTimingDisabledTest/jsonResponse')

        then: 'the response should NOT have a Server Timing header'
        response.headers.getFirst('Server-Timing') == null
    }

    void "text response should NOT include Server Timing header when plugin is disabled"() {
        when: 'we request the text action'
        ResponseEntity<String> response = doGet('/serverTimingDisabledTest/textResponse')

        then: 'the response should NOT have a Server Timing header'
        response.headers.getFirst('Server-Timing') == null
    }

    void "index page should NOT include Server Timing header when plugin is disabled"() {
        when: 'we request the index page'
        ResponseEntity<String> response = doGet('/')

        then: 'the response should NOT have a Server Timing header'
        response.headers.getFirst('Server-Timing') == null
    }

    void "static asset should NOT include Server Timing header when plugin is disabled"() {
        when: 'we request a static asset'
        ResponseEntity<String> response = doGet('/assets/application.css?compile=false')

        then: 'the response should NOT have a Server Timing header'
        response.headers.getFirst('Server-Timing') == null
    }
}
