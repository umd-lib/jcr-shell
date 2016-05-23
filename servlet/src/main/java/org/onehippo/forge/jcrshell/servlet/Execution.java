package org.onehippo.forge.jcrshell.servlet;

import java.util.LinkedList;
import java.util.List;

public class Execution {

    private String path;
    private final List<Message> messages = new LinkedList<Message>();

    public Execution(String path) {
        this.path = path;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void addMessage(Message message) {
        messages.add(message);
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

}
