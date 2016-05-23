package org.onehippo.forge.jcrshell.console;

import jline.console.ConsoleReader;
import org.fusesource.jansi.Ansi;
import org.onehippo.forge.jcrshell.IJcrShellRenderer;
import org.onehippo.forge.jcrshell.output.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import static org.fusesource.jansi.Ansi.Attribute.INTENSITY_BOLD;
import static org.fusesource.jansi.Ansi.Attribute.INTENSITY_BOLD_OFF;
import static org.fusesource.jansi.Ansi.Color.*;
import static org.fusesource.jansi.Ansi.Color.GREEN;
import static org.fusesource.jansi.Ansi.Color.RED;
import static org.fusesource.jansi.Ansi.ansi;

public class ConsoleRenderer implements IJcrShellRenderer {

    private static final Logger log = LoggerFactory.getLogger(ConsoleRenderer.class);

    public static final String CR = System.getProperty("line.separator");
    public static final int RIGHT_MARGIN = 8;
    private ConsoleReader consoleReader;

    public ConsoleRenderer(final ConsoleReader reader) {
        consoleReader = reader;
    }

    @Override
    public void print(Output output) {
        Ansi ansi = ansi();
        do {
            if (output instanceof DebugOutput) {
                ansi = ansi.fg(BLUE).a(output.getText()).fg(WHITE);
            } else if (output instanceof OkOutput) {
                ansi = ansi.fg(GREEN).a(output.getText()).fg(WHITE);
            } else if (output instanceof WarnOutput) {
                ansi = ansi.fg(YELLOW).a(output.getText()).fg(WHITE);
            } else if (output instanceof ErrorOutput) {
                ansi = ansi.fg(RED).a(output.getText()).fg(WHITE);
            } else {
                ansi = ansi.a(output.getText());
            }

            output = output.next();
        } while(output != null);

        printlnToConsole(ansi.toString());
    }

    private void printlnToConsole(final CharSequence s) {
        printToConsole(s + CR);
    }

    //------------------- print to console ----------------------------//
    private void printToConsole(final CharSequence s) {
        try {
            consoleReader.print(s);
            consoleReader.flush();
        } catch (IOException e) {
            log.error("Error while write to console", e);
        }
    }

    //------------------- table print helpers ----------------------------//
    public void printTableWithHeader(List<String[]> rows) {
        if (rows == null || rows.size() == 0) {
            return;
        }

        int[] widths = calculatedWidths(rows);

        Iterator<String[]> rowIter = rows.iterator();
        String[] header = rowIter.next();
        printTableHeader(header, widths);

        while (rowIter.hasNext()) {
            printTableRow(rowIter.next(), widths);
        }
        printTableLine();
    }

    private void printlnToConsole() {
        printToConsole(CR);
    }

    private void printTableHeader(String[] header, int[] widths) {
        printTableLine();
        for (int i = 0; i < header.length; i++) {
            printToConsole(ansi().fg(YELLOW).a(INTENSITY_BOLD).format("%-" + widths[i] + "s", header[i]).a(
                    INTENSITY_BOLD_OFF).fg(WHITE).toString());
        }
        printlnToConsole();
        printTableLine();
    }

    private void printTableRow(String[] row, int[] widths) {
        for (int i = 0; i < row.length; i++) {
            printToConsole(String.format("%-" + widths[i] + "s", row[i]));
        }
        printlnToConsole();
    }

    private void printTableLine() {
        int width = consoleReader.getTerminal().getWidth() - RIGHT_MARGIN;
        StringBuilder sb = new StringBuilder(width);
        for (int i = 0; i < width; i++) {
            sb.append('-');
        }
        printlnToConsole(ansi().fg(BLUE).a(sb).fg(WHITE).toString());
    }

    private int[] calculatedWidths(List<String[]> rows) {
        int columns = rows.get(0).length;
        int lineWidth = consoleReader.getTerminal().getWidth();
        int[] maxWidths = new int[columns];
        for (String[] row : rows) {
            for (int i = 0; i < columns; i++) {
                if (row[i].length() > maxWidths[i]) {
                    maxWidths[i] = row[i].length();
                }
            }
        }
        // add some space between
        boolean resizeNeeded = false;
        for (int i = 0; i < columns; i++) {
            maxWidths[i] += 2;
            if (maxWidths[i] > lineWidth) {
                resizeNeeded = true;
            }
        }
        return maxWidths;
    }

}
