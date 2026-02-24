package app2

import grails.converters.JSON

/**
 * A controller to test that the Server Timing HTTP header is NOT present
 * when the plugin is explicitly disabled via configuration.
 */
class ServerTimingDisabledTestController {

    /**
     * A fast action that returns immediately.
     */
    def fast() {
        [message: 'Fast action completed']
    }

    /**
     * A slow action that takes approximately 200ms to execute.
     */
    def slowAction() {
        Thread.sleep(200)
        [message: 'Slow action completed after 200ms delay']
    }

    /**
     * Returns JSON response directly (no view rendering).
     */
    def jsonResponse() {
        Thread.sleep(50)
        render([message: 'JSON response', timestamp: System.currentTimeMillis()] as JSON)
    }

    /**
     * Returns plain text response.
     */
    def textResponse() {
        Thread.sleep(30)
        render(text: 'Plain text response', contentType: 'text/plain')
    }
}
