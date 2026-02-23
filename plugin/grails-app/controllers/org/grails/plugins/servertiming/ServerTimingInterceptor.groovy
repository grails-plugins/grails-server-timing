package org.grails.plugins.servertiming

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment

import grails.artefact.Interceptor
import org.grails.plugins.servertiming.config.EnabledCondition
import org.grails.plugins.servertiming.config.ServerTimingConfig
import org.grails.plugins.servertiming.core.TimingMetric

/**
 * Interceptor that tracks timing for controller actions and view rendering.
 * Works in conjunction with ServerTimingFilter which handles adding the HTTP header.
 */
@Slf4j
@CompileStatic
class ServerTimingInterceptor implements Interceptor {

    private String metricKey

    @Autowired
    ServerTimingInterceptor(Environment env, ServerTimingConfig config) {
        if (EnabledCondition.matches(env)) {
            log.debug("Server Timing metrics are enabled. Set 'grails.plugins.serverTiming.enabled' to false to disable them.")
            matchAll()
        } else {
            log.debug("Server Timing metrics are disabled. Set 'grails.plugins.serverTiming.enabled' to true to enable them.")
        }

        metricKey = config.metricKey
    }

    @Override
    boolean before() {
        def timing = request.getAttribute(metricKey) as TimingMetric
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

        def timing = request.getAttribute(metricKey) as TimingMetric
        if (timing) {
            timing.get('action')?.stop()
            timing.create('view', 'View')
                    .start()
        }
        true
    }
}
