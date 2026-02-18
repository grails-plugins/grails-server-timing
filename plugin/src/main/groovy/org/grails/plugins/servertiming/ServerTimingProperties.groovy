package org.grails.plugins.servertiming

import groovy.transform.CompileStatic
import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Configuration properties for the Server Timing plugin.
 *
 * <p>Bound from the {@code grails.plugins.serverTiming} prefix in application
 * or plugin configuration (e.g., {@code application.yml}, {@code plugin.yml}).</p>
 *
 * <p>The {@code enabled} property controls whether the plugin's filter and
 * interceptor are active. Default values are set per Grails environment in
 * {@code plugin.yml}: enabled in {@code development} and {@code test},
 * disabled in {@code production}.</p>
 *
 * <p>The {@code metricKey} property specifies the request attribute key used
 * to store the {@link org.grails.plugins.servertiming.core.TimingMetric}
 * instance on each request. Defaults to {@code GrailsServerTiming}.</p>
 *
 * @see ServerTimingAutoConfiguration
 * @see ServerTimingFilter
 * @see ServerTimingInterceptor
 */
@CompileStatic
@ConfigurationProperties(prefix = 'grails.plugins.server-timing')
class ServerTimingProperties {

    /**
     * Whether the Server Timing plugin is enabled.
     * Defaults to {@code true} in development and test environments,
     * {@code false} in production (configured via {@code plugin.yml}).
     */
    boolean enabled = false

    /**
     * The request attribute key used to store timing metrics on each request.
     */
    String metricKey = 'GrailsServerTiming'
}
