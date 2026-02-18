package org.grails.plugins.servertiming

import groovy.transform.CompileStatic

import grails.artefact.Interceptor
import groovy.util.logging.Slf4j
import org.grails.plugins.servertiming.core.TimingMetric
import org.springframework.beans.factory.annotation.Autowired

/**
 * Interceptor that tracks timing for controller actions and view rendering.
 * Works in conjunction with ServerTimingFilter which handles adding the HTTP header.
 */
@Slf4j
@CompileStatic
class ServerTimingInterceptor implements Interceptor {

    static String HEADER_NAME = 'Server-Timing'

    @Autowired
    ServerTimingProperties serverTimingProperties

    ServerTimingInterceptor() {
        matchAll()
    }

    @Override
    boolean before() {
        if (!serverTimingProperties.enabled) {
            return true
        }

        def timing = request.getAttribute(serverTimingProperties.metricKey) as TimingMetric
        if (timing) {
            timing.create('action', 'Action')
                    .start()
        }
        true
    }

    @Override
    boolean after() {
        if (!serverTimingProperties.enabled) {
            return true
        }

        if (response.committed) {
            // no view could be rendered
            return true
        }

        def timing = request.getAttribute(serverTimingProperties.metricKey) as TimingMetric
        if (timing) {
            timing.get('action')?.stop()
            timing.create('view', 'View')
                    .start()
        }
        true
    }
}
