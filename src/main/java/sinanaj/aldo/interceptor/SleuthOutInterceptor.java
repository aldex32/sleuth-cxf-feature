package sinanaj.aldo.interceptor;

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.ErrorParser;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Tracer;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SleuthOutInterceptor extends AbstractPhaseInterceptor {

    @Autowired
    private Tracer tracer;

    @Autowired
    private ErrorParser errorParser;

    public SleuthOutInterceptor() {
        super(Phase.PRE_PROTOCOL);
    }

    @Override
    public void handleMessage(Message message) throws Fault {
        try {
            final Map<String, List<String>> headers = (Map<String, List<String>>)message.get(Message.PROTOCOL_HEADERS);
            final String endpoint = (String) message.get(Message.ENDPOINT_ADDRESS);

            final Span newSpan = this.tracer.createSpan(endpoint);
            newSpan.logEvent(Span.CLIENT_SEND);

            // Set Sleuth headers
            headers.put(Span.TRACE_ID_NAME, Collections.singletonList(newSpan.traceIdString()));
            headers.put(Span.SPAN_ID_NAME, Collections.singletonList(idToHexNullSafe(newSpan.getSpanId())));
            headers.put(Span.SAMPLED_NAME, Collections.singletonList(isSampledName(newSpan)));
            headers.put(Span.SPAN_NAME_NAME, Collections.singletonList(newSpan.getName()));
            headers.put(Span.PROCESS_ID_NAME, Collections.singletonList(newSpan.getProcessId()));
            headers.put(Span.PARENT_ID_NAME, Collections.singletonList(getParentIdHex(newSpan)));
        } catch (Exception e) {
            final Span currentSpan = tracer.getCurrentSpan();
            errorParser.parseErrorTags(currentSpan, e);

            // Close current span
            currentSpan.logEvent(Span.CLIENT_RECV);
            tracer.close(currentSpan);

            throw e;
        }
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
}
