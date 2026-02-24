package org.grails.plugins.servertiming.config

import groovy.transform.CompileStatic

import org.springframework.context.annotation.Condition
import org.springframework.context.annotation.ConditionContext
import org.springframework.core.env.Environment
import org.springframework.core.type.AnnotatedTypeMetadata

@CompileStatic
class EnabledCondition implements Condition {

    @Override
    boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        matches(context.environment)
    }

    static boolean matches(Environment env) {
        def explicitConfigValue = env.getProperty('grails.plugins.server-timing.enabled', Boolean, null)
        if (explicitConfigValue != null && explicitConfigValue == false) {
            return false
        }
        explicitConfigValue || env.matchesProfiles('development', 'test')
    }
}
