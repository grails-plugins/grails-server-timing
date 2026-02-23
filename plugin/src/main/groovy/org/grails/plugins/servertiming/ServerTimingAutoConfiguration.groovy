package org.grails.plugins.servertiming

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.ApplicationListener
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Conditional
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered

import org.grails.plugins.servertiming.config.EnabledCondition
import org.grails.plugins.servertiming.config.ServerTimingConfig

@Slf4j
@CompileStatic
@AutoConfiguration
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(ServerTimingConfig)
class ServerTimingAutoConfiguration {

    @Bean
    @Conditional(EnabledCondition)
    ServerTimingFilter serverTimingFilter() {
        new ServerTimingFilter()
    }

    @Bean
    @Conditional(EnabledCondition)
    FilterRegistrationBean<ServerTimingFilter> serverTimingFilterRegistration(ServerTimingFilter serverTimingFilter) {
        new FilterRegistrationBean<ServerTimingFilter>().tap {
            filter = serverTimingFilter
            urlPatterns = ['/*']
            order = Ordered.HIGHEST_PRECEDENCE + 100
            name = 'serverTimingFilter'
        }
    }

    @Bean
    ApplicationListener<ApplicationReadyEvent> serverTimingAutoConfigLogger(ConfigurableApplicationContext context) {
        { event ->
            def applied = !context.getBeanProvider(ServerTimingFilter).stream().findAny().empty
            def message = applied ?
                    'Applying {} plugin' :
                    '{} plugin is disabled. Set \'grails.plugins.server-timing.enabled\' to true to enable it.'
            log.debug(message, ServerTimingGrailsPlugin.pluginName)
        } as ApplicationListener
    }
}
