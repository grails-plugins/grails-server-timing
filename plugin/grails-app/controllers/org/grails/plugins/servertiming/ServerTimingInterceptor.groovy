package org.grails.plugins.servertiming

import grails.artefact.Interceptor
import grails.compiler.GrailsCompileStatic
import groovy.util.logging.Slf4j
import org.grails.plugins.servertiming.core.TimingMetric

/**
 * Interceptor that tracks timing for controller actions and view rendering.
 * Works in conjunction with ServerTimingFilter which handles adding the HTTP header.
 */
@Slf4j
@GrailsCompileStatic
class ServerTimingInterceptor implements Interceptor {

    static String HEADER_NAME = 'Server-Timing'

    String metricKey = ServerTimingUtils.instance.metricKey

    ServerTimingInterceptor() {
        if (ServerTimingUtils.instance.enabled) {
            log.debug("Server Timing metrics are enabled. Set 'grails.plugins.servertiming.enabled' to false to disable them.")
            matchAll()
        } else {
            log.debug("Server Timing metrics are disabled. Set 'grails.plugins.servertiming.enabled' to true to enable them.")
        }
    }

    @Override
    boolean before() {
        TimingMetric timing = request.getAttribute(metricKey) as TimingMetric
        if (timing) {
            timing.create('action', 'Action')
                    .start()
        }
        true
    }

    @Override
    boolean after() {
        if (response.committed) {
            // no view could be rendered
            return true
        }

        TimingMetric timing = request.getAttribute(metricKey) as TimingMetric
        if (timing) {
            timing.get('action')?.stop()
            timing.create('view', 'View')
                    .start()
        }
        true
    }
}
