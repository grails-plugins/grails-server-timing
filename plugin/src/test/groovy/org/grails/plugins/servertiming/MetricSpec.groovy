package org.grails.plugins.servertiming

import org.grails.plugins.servertiming.core.Metric
import spock.lang.Specification
import spock.lang.Unroll

import java.time.Duration

class MetricSpec extends Specification {

    def "test basic metric creation with name"() {
        when:
        Metric metric = new Metric(name: 'testMetric')

        then:
        metric.name == 'testMetric'
        metric.validate()
    }

    def "test metric creation with name and description"() {
        when:
        Metric metric = new Metric(name: 'testMetric', description: 'Test Description')

        then:
        metric.name == 'testMetric'
        metric.description == 'Test Description'
        metric.validate()
    }

    @Unroll
    def "test valid metric names: #name"() {
        when:
        Metric metric = new Metric(name: name)

        then:
        metric.validate()

        where:
        // Per RFC 7230, token characters are: !#$%&'*+-.^_`|~ plus alphanumeric
        name << [
                'test',
                'test123',
                'test-metric',
                'test_metric',
                'TEST',
                'Test123-_',
                'metric!',
                'metric#value',
                'metric$',
                'metric%',
                'metric&test',
                "metric'test",
                'metric*',
                'metric+test',
                'metric.test',
                'metric^test',
                'metric`test',
                'metric|test',
                'metric~test',
                "!#\$%&'*+-.^_`|~"  // All special token chars
        ]
    }

    @Unroll
    def "test invalid metric names: #name"() {
        when:
        Metric metric = new Metric(name: name)

        then:
        !metric.validate()
        metric.errors.hasFieldErrors('name')

        where:
        // Invalid: spaces, control chars, delimiters like ( ) < > @ , ; : \ " / [ ] ? = { }
        name << [
                'test metric',    // space not allowed
                'test@metric',    // @ not allowed
                'test/metric',    // / not allowed
                'test\\metric',   // \ not allowed
                'test"metric',    // " not allowed
                'test[metric',    // [ not allowed
                'test]metric',    // ] not allowed
                'test{metric',    // { not allowed
                'test}metric',    // } not allowed
                'test(metric',    // ( not allowed
                'test)metric',    // ) not allowed
                'test<metric',    // < not allowed
                'test>metric',    // > not allowed
                'test,metric',    // , not allowed
                'test;metric',    // ; not allowed
                'test:metric',    // : not allowed
                'test?metric',    // ? not allowed
                'test=metric',    // = not allowed
                '',               // blank not allowed
                null              // null not allowed
        ]
    }

    def "test description can be null"() {
        when:
        Metric metric = new Metric(name: 'testMetric', description: null)

        then:
        metric.validate()
    }

    def "test description cannot be blank"() {
        when:
        Metric metric = new Metric(name: 'testMetric', description: '')

        then:
        !metric.validate()
        metric.errors.hasFieldErrors('description')
    }

    def "test start() initializes timing"() {
        given:
        Metric metric = new Metric(name: 'testMetric')

        when:
        def result = metric.start()

        then:
        result.is(metric)
    }

    def "test start() throws exception if already started"() {
        given:
        Metric metric = new Metric(name: 'testMetric')
        metric.start()
        metric.stop()

        when:
        metric.start()

        then:
        thrown(IllegalStateException)
    }

    def "test stop() calculates duration"() {
        given:
        Metric metric = new Metric(name: 'testMetric')

        when:
        metric.start()
        Thread.sleep(50)
        metric.stop()

        then:
        metric.duration != null
        metric.duration.toMillis() >= 50
    }

    def "test stop() returns self for chaining"() {
        given:
        Metric metric = new Metric(name: 'testMetric')
        metric.start()

        when:
        def result = metric.stop()

        then:
        result.is(metric)
    }

    def "test stop() does nothing if not started"() {
        given:
        Metric metric = new Metric(name: 'testMetric')

        when:
        metric.stop()

        then:
        metric.duration == null
    }

    def "test calculateElapsedTime() returns elapsed time while running"() {
        given:
        Metric metric = new Metric(name: 'testMetric')
        metric.start()

        when:
        Thread.sleep(50)
        Duration elapsed = metric.calculateElapsedTime()

        then:
        elapsed != null
        elapsed.toMillis() >= 50
    }

    def "test calculateElapsedTime() throws an exception if not started"() {
        given:
        Metric metric = new Metric(name: 'testMetric')

        when:
        metric.calculateElapsedTime()

        then:
        thrown(IllegalStateException)
    }

    def "test toHeaderValue() with name only"() {
        given:
        Metric metric = new Metric(name: 'testMetric')

        when:
        String header = metric.toHeaderValue()

        then:
        header == 'testMetric'
    }

    def "test toHeaderValue() with name and description"() {
        given:
        Metric metric = new Metric(name: 'testMetric', description: 'Test Description')

        when:
        String header = metric.toHeaderValue()

        then:
        header == 'testMetric;desc="Test Description"'
    }

    def "test toHeaderValue() with name and duration"() {
        given:
        Metric metric = new Metric(name: 'testMetric')
        metric.start()
        metric.stop()

        when:
        String header = metric.toHeaderValue()

        then:
        header.startsWith('testMetric;dur=')
    }

    def "test toHeaderValue() with name, description and duration"() {
        given:
        Metric metric = new Metric(name: 'testMetric', description: 'Test Description')
        metric.start()
        metric.stop()

        when:
        String header = metric.toHeaderValue()

        then:
        header.startsWith('testMetric;dur=')
        header.contains('desc="Test Description"')
    }

    def "test toHeaderValue() throws exception if started but not stopped"() {
        given:
        Metric metric = new Metric(name: 'testMetric')
        metric.start()

        when:
        metric.toHeaderValue()

        then:
        thrown(IllegalStateException)
    }

    def "test equals() with same name (case insensitive)"() {
        given:
        Metric metric1 = new Metric(name: 'testMetric')
        Metric metric2 = new Metric(name: 'TESTMETRIC')

        expect:
        metric1 == metric2
    }

    def "test equals() with different names"() {
        given:
        Metric metric1 = new Metric(name: 'testMetric1')
        Metric metric2 = new Metric(name: 'testMetric2')

        expect:
        metric1 != metric2
    }

    def "test equals() with same instance"() {
        given:
        Metric metric = new Metric(name: 'testMetric')

        expect:
        metric == metric
    }

    def "test equals() with null"() {
        given:
        Metric metric = new Metric(name: 'testMetric')

        expect:
        metric != null
    }

    def "test equals() with different class"() {
        given:
        Metric metric = new Metric(name: 'testMetric')

        expect:
        !metric.equals('testMetric')
    }

    def "test hashCode() consistency"() {
        given:
        Metric metric1 = new Metric(name: 'testMetric')
        Metric metric2 = new Metric(name: 'TESTMETRIC')

        expect:
        metric1.hashCode() == metric2.hashCode()
    }

    def "test hashCode() with null key"() {
        given:
        Metric metric = new Metric()

        expect:
        metric.hashCode() == 0
    }

    def "test metric is serializable"() {
        given:
        Metric metric = new Metric(name: 'testMetric', description: 'Test Description')

        when:
        ByteArrayOutputStream bos = new ByteArrayOutputStream()
        ObjectOutputStream oos = new ObjectOutputStream(bos)
        oos.writeObject(metric)
        oos.close()

        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray())
        ObjectInputStream ois = new ObjectInputStream(bis)
        Metric deserializedMetric = (Metric) ois.readObject()

        then:
        deserializedMetric.name == 'testMetric'
        deserializedMetric.description == 'Test Description'
    }

    def "test start() and stop() chaining"() {
        given:
        Metric metric = new Metric(name: 'testMetric')

        when:
        metric.start().stop()

        then:
        metric.duration != null
    }

    // Server-Timing spec compliance tests
    // See: https://w3c.github.io/server-timing/#the-server-timing-header-field

    def "test toHeaderValue() duration is in milliseconds with decimal precision"() {
        given:
        Metric metric = new Metric(name: 'db')
        metric.start()
        Thread.sleep(10)
        metric.stop()

        when:
        String header = metric.toHeaderValue()

        then:
        // Should be in format: name;dur=X.X where X.X is milliseconds
        header ==~ /db;dur=\d+\.\d/
    }

    def "test toHeaderValue() format matches Server-Timing spec"() {
        given:
        Metric metric = new Metric(name: 'cache', description: 'Cache Read')
        metric.start()
        metric.stop()

        when:
        String header = metric.toHeaderValue()

        then:
        // Format should be: metric-name;dur=value;desc="description"
        header ==~ /cache;dur=\d+\.\d;desc="Cache Read"/
    }

    def "test description with special characters is properly quoted"() {
        given:
        Metric metric = new Metric(name: 'api', description: 'API Call: GET /users')

        when:
        String header = metric.toHeaderValue()

        then:
        header == 'api;desc="API Call: GET /users"'
    }

    def "test metric with zero duration"() {
        given:
        Metric metric = new Metric(name: 'instant')
        metric.start()
        metric.stop()

        when:
        String header = metric.toHeaderValue()

        then:
        // Even very small durations should output dur=
        header.contains('dur=')
    }

    def "test multiple metrics can be created independently"() {
        given:
        Metric metric1 = new Metric(name: 'db', description: 'Database')
        Metric metric2 = new Metric(name: 'cache', description: 'Cache')

        metric1.start()
        metric2.start()

        Thread.sleep(10)
        metric1.stop()

        Thread.sleep(10)
        metric2.stop()

        expect:
        metric1.duration != metric2.duration
        metric1.toHeaderValue() != metric2.toHeaderValue()
    }

    def "test metric name follows token format per RFC 7230"() {
        when:
        Metric metric = new Metric(name: 'my-metric_123')

        then:
        metric.validate()
        metric.toHeaderValue() == 'my-metric_123'
    }

    def "test duration is not included if metric was never started"() {
        given:
        Metric metric = new Metric(name: 'skipped', description: 'Skipped operation')

        when:
        String header = metric.toHeaderValue()

        then:
        header == 'skipped;desc="Skipped operation"'
        !header.contains('dur=')
    }

    def "test header value with only name is valid per spec"() {
        // Server-Timing allows metrics with just a name (no dur or desc)
        given:
        Metric metric = new Metric(name: 'miss')

        when:
        String header = metric.toHeaderValue()

        then:
        header == 'miss'
    }

    def "test header value with name and duration only"() {
        given:
        Metric metric = new Metric(name: 'total')
        metric.start()
        metric.stop()

        when:
        String header = metric.toHeaderValue()

        then:
        header ==~ /total;dur=\d+\.\d/
        !header.contains('desc=')
    }

    def "test description with embedded quotes should be escaped"() {
        // Per RFC 7230, quoted strings must escape embedded quotes with backslash
        given:
        Metric metric = new Metric(name: 'api', description: 'Said "Hello"')

        when:
        String header = metric.toHeaderValue()

        then:
        header == 'api;desc="Said \\"Hello\\""'
    }

    def "test description with backslashes should be escaped"() {
        // Per RFC 7230, backslashes in quoted strings must be escaped
        given:
        Metric metric = new Metric(name: 'path', description: 'C:\\Users\\test')

        when:
        String header = metric.toHeaderValue()

        then:
        header == 'path;desc="C:\\\\Users\\\\test"'
    }

    def "test description with both quotes and backslashes"() {
        given:
        Metric metric = new Metric(name: 'complex', description: 'Path: "C:\\temp"')

        when:
        String header = metric.toHeaderValue()

        then:
        header == 'complex;desc="Path: \\"C:\\\\temp\\""'
    }

    def "test sub-millisecond duration precision"() {
        given:
        Metric metric = new Metric(name: 'fast')
        metric.start()
        // Don't sleep - capture very fast operation
        metric.stop()

        when:
        String header = metric.toHeaderValue()

        then:
        // Should still produce valid output even for sub-millisecond durations
        header ==~ /fast;dur=\d+\.\d/
    }

    def "test duration value format is decimal"() {
        given:
        Metric metric = new Metric(name: 'test')
        metric.start()
        Thread.sleep(5)
        metric.stop()

        when:
        String header = metric.toHeaderValue()
        String durValue = (header =~ /dur=(\d+\.\d)/)[0][1]

        then:
        // Duration should be parseable as a decimal number
        Double.parseDouble(durValue) >= 0
    }

    def "test metric can be used in Set due to equals/hashCode contract"() {
        given:
        Set<Metric> metrics = new HashSet<>()
        Metric metric1 = new Metric(name: 'db')
        Metric metric2 = new Metric(name: 'DB') // Same key (case insensitive)
        Metric metric3 = new Metric(name: 'cache')

        when:
        metrics.add(metric1)
        metrics.add(metric2)
        metrics.add(metric3)

        then:
        metrics.size() == 2 // metric1 and metric2 are equal
    }
}
