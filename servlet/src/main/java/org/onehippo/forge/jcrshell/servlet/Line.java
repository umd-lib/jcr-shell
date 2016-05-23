package org.onehippo.forge.jcrshell.servlet;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Line extends Message {

    public enum TextMode {
        PLAIN, DEBUG, OK, WARN, ERROR
    }

    private final List<Text> parts = new LinkedList<Text>();

    public List<Text> getParts() {
        return Collections.unmodifiableList(parts);
    }

    public void addText(TextMode mode, String text) {
        parts.add(new Text(mode, text));
    }

    @Override
    public String getType() {
        return "LINE";
    }
}
