package org.grails.plugins.servertiming.core

import grails.compiler.GrailsCompileStatic
import grails.validation.Validateable

import java.time.Duration

/**
 * Implements a metric for the Server Timing header
 *
 * @link https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Headers/Server-Timing
 */
@GrailsCompileStatic
class Metric implements Validateable, Serializable {

    private static final long serialVersionUID = 1L

    /**
     * A name token (no spaces or special characters)
     */
    String name

    void setName(String name) {
        this.key = name?.toUpperCase()
        this.name = name
    }

    /**
     * A human-readable description of the metric
     */
    String description

    /**
     * The time the metric took to process
     */
    Duration getDuration() {
        this.duration
    }

    private Long startTimeNanos
    private String key
    private Duration duration

    static constraints = {
        // Per RFC 7230, token characters are: !#$%&'*+-.^_`|~ plus alphanumeric
        name(nullable: false, blank: false, matches: /^[a-zA-Z0-9!#$%&'*+\-.^_`|~]+$/)
        description(nullable: true, blank: false)
        duration(nullable: true)
    }

    Metric start() {
        if (duration) {
            throw new IllegalStateException('The metric has already started.')
        }

        startTimeNanos = System.nanoTime()
        return this
    }

    Duration calculateElapsedTime() {
        if (startTimeNanos == null) {
            throw new IllegalStateException('The metric has not been started yet.')
        }

        long elapsedNanos = System.nanoTime() - startTimeNanos
        return Duration.ofNanos(elapsedNanos)
    }

    Metric stop() {
        if (startTimeNanos != null) {
            long elapsedNanos = System.nanoTime() - startTimeNanos
            duration = Duration.ofNanos(elapsedNanos)
        }
        return this
    }

    boolean isRunning() {
        startTimeNanos != null && !duration
    }

    boolean isRan() {
        startTimeNanos != null && duration != null
    }

    String toHeaderValue() {
        List<String> parts = [name]
        if (running) {
            // if started, require a stop()
            throw new IllegalStateException("The metric [${name}] has not been stopped yet.")
        }

        if (ran) {
            long nanos = duration.toNanos()
            double millis = nanos / 1_000_000.0d
            parts << "dur=${millis.round(1)}".toString()
        }

        if (description) {
            // Escape backslashes first, then quotes per RFC 7230 quoted-string
            String escapedDesc = description
                    .replace('\\', '\\\\')
                    .replace('"', '\\"')
            parts << "desc=\"${escapedDesc}\"".toString()
        }

        return parts.join(';')
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (o == null || getClass() != o.class) return false

        Metric metric = (Metric) o

        if (key != metric.key) return false

        return true
    }

    int hashCode() {
        return (key != null ? key.hashCode() : 0)
    }
}
