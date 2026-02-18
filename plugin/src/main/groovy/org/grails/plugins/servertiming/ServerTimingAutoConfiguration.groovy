package org.grails.plugins.servertiming

import groovy.transform.CompileStatic
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean

/**
 * Spring Boot auto-configuration for the Server Timing plugin.
 *
 * <p>Unconditionally registers {@link ServerTimingProperties} so that all
 * components (including Grails artefacts like {@link ServerTimingInterceptor})
 * can access plugin configuration.</p>
 *
 * <p>Conditionally registers a {@link ServerTimingFilter} and its
 * {@link FilterRegistrationBean} when the plugin is enabled and the application
 * is a servlet-based web application. When the plugin is disabled, no filter is
 * registered, avoiding any request-processing overhead.</p>
 *
 * <p>Enablement is controlled by the {@code grails.plugins.serverTiming.enabled}
 * property via {@code @ConditionalOnProperty}. Default values are set per Grails
 * environment in {@code plugin.yml}: enabled in {@code development} and
 * {@code test}, disabled in {@code production}.</p>
 *
 * @see ServerTimingFilter
 * @see ServerTimingProperties
 */
@CompileStatic
@AutoConfiguration
@EnableConfigurationProperties(ServerTimingProperties)
class ServerTimingAutoConfiguration {

    @Bean
    @ConditionalOnProperty(name = 'grails.plugins.serverTiming.enabled', havingValue = 'true')
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    ServerTimingFilter serverTimingFilter(ServerTimingProperties properties) {
        new ServerTimingFilter(properties)
    }

    @Bean
    @ConditionalOnProperty(name = 'grails.plugins.serverTiming.enabled', havingValue = 'true')
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    FilterRegistrationBean<ServerTimingFilter> serverTimingFilterRegistration(ServerTimingFilter serverTimingFilter) {
        def registration = new FilterRegistrationBean<ServerTimingFilter>(serverTimingFilter)
        registration.urlPatterns = ['/*']
        registration.order = serverTimingFilter.order
        registration.name = 'serverTimingFilter'
        registration
    }
}
