package org.onehippo.forge.jcrshell.servlet;

import java.util.LinkedList;
import java.util.List;


public class Completion {
    private List<String> candidates = new LinkedList<String>();

    private int start;

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public List<String> getCandidates() {
        return candidates;
    }
}
