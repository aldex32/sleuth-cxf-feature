package sinanaj.aldo.filter;

import org.springframework.cloud.sleuth.instrument.web.TraceRequestAttributes;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class SleuthFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        final HttpServletRequest httpRequest = (HttpServletRequest) request;
        httpRequest.setAttribute(TraceRequestAttributes.HANDLED_SPAN_REQUEST_ATTR, "true");

        chain.doFilter(httpRequest, response);
    }

    @Override
    public void destroy() {

    }
}
