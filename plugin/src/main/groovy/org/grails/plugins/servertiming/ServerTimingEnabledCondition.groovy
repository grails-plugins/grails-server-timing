package org.grails.plugins.servertiming

import grails.util.Environment
import groovy.transform.CompileStatic
import org.springframework.context.annotation.Condition
import org.springframework.context.annotation.ConditionContext
import org.springframework.core.type.AnnotatedTypeMetadata

/**
 * A Spring {@link Condition} that evaluates whether the Server Timing plugin is enabled.
 *
 * <p>Checks the {@code grails.plugins.serverTiming.enabled} property from the Spring
 * {@link org.springframework.core.env.Environment}. If the property is not set, defaults
 * to enabled in {@code DEVELOPMENT} and {@code TEST} Grails environments and disabled
 * in {@code PRODUCTION}.</p>
 *
 * <p>This condition reads directly from the Spring Environment rather than
 * {@code Holders.config} because auto-configuration conditions are evaluated before
 * the Grails config holder is initialized.</p>
 *
 * @see ServerTimingAutoConfiguration
 */
@CompileStatic
class ServerTimingEnabledCondition implements Condition {

    @Override
    boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        def explicitlyEnabled = context.environment.getProperty('grails.plugins.serverTiming.enabled', Boolean)
        if (explicitlyEnabled != null) {
            return explicitlyEnabled
        }

        Environment.current in [Environment.DEVELOPMENT, Environment.TEST]
    }
}
