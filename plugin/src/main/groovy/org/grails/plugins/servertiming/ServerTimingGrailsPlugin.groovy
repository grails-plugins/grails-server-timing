package org.grails.plugins.servertiming

import groovy.util.logging.Slf4j

import grails.plugins.Plugin

/**
 * Grails plugin that provides Server Timing header support for HTTP responses.
 *
 * <p>This plugin automatically registers a {@link ServerTimingFilter} that adds
 * <a href="https://www.w3.org/TR/server-timing/">Server Timing</a> headers to HTTP responses,
 * allowing developers to communicate backend server performance metrics to the browser.</p>
 *
 * <h3>Configuration</h3>
 * <p>The plugin can be enabled or disabled via the configuration property:</p>
 * <pre>
 * grails.plugins.server-timing.enabled = true
 * </pre>
 *
 * <h3>Filter Registration</h3>
 * <p>When enabled, the plugin registers a servlet filter with the following characteristics:</p>
 * <ul>
 *   <li>URL Pattern: <code>/*</code> (applies to all requests)</li>
 *   <li>Order: <code>Ordered.HIGHEST_PRECEDENCE + 100</code> (executes early in the filter chain)</li>
 * </ul>
 *
 * @see ServerTimingFilter
 */
@Slf4j
class ServerTimingGrailsPlugin extends Plugin {

    static final pluginName = 'Server Timing'

    /** Minimum Grails version required for this plugin */
    def grailsVersion = '7.0.0  > *'

    /** Plugin title */
    def title = pluginName

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
}
