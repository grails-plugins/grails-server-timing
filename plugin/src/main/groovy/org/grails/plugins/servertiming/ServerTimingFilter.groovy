package org.grails.plugins.servertiming

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.FilterConfig
import jakarta.servlet.ServletException
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.grails.plugins.servertiming.core.TimingMetric
import org.springframework.core.Ordered

/**
 * A Servlet Filter that wraps responses to ensure Server Timing headers are added to HTTP responses.
 *
 * This filter works in conjunction with the TimingMetricInterceptor & ServerTimingResponseWrapper.
 * The interceptor assists in creating initial timing metrics for actions & views
 * The response wrapper ensures the Server Timing header is added before the response is committed.
 * For non-controller requests (static resources, etc.), the filter tracks timing as 'other'.
 */
@Slf4j
@CompileStatic
class ServerTimingFilter implements Filter, Ordered {

    private String metricKey

    @Override
    void init(FilterConfig filterConfig) throws ServletException {
        metricKey = ServerTimingUtils.instance.metricKey
    }

    @Override
    void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (!(request instanceof HttpServletRequest) || !(response instanceof HttpServletResponse)) {
            log.warn("Could not apply Server Timing Filter because request or response was not an expected HttpServlet type: {} / {}", request.class, response.class)
            chain.doFilter(request, response)
            return
        }

        HttpServletRequest httpRequest = (HttpServletRequest) request
        HttpServletResponse httpResponse = (HttpServletResponse) response

        // Create the timing metric and store it in the request
        // The interceptor will add 'action' and 'view' metrics for controller requests
        // For non-controller requests (static resources), we track as 'other'
        TimingMetric timing = new TimingMetric()
        httpRequest.setAttribute(metricKey, timing)
        timing.create('total', 'Total').start()
        timing.create('other', 'Non-Grails Controller Action/View').start()

        // Wrap the response to intercept commits and add the header
        ServerTimingResponseWrapper wrappedResponse = new ServerTimingResponseWrapper(httpResponse, timing)
        try {
            chain.doFilter(request, wrappedResponse)
        } finally {
            // Ensure the header is added if it hasn't been already
            // (handles cases where no output was written)
            wrappedResponse.beforeCommit()
        }
    }

    @Override
    void destroy() {
        log.trace('ServerTimingFilter destroyed')
    }

    @Override
    int getOrder() {
        // Run early to wrap the entire request
        Ordered.HIGHEST_PRECEDENCE + 100
    }
}

