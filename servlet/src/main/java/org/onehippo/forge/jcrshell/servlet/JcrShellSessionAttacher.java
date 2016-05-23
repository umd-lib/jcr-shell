package org.onehippo.forge.jcrshell.servlet;

import org.onehippo.forge.jcrshell.JcrShellSession;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

public class JcrShellSessionAttacher implements HttpSessionListener {

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        HttpSession httpSession = se.getSession();
        JcrShellSession shellSession = new JcrShellSession();
        httpSession.setAttribute(JcrShellSession.class.getName(), shellSession);
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        HttpSession httpSession = se.getSession();
        JcrShellSession session = (JcrShellSession) httpSession.getAttribute(JcrShellSession.class.getName());
        if (session != null) {
            httpSession.removeAttribute(JcrShellSession.class.getName());
            session.destroy();
        }
    }
}
