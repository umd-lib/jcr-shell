package org.onehippo.forge.jcrshell;

import org.onehippo.forge.jcrshell.output.Output;

import java.util.List;

public interface IJcrShellRenderer {

    void print(Output output);

    void printTableWithHeader(List<String[]> rows);
}
