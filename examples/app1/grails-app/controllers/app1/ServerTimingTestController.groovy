package app1

import grails.converters.JSON

/**
 * A controller to test the Server-Timing HTTP header functionality.
 * Various actions simulate slow operations to verify timing is captured correctly.
 */
class ServerTimingTestController {

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
     * An action with variable slowness based on a parameter.
     * @param delay The delay in milliseconds (defaults to 100ms)
     */
    def variableDelay() {
        int delay = params.int('delay', 100)
        // Cap the delay at 2 seconds to prevent abuse
        delay = Math.min(delay, 2000)
        Thread.sleep(delay)
        [message: "Action completed after ${delay}ms delay", delay: delay]
    }

    /**
     * A fast action with a slow view (view contains sleep logic).
     */
    def fastActionSlowView() {
        [viewDelay: params.int('viewDelay', 150)]
    }

    /**
     * Both action and view are slow.
     */
    def slowActionSlowView() {
        Thread.sleep(100)
        [actionDelay: 100, viewDelay: params.int('viewDelay', 100)]
    }

    /**
     * An action that performs multiple database-like operations (simulated).
     */
    def multipleOperations() {
        // Simulate multiple operations
        def results = []

        // Simulate first operation
        Thread.sleep(50)
        results << 'Operation 1 complete'

        // Simulate second operation
        Thread.sleep(75)
        results << 'Operation 2 complete'

        // Simulate third operation
        Thread.sleep(25)
        results << 'Operation 3 complete'

        [results: results, totalDelay: 150]
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

    /**
     * An action that redirects to the fast action.
     * This tests that the Server-Timing header is present on the redirect (302) response.
     */
    def redirectToFast() {
        Thread.sleep(50)
        redirect(action: 'fast')
    }

    /**
     * An action that forwards to the forwardTarget action.
     * This tests that the Server-Timing header is present when using server-side forward.
     */
    def forwardToTarget() {
        Thread.sleep(50)
        forward(action: 'forwardTarget')
    }

    /**
     * Target action for forward tests.
     */
    def forwardTarget() {
        Thread.sleep(50)
        [message: 'Forward target reached']
    }

    /**
     * An action that chains to the chainTarget action, passing model data.
     * This tests that the Server-Timing header is present when using Grails chain.
     */
    def chainToTarget() {
        Thread.sleep(50)
        chain(action: 'chainTarget', model: [origin: 'chainToTarget'])
    }

    /**
     * Target action for chain tests.
     */
    def chainTarget() {
        Thread.sleep(50)
        [message: 'Chain target reached']
    }
}
