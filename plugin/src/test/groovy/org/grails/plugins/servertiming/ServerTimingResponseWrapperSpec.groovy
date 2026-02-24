package org.grails.plugins.servertiming

import jakarta.servlet.ServletOutputStream
import jakarta.servlet.WriteListener
import jakarta.servlet.http.HttpServletResponse
import org.grails.plugins.servertiming.core.TimingMetric
import spock.lang.Specification

class ServerTimingResponseWrapperSpec extends Specification {

    HttpServletResponse mockResponse
    TimingMetric timing

    def setup() {
        mockResponse = Mock(HttpServletResponse)
        timing = new TimingMetric()
        timing.create('total', 'Total').start()
    }

    // --- getOutputStream() tests ---

    def "getOutputStream() does not add header until first write"() {
        given:
        def realOutputStream = new StubServletOutputStream()
        mockResponse.getOutputStream() >> realOutputStream
        def wrapper = new ServerTimingResponseWrapper(mockResponse, timing)

        when: 'getting the output stream without writing'
        wrapper.getOutputStream()

        then: 'header is not added yet'
        0 * mockResponse.addHeader(_, _)
    }

    def "getOutputStream() adds header on first write(int)"() {
        given:
        def realOutputStream = new StubServletOutputStream()
        mockResponse.getOutputStream() >> realOutputStream
        def wrapper = new ServerTimingResponseWrapper(mockResponse, timing)
        def os = wrapper.getOutputStream()

        when:
        os.write(65)

        then:
        1 * mockResponse.addHeader('Server-Timing', _)
    }

    def "getOutputStream() adds header on first write(byte[])"() {
        given:
        def realOutputStream = new StubServletOutputStream()
        mockResponse.getOutputStream() >> realOutputStream
        def wrapper = new ServerTimingResponseWrapper(mockResponse, timing)
        def os = wrapper.getOutputStream()

        when:
        os.write('hello'.bytes)

        then:
        1 * mockResponse.addHeader('Server-Timing', _)
    }

    def "getOutputStream() adds header on first write(byte[], off, len)"() {
        given:
        def realOutputStream = new StubServletOutputStream()
        mockResponse.getOutputStream() >> realOutputStream
        def wrapper = new ServerTimingResponseWrapper(mockResponse, timing)
        def os = wrapper.getOutputStream()
        def bytes = 'hello'.bytes

        when:
        os.write(bytes, 0, bytes.length)

        then:
        1 * mockResponse.addHeader('Server-Timing', _)
    }

    def "getOutputStream() adds header on flush"() {
        given:
        def realOutputStream = new StubServletOutputStream()
        mockResponse.getOutputStream() >> realOutputStream
        def wrapper = new ServerTimingResponseWrapper(mockResponse, timing)
        def os = wrapper.getOutputStream()

        when:
        os.flush()

        then:
        1 * mockResponse.addHeader('Server-Timing', _)
    }

    def "getOutputStream() adds header on close"() {
        given:
        def realOutputStream = new StubServletOutputStream()
        mockResponse.getOutputStream() >> realOutputStream
        def wrapper = new ServerTimingResponseWrapper(mockResponse, timing)
        def os = wrapper.getOutputStream()

        when:
        os.close()

        then:
        1 * mockResponse.addHeader('Server-Timing', _)
    }

    def "getOutputStream() adds header only once across multiple writes"() {
        given:
        def realOutputStream = new StubServletOutputStream()
        mockResponse.getOutputStream() >> realOutputStream
        def wrapper = new ServerTimingResponseWrapper(mockResponse, timing)
        def os = wrapper.getOutputStream()

        when:
        os.write(65)
        os.write(66)
        os.write(67)
        os.flush()

        then:
        1 * mockResponse.addHeader('Server-Timing', _)
    }

    // --- getWriter() tests ---

    def "getWriter() does not add header until first write"() {
        given:
        def stringWriter = new StringWriter()
        def realWriter = new PrintWriter(stringWriter)
        mockResponse.getWriter() >> realWriter
        def wrapper = new ServerTimingResponseWrapper(mockResponse, timing)

        when: 'getting the writer without writing'
        wrapper.getWriter()

        then: 'header is not added yet'
        0 * mockResponse.addHeader(_, _)
    }

    def "getWriter() adds header on first write(int)"() {
        given:
        def stringWriter = new StringWriter()
        def realWriter = new PrintWriter(stringWriter)
        mockResponse.getWriter() >> realWriter
        def wrapper = new ServerTimingResponseWrapper(mockResponse, timing)
        def writer = wrapper.getWriter()

        when:
        writer.write((int) 'A'.charAt(0))

        then:
        1 * mockResponse.addHeader('Server-Timing', _)
    }

    def "getWriter() adds header on first write(char[], off, len)"() {
        given:
        def stringWriter = new StringWriter()
        def realWriter = new PrintWriter(stringWriter)
        mockResponse.getWriter() >> realWriter
        def wrapper = new ServerTimingResponseWrapper(mockResponse, timing)
        def writer = wrapper.getWriter()
        def chars = 'hello'.toCharArray()

        when:
        writer.write(chars, 0, chars.length)

        then:
        1 * mockResponse.addHeader('Server-Timing', _)
    }

    def "getWriter() adds header on first write(String, off, len)"() {
        given:
        def stringWriter = new StringWriter()
        def realWriter = new PrintWriter(stringWriter)
        mockResponse.getWriter() >> realWriter
        def wrapper = new ServerTimingResponseWrapper(mockResponse, timing)
        def writer = wrapper.getWriter()

        when:
        writer.write('hello', 0, 5)

        then:
        1 * mockResponse.addHeader('Server-Timing', _)
    }

    def "getWriter() adds header on flush"() {
        given:
        def stringWriter = new StringWriter()
        def realWriter = new PrintWriter(stringWriter)
        mockResponse.getWriter() >> realWriter
        def wrapper = new ServerTimingResponseWrapper(mockResponse, timing)
        def writer = wrapper.getWriter()

        when:
        writer.flush()

        then:
        1 * mockResponse.addHeader('Server-Timing', _)
    }

    def "getWriter() adds header on close"() {
        given:
        def stringWriter = new StringWriter()
        def realWriter = new PrintWriter(stringWriter)
        mockResponse.getWriter() >> realWriter
        def wrapper = new ServerTimingResponseWrapper(mockResponse, timing)
        def writer = wrapper.getWriter()

        when:
        writer.close()

        then:
        1 * mockResponse.addHeader('Server-Timing', _)
    }

    def "getWriter() adds header only once across multiple writes"() {
        given:
        def stringWriter = new StringWriter()
        def realWriter = new PrintWriter(stringWriter)
        mockResponse.getWriter() >> realWriter
        def wrapper = new ServerTimingResponseWrapper(mockResponse, timing)
        def writer = wrapper.getWriter()

        when:
        writer.write('hello')
        writer.write(' world')
        writer.flush()

        then:
        1 * mockResponse.addHeader('Server-Timing', _)
    }

    // --- Behavioral consistency tests ---

    def "getWriter() and getOutputStream() both defer header injection to first write"() {
        given: 'a wrapper using getOutputStream'
        def realOutputStream = new StubServletOutputStream()
        def osResponse = Mock(HttpServletResponse)
        osResponse.getOutputStream() >> realOutputStream
        def osTiming = new TimingMetric()
        osTiming.create('total', 'Total').start()
        def osWrapper = new ServerTimingResponseWrapper(osResponse, osTiming)

        and: 'a wrapper using getWriter'
        def stringWriter = new StringWriter()
        def realWriter = new PrintWriter(stringWriter)
        def writerResponse = Mock(HttpServletResponse)
        writerResponse.getWriter() >> realWriter
        def writerTiming = new TimingMetric()
        writerTiming.create('total', 'Total').start()
        def writerWrapper = new ServerTimingResponseWrapper(writerResponse, writerTiming)

        when: 'both are obtained but not yet written to'
        osWrapper.getOutputStream()
        writerWrapper.getWriter()

        then: 'neither adds the header'
        0 * osResponse.addHeader(_, _)
        0 * writerResponse.addHeader(_, _)

        when: 'both write data'
        osWrapper.getOutputStream().write(65)
        writerWrapper.getWriter().write('A')

        then: 'both add the header exactly once'
        1 * osResponse.addHeader('Server-Timing', _)
        1 * writerResponse.addHeader('Server-Timing', _)
    }

    def "getWriter() data is written through to delegate"() {
        given:
        def stringWriter = new StringWriter()
        def realWriter = new PrintWriter(stringWriter)
        mockResponse.getWriter() >> realWriter
        def wrapper = new ServerTimingResponseWrapper(mockResponse, timing)
        def writer = wrapper.getWriter()

        when:
        writer.write('hello world')
        writer.flush()

        then:
        stringWriter.toString() == 'hello world'
    }

    def "getOutputStream() data is written through to delegate"() {
        given:
        def realOutputStream = new StubServletOutputStream()
        mockResponse.getOutputStream() >> realOutputStream
        def wrapper = new ServerTimingResponseWrapper(mockResponse, timing)
        def os = wrapper.getOutputStream()

        when:
        os.write('hello'.bytes)

        then:
        realOutputStream.data.toByteArray() == 'hello'.bytes
    }

    // --- beforeCommit / safety net tests ---

    def "beforeCommit() adds header even if neither getWriter nor getOutputStream was called"() {
        given:
        def wrapper = new ServerTimingResponseWrapper(mockResponse, timing)

        when:
        wrapper.beforeCommit()

        then:
        1 * mockResponse.addHeader('Server-Timing', _)
    }

    def "beforeCommit() is idempotent"() {
        given:
        def wrapper = new ServerTimingResponseWrapper(mockResponse, timing)

        when:
        wrapper.beforeCommit()
        wrapper.beforeCommit()

        then:
        1 * mockResponse.addHeader('Server-Timing', _)
    }

    def "reset() allows header to be re-added"() {
        given:
        def wrapper = new ServerTimingResponseWrapper(mockResponse, timing)

        when:
        wrapper.beforeCommit()
        wrapper.reset()
        wrapper.beforeCommit()

        then:
        2 * mockResponse.addHeader('Server-Timing', _)
    }

    // --- Returns same instance tests ---

    def "getOutputStream() returns the same instance on subsequent calls"() {
        given:
        def realOutputStream = new StubServletOutputStream()
        mockResponse.getOutputStream() >> realOutputStream
        def wrapper = new ServerTimingResponseWrapper(mockResponse, timing)

        when:
        def os1 = wrapper.getOutputStream()
        def os2 = wrapper.getOutputStream()

        then:
        os1.is(os2)
    }

    def "getWriter() returns the same instance on subsequent calls"() {
        given:
        def stringWriter = new StringWriter()
        def realWriter = new PrintWriter(stringWriter)
        mockResponse.getWriter() >> realWriter
        def wrapper = new ServerTimingResponseWrapper(mockResponse, timing)

        when:
        def w1 = wrapper.getWriter()
        def w2 = wrapper.getWriter()

        then:
        w1.is(w2)
    }

    /**
     * Minimal ServletOutputStream stub for testing.
     */
    static class StubServletOutputStream extends ServletOutputStream {

        final ByteArrayOutputStream data = new ByteArrayOutputStream()

        @Override
        void write(int b) throws IOException {
            data.write(b)
        }

        @Override
        boolean isReady() {
            return true
        }

        @Override
        void setWriteListener(WriteListener writeListener) {
            // no-op
        }
    }
}
