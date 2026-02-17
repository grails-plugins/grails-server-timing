package org.grails.plugins.servertiming

import grails.plugins.Plugin
import groovy.util.logging.Slf4j

/**
 * Grails plugin descriptor for the Server Timing plugin.
 *
 * <p>Provides plugin metadata (version compatibility, author, documentation, etc.)
 * for the Grails plugin framework.</p>
 *
 * <p>Bean wiring is handled by {@link ServerTimingAutoConfiguration} using standard
 * Spring Boot auto-configuration rather than the Grails {@code doWithSpring()} mechanism.</p>
 *
 * @see ServerTimingAutoConfiguration
 * @see ServerTimingFilter
 * @see ServerTimingUtils
 */
@Slf4j
class ServerTimingGrailsPlugin extends Plugin {

    /** Minimum Grails version required for this plugin */
    def grailsVersion = '7.0.0  > *'

    /** Plugin title */
    def title = 'Server Timing'

    /** Plugin author */
    def author = 'James Daugherty'

    /** Plugin description */
    def description = 'A Grails plugin to generate Server Timing headers for HTTP responses.'

    /** URL to the plugin documentation */
    def documentation = 'https://grails-plugins.github.io/grails-server-timing/'

    /** Plugin license type */
    def license = 'APACHE'

    /** Source control management information */
    def scm = [url: 'https://github.com/grails-plugins/grails-server-timing']

    /**
     * Performs initialization tasks after the Spring application context is available.
     *
     * <p>Logs whether the plugin is enabled or disabled based on the configuration.</p>
     */
    @Override
    void doWithApplicationContext() {
        if (ServerTimingUtils.instance.enabled) {
            log.debug('Applying {} plugin', title)
        } else {
            log.debug('{} plugin is disabled. Set \'grails.plugins.serverTiming.enabled\' to true to enable it.', title)
        }
    }
}
