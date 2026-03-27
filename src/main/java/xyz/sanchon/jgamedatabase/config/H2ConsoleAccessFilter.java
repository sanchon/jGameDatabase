package xyz.sanchon.jgamedatabase.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import xyz.sanchon.jgamedatabase.service.AppConfigurationService;

import java.io.IOException;

@Configuration
public class H2ConsoleAccessFilter {

    @Bean
    public FilterRegistrationBean<Filter> h2ConsoleFilter(AppConfigurationService configService) {
        FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>();
        registration.setFilter((request, response, chain) -> {
            if (!configService.isH2ConsoleEnabled()) {
                ((HttpServletResponse) response).sendError(HttpServletResponse.SC_FORBIDDEN, "H2 console is disabled");
                return;
            }
            chain.doFilter(request, response);
        });
        registration.addUrlPatterns("/h2-console/*");
        registration.setOrder(1);
        return registration;
    }
}