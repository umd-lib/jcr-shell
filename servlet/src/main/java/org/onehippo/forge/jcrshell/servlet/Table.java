package org.onehippo.forge.jcrshell.servlet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Table extends Message {

    private final List<String> headers;
    private final List<List<String>> rows = new LinkedList<List<String>>();

    public Table(List<String> headers) {
        this.headers = new ArrayList<String>(headers);
    }

    public List<String> getHeaders() {
        return Collections.unmodifiableList(headers);
    }

    public List<List<String>> getRows() {
        return Collections.unmodifiableList(rows);
    }

    public void addRow(List<String> row) {
        assert (row.size() == headers.size());
        rows.add(new ArrayList<String>(row));
    }

    @Override
    public String getType() {
        return "TABLE";
    }

}
