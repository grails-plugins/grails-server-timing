package org.grails.plugins.servertiming.core

import grails.validation.ValidationException
import groovy.transform.CompileStatic

/**
 * Implements a collection of metrics for the Server Timing header
 *
 * @link https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Headers/Server-Timing
 */
@CompileStatic
class TimingMetric implements Serializable {

    private static final long serialVersionUID = 1L

    private LinkedHashMap<String, Metric> metrics = [:]

    Metric create(String name, String description = null) {
        def metric = new Metric(name: name, description: description)
        metrics.put(name, metric)

        if (!metric.validate()) {
            throw new ValidationException('Invalid Metric', metric.errors)
        }
        return metric
    }

    void remove(String name) {
        metrics.remove(name)
    }

    Metric get(String name) {
        metrics[name]
    }

    boolean has(String name) {
        metrics.containsKey(name)
    }

    String toHeaderValue() {
        if (!metrics) {
            return null
        }

        return metrics.collect { it.value.toHeaderValue() }.join(',')
    }
}
