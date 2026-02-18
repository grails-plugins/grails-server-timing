package org.grails.plugins.servertiming

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import jakarta.servlet.ServletOutputStream
import jakarta.servlet.WriteListener
import jakarta.servlet.http.HttpServletResponse
import jakarta.servlet.http.HttpServletResponseWrapper
import org.grails.plugins.servertiming.core.TimingMetric

/**
 * A response wrapper that intercepts the response commit to add the Server Timing header
 * before the response is actually committed.
 */
@Slf4j
@CompileStatic
class ServerTimingResponseWrapper extends HttpServletResponseWrapper {

    private final TimingMetric timing
    private final HttpServletResponse originalResponse
    private boolean headerAdded = false
    private ServletOutputStream wrappedOutputStream
    private PrintWriter wrappedWriter

    ServerTimingResponseWrapper(HttpServletResponse response, TimingMetric timing) {
        super(response)
        this.originalResponse = response
        this.timing = timing
    }

    /**
     * Adds the Server Timing header if not already added.
     */
    private void addServerTimingHeaderIfNeeded() {
        if (!headerAdded && timing) {
            log.debug('Adding {} header with timing metrics', ServerTimingInterceptor.HEADER_NAME)

            headerAdded = true

            stopTimings()

            def headerValue = timing.toHeaderValue()
            log.trace('{} header value: {}', ServerTimingInterceptor.HEADER_NAME, headerValue)
            if (headerValue) {
                originalResponse.addHeader(ServerTimingInterceptor.HEADER_NAME, headerValue)
            }
        } else {
            log.debug('{} header already added or timing metric not available, skipping header addition', ServerTimingInterceptor.HEADER_NAME)
        }
    }

    private void stopTimings() {
        // not all requests go through the interceptor (e.g., static resources); show other when no action/view
        if (timing.has('view') || timing.has('action')) {
            timing.remove('other')
        } else {
            timing.get('other')?.stop()
        }

        def actionTiming = timing.get('action')
        if (actionTiming?.running) {
            actionTiming.stop()
        }

        // view won't exist if the action committed the request
        def viewTiming = timing.get('view')
        if (viewTiming?.running) {
            viewTiming.stop()
        }

        timing.get('total')?.stop()
    }

    @Override
    ServletOutputStream getOutputStream() throws IOException {
        if (wrappedOutputStream == null) {
            wrappedOutputStream = new ServerTimingServletOutputStream(
                    originalResponse.getOutputStream(),
                    this
            )
        }
        return wrappedOutputStream
    }

    @Override
    PrintWriter getWriter() throws IOException {
        if (wrappedWriter == null) {
            wrappedWriter = new ServerTimingPrintWriter(
                    originalResponse.getWriter(),
                    this
            )
        }
        return wrappedWriter
    }

    @Override
    void sendError(int sc, String msg) throws IOException {
        addServerTimingHeaderIfNeeded()
        super.sendError(sc, msg)
    }

    @Override
    void sendError(int sc) throws IOException {
        addServerTimingHeaderIfNeeded()
        super.sendError(sc)
    }

    @Override
    void sendRedirect(String location) throws IOException {
        addServerTimingHeaderIfNeeded()
        super.sendRedirect(location)
    }

    @Override
    void flushBuffer() throws IOException {
        addServerTimingHeaderIfNeeded()
        super.flushBuffer()
    }

    @Override
    void reset() {
        headerAdded = false
        super.reset()
    }

    /**
     * Called when the response is about to be committed (first write or flush).
     */
    void beforeCommit() {
        addServerTimingHeaderIfNeeded()
    }

    /**
     * Wrapped ServletOutputStream that triggers header addition before first write.
     */
    @CompileStatic
    private static class ServerTimingServletOutputStream extends ServletOutputStream {

        private final ServletOutputStream delegate
        private final ServerTimingResponseWrapper wrapper
        private boolean firstWrite = true

        ServerTimingServletOutputStream(ServletOutputStream delegate, ServerTimingResponseWrapper wrapper) {
            this.delegate = delegate
            this.wrapper = wrapper
        }

        private void beforeWrite() {
            if (firstWrite) {
                firstWrite = false
                wrapper.beforeCommit()
            }
        }

        @Override
        void write(int b) throws IOException {
            beforeWrite()
            delegate.write(b)
        }

        @Override
        void write(byte[] b) throws IOException {
            beforeWrite()
            delegate.write(b)
        }

        @Override
        void write(byte[] b, int off, int len) throws IOException {
            beforeWrite()
            delegate.write(b, off, len)
        }

        @Override
        void flush() throws IOException {
            beforeWrite()
            delegate.flush()
        }

        @Override
        void close() throws IOException {
            beforeWrite()
            delegate.close()
        }

        @Override
        boolean isReady() {
            return delegate.isReady()
        }

        @Override
        void setWriteListener(WriteListener writeListener) {
            delegate.setWriteListener(writeListener)
        }
    }

    /**
     * Wrapped PrintWriter that triggers header addition before first write,
     * consistent with the deferred approach used by ServerTimingServletOutputStream.
     */
    @CompileStatic
    private static class ServerTimingPrintWriter extends PrintWriter {

        private final ServerTimingResponseWrapper wrapper
        private boolean firstWrite = true

        ServerTimingPrintWriter(PrintWriter delegate, ServerTimingResponseWrapper wrapper) {
            super(delegate)
            this.wrapper = wrapper
        }

        private void beforeWrite() {
            if (firstWrite) {
                firstWrite = false
                wrapper.beforeCommit()
            }
        }

        @Override
        void write(int c) {
            beforeWrite()
            super.write(c)
        }

        @Override
        void write(char[] buf, int off, int len) {
            beforeWrite()
            super.write(buf, off, len)
        }

        @Override
        void write(String s, int off, int len) {
            beforeWrite()
            super.write(s, off, len)
        }

        @Override
        void flush() {
            beforeWrite()
            super.flush()
        }

        @Override
        void close() {
            beforeWrite()
            super.close()
        }
    }
}

