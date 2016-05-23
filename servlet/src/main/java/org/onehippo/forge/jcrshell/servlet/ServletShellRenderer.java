package org.onehippo.forge.jcrshell.servlet;

import org.onehippo.forge.jcrshell.IJcrShellRenderer;
import org.onehippo.forge.jcrshell.output.*;

import java.util.Arrays;
import java.util.List;

public class ServletShellRenderer implements IJcrShellRenderer {

    private final Execution execution;

    public ServletShellRenderer(Execution execution) {
        this.execution = execution;
    }

    @Override
    public void print(Output output) {
        Line line = new Line();
        do {
            if (output instanceof DebugOutput) {
                line.addText(Line.TextMode.DEBUG, output.getText());
            } else if (output instanceof OkOutput) {
                line.addText(Line.TextMode.OK, output.getText());
            } else if (output instanceof WarnOutput) {
                line.addText(Line.TextMode.WARN, output.getText());
            } else if (output instanceof ErrorOutput) {
                line.addText(Line.TextMode.ERROR, output.getText());
            } else {
                line.addText(Line.TextMode.PLAIN, output.getText());
            }

            output = output.next();
        } while(output != null);

        execution.addMessage(line);
    }

    @Override
    public void printTableWithHeader(List<String[]> rows) {
        Table table = new Table(Arrays.asList(rows.get(0)));
        boolean first = true;
        for (String[] row : rows) {
            if (first) {
                first = false;
                continue;
            }
            table.addRow(Arrays.asList(row));
        }
        execution.addMessage(table);
    }
}
