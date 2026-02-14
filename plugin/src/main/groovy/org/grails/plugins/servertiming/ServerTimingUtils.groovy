package org.grails.plugins.servertiming

import grails.util.Environment
import grails.util.Holders
import groovy.transform.CompileStatic

/**
 * Various utilities for configuring the Server Timing plugin
 */
@CompileStatic
@Singleton
class ServerTimingUtils {

    boolean isEnabled() {
        Boolean explicitlyEnabled = Holders.config.getProperty('grails.plugins.servertiming.enabled', Boolean, null)
        if (explicitlyEnabled != null) {
            return explicitlyEnabled
        }

        return Environment.current in [Environment.DEVELOPMENT, Environment.TEST]
    }

    String getMetricKey() {
        Holders.config.getProperty('grails.plugins.servertiming.metricKey', String, 'GrailsServerTiming')
    }
}
