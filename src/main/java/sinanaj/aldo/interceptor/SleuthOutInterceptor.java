package sinanaj.aldo.interceptor;

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.transport.http.Address;
import org.apache.cxf.transport.http.HTTPConduit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.ErrorParser;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.TraceKeys;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.cloud.sleuth.util.SpanNameUtil;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class SleuthOutInterceptor extends AbstractPhaseInterceptor {

    @Autowired
    private Tracer tracer;

    @Autowired
    private TraceKeys traceKeys;

    @Autowired
    private ErrorParser errorParser;

    public SleuthOutInterceptor() {
        super(Phase.PRE_PROTOCOL);
    }

    @Override
    public void handleMessage(Message message) throws Fault {
        try {
            final String httpMethod = (String) message.get(Message.HTTP_REQUEST_METHOD);
            final Address address = (Address) message.get(HTTPConduit.KEY_HTTP_CONNECTION_ADDRESS);
            final URI addressURI = address.getURI();

            final String spanName = SpanNameUtil.shorten(addressURI.getScheme() + ":" + addressURI.getPath());
            final Span newSpan = this.tracer.createSpan(spanName);
            newSpan.logEvent(Span.CLIENT_SEND);

            // Set Sleuth headers
            final Map<String, List<String>> headers = (Map<String, List<String>>)message.get(Message.PROTOCOL_HEADERS);
            addSleuthHeaders(newSpan, headers);

            // Add request tags
            addRequestTags(addressURI.toString(), addressURI.getHost(), addressURI.getPath(), httpMethod, headers);
        } catch (Exception e) {
            final Span currentSpan = tracer.getCurrentSpan();
            errorParser.parseErrorTags(currentSpan, e);

            // Close current span
            currentSpan.logEvent(Span.CLIENT_RECV);
            tracer.close(currentSpan);

            throw e;
        }
    }

    private void addSleuthHeaders(Span newSpan, Map<String, List<String>> headers) {
        headers.put(Span.TRACE_ID_NAME, Collections.singletonList(newSpan.traceIdString()));
        headers.put(Span.SPAN_ID_NAME, Collections.singletonList(idToHexNullSafe(newSpan.getSpanId())));
        headers.put(Span.SAMPLED_NAME, Collections.singletonList(isSampledName(newSpan)));
        headers.put(Span.SPAN_NAME_NAME, Collections.singletonList(newSpan.getName()));
        headers.put(Span.PROCESS_ID_NAME, Collections.singletonList(newSpan.getProcessId()));
        headers.put(Span.PARENT_ID_NAME, Collections.singletonList(getParentIdHex(newSpan)));
    }

    private String isSampledName(Span span) {
        return span.isExportable() ? Span.SPAN_SAMPLED : Span.SPAN_NOT_SAMPLED;
    }

    private String idToHexNullSafe(Long id) {
        return id == null ? "" : Span.idToHex(id);
    }

    private String getParentIdHex(Span span) {
        final Long parentId = span.getParents().isEmpty() ? null : span.getParents().get(0);

        return idToHexNullSafe(parentId);
    }

    private void addRequestTags(String url, String host, String path, String method, Map<String, ? extends Collection<String>> headers) {
        tracer.addTag(traceKeys.getHttp().getUrl(), url);
        tracer.addTag(traceKeys.getHttp().getHost(), host);
        tracer.addTag(traceKeys.getHttp().getPath(), path);
        tracer.addTag(traceKeys.getHttp().getMethod(), method);

        addRequestTagsFromHeaders(headers);
    }

    private void addRequestTagsFromHeaders(Map<String, ? extends Collection<String>> headers) {
        final Iterator headersIterator = this.traceKeys.getHttp().getHeaders().iterator();

        while(headersIterator.hasNext()) {
            String headerName = (String)headersIterator.next();
            Collection<String> headerValue = headers.get(headerName);

            if (headerValue != null) {
                this.addTagForEntry(headerName, headerValue);
            }
        }
    }

    private void addTagForEntry(String name, Collection<String> list) {
        final String key = traceKeys.getHttp().getPrefix() + name.toLowerCase();
        final String value = list.size() == 1 ? list.iterator().next() : StringUtils.collectionToDelimitedString(list, ",", "'", "'");
        tracer.addTag(key, value);
    }
}

