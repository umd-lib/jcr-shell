package org.onehippo.forge.jcrshell.servlet;

import org.onehippo.forge.jcrshell.JcrShellSession;
import org.onehippo.forge.jcrshell.JcrWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class JcrShellSessionFilter implements Filter {

    static final Logger log = LoggerFactory.getLogger(JcrShellSessionFilter.class);

    public void init(FilterConfig filterConfig) throws ServletException {
    }

    public void destroy() {
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws java.io.IOException, ServletException {
        HttpSession httpSession = ((HttpServletRequest) request).getSession();
        JcrShellSession session = (JcrShellSession) httpSession.getAttribute(JcrShellSession.class.getName());
        try {
            JcrWrapper.setShellSession(session);
            chain.doFilter(request, response);
        } finally {
            JcrWrapper.setShellSession(null);
        }
    }

}
