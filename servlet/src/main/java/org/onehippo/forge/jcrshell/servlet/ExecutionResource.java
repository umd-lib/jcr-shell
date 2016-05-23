package org.onehippo.forge.jcrshell.servlet;

import org.onehippo.forge.jcrshell.*;
import org.onehippo.forge.jcrshell.util.QuotedStringTokenizer;

import javax.jcr.RepositoryException;
import javax.ws.rs.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Path("/{path:.*}")
@Produces("application/json")
public class ExecutionResource {

    private static final String DEFAULT_COMMANDS = "/jcr-shell.commands";
    private static final String EXTRA_COMMANDS = "/extra.commands";

    static {
        CommandHelper.loadCommandsFromResource(DEFAULT_COMMANDS);
        CommandHelper.loadCommandsFromResource(EXTRA_COMMANDS);
    }

    @GET
    public Execution execute(@PathParam("path") String path,
                             @QueryParam("command") String command) {

        Execution execution = new Execution("/" + path);
        JcrShellPrinter.setConsolePrinter(new ServletShellRenderer(execution));
        try {
            JcrWrapper.cd("/" + path);
            handleCommand(command);
            execution.setPath(JcrWrapper.getCurrentNode().getPath());
        } catch (RepositoryException e) {
            throw new WebApplicationException(e);
        } catch (IOException e) {
            throw new WebApplicationException(e);
        } finally {
            JcrShellPrinter.setConsolePrinter(null);
        }
        return execution;
    }

    /**
     * Parse and handle command line.
     *
     * @param line the command line
     * @return true if the command line was successful handled and executed
     * @throws java.io.IOException when the interaction with the shell fails
     */
    private static boolean handleCommand(final String line) throws IOException {
        long tickStart = System.currentTimeMillis();

        String cmdLine = line.trim();
        // # = comment
        if (cmdLine.length() == 0) {
            return true;
        }
        if (cmdLine.startsWith("#")) {
            return true;
        }
        String[] args = tokenizeCommand(cmdLine);

        // white space
        if (args.length == 0) {
            return true;
        }

        String cmd = args[0].trim().toLowerCase(Locale.ENGLISH);
        if (CommandHelper.isAlias(cmd)) {
            cmd = CommandHelper.getCommandForAlias(cmd);
        }

        if (!CommandHelper.isCommand(cmd)) {
            JcrShellPrinter.printWarnln("Unknown command: " + cmd);
            return false;
        }

        Command command = CommandHelper.getCommandInstance(cmd);
        if (command != null) {
            try {
                boolean ret = command.execute(args);
                JcrShellPrinter.printDebugln("Command completed in " + (System.currentTimeMillis() - tickStart) + " msecs.");
                return ret;
            } catch (RepositoryException e) {
                JcrShellPrinter.printErrorln(e.getClass().getSimpleName() + " while executing " + cmd + ": " + e.getMessage());
            } catch (IOException e) {
                JcrShellPrinter.printErrorln("IO exception while executing " + cmd + ": " + e.getMessage());
            } catch (NoConnectionException e) {
                JcrShellPrinter.printErrorln(e.getMessage());
            } catch (RuntimeException e) {
                JcrShellPrinter.printErrorln("Unexpected error " + e.getClass().getSimpleName() + " while executing command: " + e.getMessage());
            }
        }
        return false;
    }

    /**
     * Tokenize the command line on whitespace.
     *
     * @param line commandline
     * @return String array with tokens
     */
    private static String[] tokenizeCommand(final String line) {
        List<String> parts = new ArrayList<String>();

        QuotedStringTokenizer tok = new QuotedStringTokenizer(line, " \t\n\r", false, false);
        while (tok.hasMoreElements()) {
            String c = tok.nextToken();
            if (c != null) {
                parts.add(c);
            }
        }
        return parts.toArray(new String[parts.size()]);
    }

}
