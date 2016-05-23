package org.onehippo.forge.jcrshell.servlet;

public class Text {

    private final String text;
    private final Line.TextMode mode;

    Text(Line.TextMode mode, String text) {
        this.mode = mode;
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public Line.TextMode getMode() {
        return mode;
    }
}

