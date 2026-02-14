package org.grails.plugins.servertiming

import grails.plugins.Plugin
import groovy.util.logging.Slf4j
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.core.Ordered

/**
 * Grails plugin that provides Server-Timing header support for HTTP responses.
 *
 * <p>This plugin automatically registers a {@link ServerTimingFilter} that adds
 * <a href="https://www.w3.org/TR/server-timing/">Server-Timing</a> headers to HTTP responses,
 * allowing developers to communicate backend server performance metrics to the browser.</p>
 *
 * <h3>Configuration</h3>
 * <p>The plugin can be enabled or disabled via the configuration property:</p>
 * <pre>
 * grails.plugins.servertiming.enabled = true
 * </pre>
 *
 * <h3>Filter Registration</h3>
 * <p>When enabled, the plugin registers a servlet filter with the following characteristics:</p>
 * <ul>
 *   <li>URL Pattern: <code>/*</code> (applies to all requests)</li>
 *   <li>Order: <code>Ordered.HIGHEST_PRECEDENCE + 100</code> (executes early in the filter chain)</li>
 * </ul>
 *
 * @see ServerTimingFilter* @see ServerTimingUtils
 */
@Slf4j
class GrailsServerTimingGrailsPlugin extends Plugin {

    /** Minimum Grails version required for this plugin */
    def grailsVersion = '7.0.7  > *'

    /** Plugin title */
    def title = 'grails-server-timing'

    /** Plugin author */
    def author = 'James Daugherty'

    /** Plugin description */
    def description = 'A Grails plugin to generate Server-Timing headers for HTTP responses.'

    /** URL to the plugin documentation */
    def documentation = 'https://grails-plugins.github.io/grails-server-timing/'

    /** Plugin license type */
    def license = 'APACHE'

    /** Source control management information */
    def scm = [url: 'https://github.com/grails-plugins/grails-server-timing']

    /**
     * Registers Spring beans for the Server-Timing functionality.
     *
     * <p>When the plugin is enabled, this method registers:</p>
     * <ul>
     *   <li><strong>serverTimingFilter</strong> - The {@link ServerTimingFilter} bean</li>
     *   <li><strong>serverTimingFilterRegistration</strong> - A {@link FilterRegistrationBean}
     *       that configures the filter to intercept all requests</li>
     * </ul>
     *
     * @return a closure that defines the Spring bean configuration
     */
    Closure doWithSpring() {
        { ->
            if (ServerTimingUtils.instance.enabled) {
                serverTimingFilter(ServerTimingFilter)

                serverTimingFilterRegistration(FilterRegistrationBean) {
                    filter = ref('serverTimingFilter')
                    urlPatterns = ['/*']
                    order = Ordered.HIGHEST_PRECEDENCE + 100
                    name = 'serverTimingFilter'
                }
            }
        }
    }

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
            log.debug('{} plugin is disabled. Set \'grails.plugins.servertiming.enabled\' to true to enable it.', title)
        }
    }
}
