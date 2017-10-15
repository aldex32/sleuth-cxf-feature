package sinanaj.aldo.interceptor;

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Tracer;

public class SleuthInInterceptor extends AbstractPhaseInterceptor {

    @Autowired
    private Tracer tracer;

    public SleuthInInterceptor() {
        super(Phase.PRE_INVOKE);
    }

    @Override
    public void handleMessage(Message message) throws Fault {
        final Span currentSpan = tracer.getCurrentSpan();
        currentSpan.logEvent(Span.CLIENT_RECV);
        tracer.close(currentSpan);
    }
}
