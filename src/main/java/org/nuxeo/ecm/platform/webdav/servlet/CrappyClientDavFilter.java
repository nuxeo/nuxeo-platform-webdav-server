package org.nuxeo.ecm.platform.webdav.servlet;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

public class CrappyClientDavFilter implements Filter {

    public void destroy() {
        // TODO Auto-generated method stub

    }

    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest=(HttpServletRequest)request;

        String method = httpRequest.getMethod();
        if (method.equals(WebDavConst.METHOD_PROPFIND) || method.equals(WebDavConst.METHOD_OPTIONS)) {
            httpRequest.getRequestDispatcher("/nuxeo/dav/").forward(request, response);
        }
        else
            chain.doFilter(request, response);
    }

    public void init(FilterConfig filterConfig) throws ServletException {
        // TODO Auto-generated method stub

    }

}
