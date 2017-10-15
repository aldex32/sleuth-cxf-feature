package sinanaj.aldo.feature;

import sinanaj.aldo.interceptor.SleuthInInterceptor;
import sinanaj.aldo.interceptor.SleuthOutInterceptor;
import org.apache.cxf.Bus;
import org.apache.cxf.annotations.EvaluateAllEndpoints;
import org.apache.cxf.annotations.Provider;
import org.apache.cxf.common.injection.NoJSR250Annotations;
import org.apache.cxf.feature.AbstractFeature;
import org.apache.cxf.interceptor.InterceptorProvider;

@NoJSR250Annotations
@EvaluateAllEndpoints
@Provider(
        value = Provider.Type.Feature,
        scope = Provider.Scope.Client
)
public class SleuthFeature extends AbstractFeature {

    private final SleuthInInterceptor sleuthInInterceptor;
    private final SleuthOutInterceptor sleuthOutInterceptor;

    public SleuthFeature(SleuthInInterceptor sleuthInInterceptor, SleuthOutInterceptor sleuthOutInterceptor) {
        this.sleuthInInterceptor = sleuthInInterceptor;
        this.sleuthOutInterceptor = sleuthOutInterceptor;
    }

    @Override
    protected void initializeProvider(InterceptorProvider provider, Bus bus) {
        provider.getInInterceptors().add(sleuthInInterceptor);
        provider.getOutInterceptors().add(sleuthOutInterceptor);
        provider.getInFaultInterceptors().add(sleuthInInterceptor);
        provider.getOutFaultInterceptors().add(sleuthOutInterceptor);
    }
}
