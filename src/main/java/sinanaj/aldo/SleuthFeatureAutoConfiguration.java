package sinanaj.aldo;

import sinanaj.aldo.feature.SleuthFeature;
import sinanaj.aldo.filter.SleuthFilter;
import sinanaj.aldo.interceptor.SleuthInInterceptor;
import sinanaj.aldo.interceptor.SleuthOutInterceptor;
import org.apache.cxf.bus.spring.SpringBus;
import org.apache.cxf.transport.servlet.CXFServlet;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.web.EmbeddedServletContainerAutoConfiguration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Filter;

@Configuration
@ConditionalOnWebApplication
@ConditionalOnClass({SpringBus.class, CXFServlet.class})
@AutoConfigureAfter({EmbeddedServletContainerAutoConfiguration.class})
public class SleuthFeatureAutoConfiguration {

    @Bean
    public SleuthOutInterceptor sleuthOutInterceptor() {
        return new SleuthOutInterceptor();
    }

    @Bean
    public SleuthInInterceptor sleuthInInterceptor() {
        return new SleuthInInterceptor();
    }

    @Bean
    public FilterRegistrationBean sleuthFilterRegistration() {
        final FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean();
        filterRegistrationBean.setFilter(getSleuthFilter());

        return filterRegistrationBean;
    }

    @Bean
    public Filter getSleuthFilter() {
        return new SleuthFilter();
    }

    @Bean
    public SleuthFeature sleuthFeature() {
        return new SleuthFeature(sleuthInInterceptor(), sleuthOutInterceptor());
    }
}
