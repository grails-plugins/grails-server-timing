package org.grails.plugins.servertiming

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Conditional

/**
 * Spring Boot auto-configuration for the Server Timing plugin.
 *
 * <p>Registers a {@link ServerTimingFilter} and its {@link FilterRegistrationBean}
 * when the plugin is enabled and the application is a servlet-based web application.</p>
 *
 * <p>Enablement is controlled by {@link ServerTimingEnabledCondition}, which delegates
 * to {@link ServerTimingUtils#isEnabled()} — auto-enabled in {@code DEVELOPMENT} and
 * {@code TEST} environments, disabled in {@code PRODUCTION}, unless explicitly
 * overridden via {@code grails.plugins.serverTiming.enabled}.</p>
 *
 * @see ServerTimingFilter
 * @see ServerTimingEnabledCondition
 * @see ServerTimingUtils
 */
@Slf4j
@CompileStatic
@AutoConfiguration
@Conditional(ServerTimingEnabledCondition)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
class ServerTimingAutoConfiguration {

    @Bean
    ServerTimingFilter serverTimingFilter() {
        new ServerTimingFilter()
    }

    @Bean
    FilterRegistrationBean<ServerTimingFilter> serverTimingFilterRegistration(ServerTimingFilter serverTimingFilter) {
        def registration = new FilterRegistrationBean<ServerTimingFilter>(serverTimingFilter)
        registration.urlPatterns = ['/*']
        registration.order = serverTimingFilter.order
        registration.name = 'serverTimingFilter'
        registration
    }
}
