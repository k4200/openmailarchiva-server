package com.stimulus.struts.filter;

import java.io.IOException;
import java.util.*;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

public final class ParamFilter implements Filter {

    FilterConfig filterConfig = null;

    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        String regex = this.filterConfig.getInitParameter("excludeParams");
        chain.doFilter(new ParamFilteredRequest(request, regex), response);
    }

    public void destroy() { }
    
    static class ParamFilteredRequest extends HttpServletRequestWrapper {

        private HttpServletRequest originalRequest;
        private String regex;

        public ParamFilteredRequest(ServletRequest request, String regex) {
            super((HttpServletRequest)request);
            this.originalRequest = (HttpServletRequest) request;
            this.regex = regex;
        }
        public Enumeration getParameterNames() {
            List<String> requestParameterNames = Collections.list((Enumeration<String>) super.getParameterNames());
            List finalParameterNames = new ArrayList();

            for (String parameterName:requestParameterNames) {
                if (!parameterName.matches(regex)) {
                    finalParameterNames.add(parameterName);
                    System.out.println("Param : " + parameterName);
                }
            }
            return Collections.enumeration(finalParameterNames);
        }
    }
}
