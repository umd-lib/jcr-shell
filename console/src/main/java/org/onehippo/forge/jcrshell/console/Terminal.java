/*
 *  Copyright 2008 Hippo.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.onehippo.forge.jcrshell.console;

import jline.UnsupportedTerminal;
import jline.console.ConsoleReader;
import jline.console.history.FileHistory;
import jline.console.history.History;
import org.apache.commons.io.IOUtils;
import org.fusesource.jansi.AnsiConsole;
import org.onehippo.forge.jcrshell.*;
import org.onehippo.forge.jcrshell.completers.ShellCompleter;
import org.onehippo.forge.jcrshell.output.TextOutput;
import org.onehippo.forge.jcrshell.util.QuotedStringTokenizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.onehippo.forge.jcrshell.output.Output.out;

/**
 * Manage the terminal (with jline).
 */
public class Terminal {

    /** logger */
    private static final Logger log = LoggerFactory.getLogger(Terminal.class);
    
    /**
     * Displayed when the program first starts. Provides for customization
     * option.
     */
    private String commandLineHeader = null;

    /**
     * Provides an unchanging prompt for each line of input. Provides for
     * customization option.
     */
    private String commandLinePrompt = "> ";

    /**
     * Displayed when the program first starts. Provides for customization
     * option.
     */
    private String commandLineVersion = null;

    /**
     * Jline history file name.
     */
    private static final String HISTORYFILE = ".javaterm"; // history file in user's

    /**
     * The console reader from jline.
     */
    private static ConsoleReader consoleReader = createConsoleReader();

    /**
     * Helper method for creating the console
     * @return
     */
    private static ConsoleReader createConsoleReader() {
        try {
            return new ConsoleReader();
        } catch (IOException e) {
            // just print error and exit, there's no use to continue if the console reader creation fails
            log.error("Unable to create the console", e);
        }
        return null;
    }

    /**
     * Main entry point. The first argument can be a filename with an
     * application initialization file.
     * @throws IOException
     */
    public final void start() {
        if (consoleReader == null) {
            log.error("Console not available");
            return;
        }

        // enable ansi codes on windows
        AnsiConsole.systemInstall();

        // initialize JcrShellPrinter
        JcrShellPrinter.setConsolePrinter(new ConsoleRenderer(consoleReader));
        
        // initialize history
        String historyFile = System.getProperty("user.home") + File.separator + HISTORYFILE;
        try {
            consoleReader.setHistory(new FileHistory(new File(historyFile)));
        } catch (IOException e) {
            log.warn("Unable to use history file", e);
            JcrShellPrinter.printErrorln("History not available");
        }
        
        
        // set completer with list of words
        consoleReader.addCompleter(new ShellCompleter());

        if (getCommandLineHeader() != null) {
            JcrShellPrinter.println(getCommandLineHeader());
        }
        if (getCommandLineVersion() != null) {
            JcrShellPrinter.println(getCommandLineVersion());
        }
        JcrShellPrinter.println("exit or quit leaves program.");
        JcrShellPrinter.println("help lists commands.");

        // main input loop
        boolean keepRunning = true;
        String line;
        while (keepRunning) {
            try {
                line = consoleReader.readLine(getCommandLinePrompt());
                if (line != null) {
                    handleCommand(line);
                } else {
                    // Ctrl-D, do proper exit
                    handleCommand("exit");
                }
            } catch (JcrShellShutdownException e) {
                // thrown by exit command
                keepRunning = false;
            } catch (java.io.EOFException e) {
                keepRunning = false;
            } catch (IOException e) {
                log.error("IOException while handling command", e);
            }
        }

        // cleanup
        JcrWrapper.logout();
        JcrShellPrinter.printWarnln("Bye bye!");
        
        // clear line
        try {
            History history = consoleReader.getHistory();
            if (history instanceof Flushable) {
                ((Flushable) history).flush();
            }
        } catch (IOException e) {
            log.error("Error while saving command history", e);
        }
    }

    public static final void run(InputStream input, OutputStream output) {
        String cr = System.getProperty("line.separator");
        Writer writer = new OutputStreamWriter(output);
        BufferedReader br = new BufferedReader(new InputStreamReader(input));
        try {
            consoleReader = new ConsoleReader(new FileInputStream(FileDescriptor.in), new PrintWriter(writer), null,
                    new UnsupportedTerminal());
            while (br.ready()) {
                String line = br.readLine();
                if (line != null && !line.startsWith("#") && line.trim().length() > 0) {
                    writer.append("Executing: ").append(line).append(cr);
                    writer.flush();
                    try {
                        if (!handleCommand(line)) {
                            writer.append("Command failed, exiting..").append(cr);
                            handleCommand("Exit" + cr);
                            break;
                        }
                    } catch (JcrShellShutdownException e) {
                        break;
                    }
                }
            }
            writer.append("Finished.").append(cr).flush();
        } catch (IOException e) {
            PrintWriter pw = new PrintWriter(writer);
            e.printStackTrace(pw);
            pw.flush();
        } finally {
            IOUtils.closeQuietly(input);
            IOUtils.closeQuietly(output);
            JcrWrapper.logout();
        }
    }

    /**
     * Parse and handle command line.
     * @param line the command line
     * @return true if the command line was successful handled and executed
     * @throws IOException when the interaction with the shell fails
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
            } catch (JcrShellShutdownException e) {
                // retrow to prevent the generic catch below
                throw e;
            } catch (RuntimeException e) {
                log.error("Error while executing command {}", command.getCommand(), e);
                JcrShellPrinter.printErrorln("Unexpected error " + e.getClass().getSimpleName() + " while executing command: " + e.getMessage());
            }
        } 
        return false;
    }

    /**
     * Tokenize the command line on whitespace.
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

    /**
     * Getter for commandLineHeader variable.
     * @return the command line header.
     */
    public final String getCommandLineHeader() {
        return commandLineHeader;
    }

    /**
     * Getter for commandLinePrompt variable.
     * @return the command line prompt.
     */
    public final String getCommandLinePrompt() {
        return commandLinePrompt;
    }

    /**
     * Getter for commandLineVersion variable.
     * @return the command line version.
     */
    public final String getCommandLineVersion() {
        return commandLineVersion;
    }

    /**
     * Setter for the commandLineHeader variable.
     * @param string The command line header.
     */
    public final void setCommandLineHeader(final String string) {
        commandLineHeader = string;
    }

    /**
     * Setter for the commandLinePrompt variable.
     * @param string The prompt.
     */
    public final void setCommandLinePrompt(final String string) {
        commandLinePrompt = string;
    }

    /**
     * Setter for the commandLineVersion variable.
     * @param string The version.
     */
    public final void setCommandLineVersion(final String string) {
        commandLineVersion = string;
    }

    /**
     * Get the custom shutdown hook.
     * @return the shutdown hook
     */
    public static final ShutdownHook getShutdownHook() {
        return new ShutdownHook();
    }

    /**
     * Read password from terminal with masking.
     * @return the entered password
     */
    public static final String getPassword() {
        try {
            return consoleReader.readLine("password: ", '*');
        } catch (IOException e) {
            log.error("Error while reading password", e);
            return null;
        }
    }

    /**
     * Trivial shutdown hook class.
     */
    static class ShutdownHook extends Thread {
        /**
         * Exit properly on shutdown.
         */
        @Override
        public void run() {
            try {
                JcrShellPrinter.println("");
                TextOutput text = out().a("Shuting down..");
                JcrWrapper.logout();
                JcrShellPrinter.print(text.ok("done."));
            } catch (JcrShellShutdownException e) {
                // ignore, expected, thrown by exit command
            }
        }
    }
}
