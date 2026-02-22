package org.grails.plugins.servertiming

import grails.validation.ValidationException
import org.grails.plugins.servertiming.core.Metric
import org.grails.plugins.servertiming.core.TimingMetric
import spock.lang.Specification

class TimingMetricSpec extends Specification {

    def "test create() returns a new Metric"() {
        given:
        def timingMetric = new TimingMetric()

        when:
        def metric = timingMetric.create('db')

        then:
        metric != null
        metric.name == 'db'
    }

    def "test create() with name and description"() {
        given:
        def timingMetric = new TimingMetric()

        when:
        def metric = timingMetric.create('db', 'Database Query')

        then:
        metric != null
        metric.name == 'db'
        metric.description == 'Database Query'
    }

    def "test create() stores metric for later retrieval"() {
        given:
        def timingMetric = new TimingMetric()

        when:
        def created = timingMetric.create('cache')
        def retrieved = timingMetric.get('cache')

        then:
        created.is(retrieved)
    }

    def "test create() indicates metric exists"() {
        given:
        def timingMetric = new TimingMetric()

        when:
        def created = timingMetric.create('cache')

        then:
        timingMetric.has('cache')
    }

    def "test remove() removes a metric"() {
        given:
        def timingMetric = new TimingMetric()

        when:
        timingMetric.create('cache')
        timingMetric.remove('cache')

        then:
        !timingMetric.has('cache')
    }

    def "test create() throws ValidationException for invalid metric name"() {
        given:
        def timingMetric = new TimingMetric()

        when:
        timingMetric.create('invalid name') // space not allowed

        then:
        thrown(ValidationException)
    }

    def "test create() throws ValidationException for null name"() {
        given:
        def timingMetric = new TimingMetric()

        when:
        timingMetric.create(null)

        then:
        thrown(ValidationException)
    }

    def "test create() throws ValidationException for blank name"() {
        given:
        def timingMetric = new TimingMetric()

        when:
        timingMetric.create('')

        then:
        thrown(ValidationException)
    }

    def "test create() throws ValidationException for blank description"() {
        given:
        def timingMetric = new TimingMetric()

        when:
        timingMetric.create('valid', '')

        then:
        thrown(ValidationException)
    }

    def "test create() allows null description"() {
        given:
        def timingMetric = new TimingMetric()

        when:
        def metric = timingMetric.create('valid', null)

        then:
        notThrown(ValidationException)
        metric.description == null
    }

    def "test create() overwrites existing metric with same name"() {
        given:
        def timingMetric = new TimingMetric()

        when:
        def first = timingMetric.create('db', 'First')
        def second = timingMetric.create('db', 'Second')

        then:
        timingMetric.get('db').is(second)
        timingMetric.get('db').description == 'Second'
    }

    def "test get() returns null for non-existent metric"() {
        given:
        def timingMetric = new TimingMetric()

        expect:
        timingMetric.get('nonexistent') == null
    }

    def "test get() returns the correct metric"() {
        given:
        def timingMetric = new TimingMetric()
        timingMetric.create('db', 'Database')
        timingMetric.create('cache', 'Cache')

        expect:
        timingMetric.get('db').name == 'db'
        timingMetric.get('db').description == 'Database'
        timingMetric.get('cache').name == 'cache'
        timingMetric.get('cache').description == 'Cache'
    }

    def "test toHeaderValue() returns null when no metrics"() {
        given:
        def timingMetric = new TimingMetric()

        expect:
        timingMetric.toHeaderValue() == null
    }

    def "test toHeaderValue() with single metric (name only)"() {
        given:
        def timingMetric = new TimingMetric()
        timingMetric.create('miss')

        expect:
        timingMetric.toHeaderValue() == 'miss'
    }

    def "test toHeaderValue() with single metric (name and description)"() {
        given:
        def timingMetric = new TimingMetric()
        timingMetric.create('cache', 'Cache Read')

        expect:
        timingMetric.toHeaderValue() == 'cache;desc="Cache Read"'
    }

    def "test toHeaderValue() with single metric (name and duration)"() {
        given:
        def timingMetric = new TimingMetric()
        def metric = timingMetric.create('db')
        metric.start()
        metric.stop()

        when:
        def header = timingMetric.toHeaderValue()

        then:
        header ==~ /db;dur=\d+\.\d/
    }

    def "test toHeaderValue() with multiple metrics"() {
        given:
        def timingMetric = new TimingMetric()
        timingMetric.create('db', 'Database')
        timingMetric.create('cache', 'Cache')

        expect:
        timingMetric.toHeaderValue() == 'db;desc="Database",cache;desc="Cache"'
    }

    def "test toHeaderValue() with multiple metrics including durations"() {
        given:
        def timingMetric = new TimingMetric()

        def db = timingMetric.create('db', 'Database')
        db.start()
        db.stop()

        def cache = timingMetric.create('cache', 'Cache')
        cache.start()
        cache.stop()

        when:
        def header = timingMetric.toHeaderValue()

        then:
        header.contains('db;dur=')
        header.contains('desc="Database"')
        header.contains('cache;dur=')
        header.contains('desc="Cache"')
        header.contains(',')
    }

    def "test toHeaderValue() preserves metric order"() {
        given:
        def timingMetric = new TimingMetric()
        timingMetric.create('first')
        timingMetric.create('second')
        timingMetric.create('third')

        when:
        def header = timingMetric.toHeaderValue()

        then:
        header == 'first,second,third'
        header.indexOf('first') < header.indexOf('second')
        header.indexOf('second') < header.indexOf('third')
    }

    def "test TimingMetric is serializable"() {
        given:
        def timingMetric = new TimingMetric()
        timingMetric.create('db', 'Database')
        timingMetric.create('cache', 'Cache')

        when:
        def bos = new ByteArrayOutputStream()
        def oos = new ObjectOutputStream(bos)
        oos.writeObject(timingMetric)
        oos.close()

        def bis = new ByteArrayInputStream(bos.toByteArray())
        def ois = new ObjectInputStream(bis)
        def deserialized = (TimingMetric) ois.readObject()

        then:
        deserialized.get('db') != null
        deserialized.get('db').name == 'db'
        deserialized.get('cache') != null
        deserialized.get('cache').name == 'cache'
    }

    def "test toHeaderValue() format matches Server Timing spec"() {
        // Server-Timing header format: metric-name;dur=value;desc="description", ...
        given:
        def timingMetric = new TimingMetric()

        def db = timingMetric.create('db', 'Database Query')
        db.start()
        Thread.sleep(5)
        db.stop()

        timingMetric.create('miss')

        when:
        def header = timingMetric.toHeaderValue()

        then:
        // Should produce: db;dur=X.X;desc="Database Query",miss
        header ==~ /db;dur=\d+\.\d;desc="Database Query",miss/
    }

    def "test toHeaderValue() handles special characters in descriptions"() {
        given:
        def timingMetric = new TimingMetric()
        timingMetric.create('api', 'GET /users?id=1')
        timingMetric.create('db', 'Query: "SELECT *"')

        when:
        def header = timingMetric.toHeaderValue()

        then:
        header.contains('api;desc="GET /users?id=1"')
        header.contains('db;desc="Query: \\"SELECT *\\""')
    }

    def "test typical usage pattern"() {
        given:
        def timingMetric = new TimingMetric()

        when: 'Create and time a database operation'
        def db = timingMetric.create('db', 'Database')
        db.start()
        Thread.sleep(10)
        db.stop()

        and: 'Create and time a cache operation'
        def cache = timingMetric.create('cache', 'Cache')
        cache.start()
        Thread.sleep(5)
        cache.stop()

        and: 'Record a cache miss (no timing)'
        timingMetric.create('miss')

        then:
        def header = timingMetric.toHeaderValue()
        header.contains('db;dur=')
        header.contains('cache;dur=')
        header.contains('miss')
        header.split(',').size() == 3
    }
}
