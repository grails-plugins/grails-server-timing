package org.grails.plugins.servertiming.config

import groovy.transform.CompileStatic

import org.springframework.boot.context.properties.ConfigurationProperties

@CompileStatic
@ConfigurationProperties('grails.plugins.server-timing')
class ServerTimingConfig {

    Boolean enabled = null
    String metricKey = 'GrailsServerTiming'

}
