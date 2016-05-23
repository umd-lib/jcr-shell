/*
 *  Copyright 2010 Hippo.
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
package org.onehippo.forge.jcrshell.commands;

import java.io.IOException;

import javax.jcr.RepositoryException;

import org.onehippo.forge.jcrshell.Command;
import org.onehippo.forge.jcrshell.JcrWrapper;
import org.onehippo.forge.jcrshell.JcrShellPrinter;
import static org.onehippo.forge.jcrshell.output.Output.out;

/**
 * Base class which can be used to easily implement a {@link Command}. 
 */
public abstract class AbstractCommand implements Command {

    private final String command;
    private final String usage;
    private final String help;

    private String[] aliases;
    private ArgumentType[] arguments;

    protected AbstractCommand(String command, String[] aliases, String usage, String help, ArgumentType[] arguments) {
        this.command = command;
        this.aliases = aliases.clone();
        this.usage = usage;
        this.help = help;
        this.arguments = arguments.clone();
    }

    protected AbstractCommand(String command, String[] aliases, String usage, String help) {
        this(command, aliases, usage, help, new ArgumentType[0]);
    }

    protected AbstractCommand(String command, String usage, String help) {
        this(command, new String[0], usage, help, new ArgumentType[0]);
    }

    protected final void setAliases(String[] aliases) {
        this.aliases = aliases.clone();
    }

    protected final void setArgumentTypes(ArgumentType[] arguments) {
        this.arguments = arguments.clone();
    }

    /**
     * {@inheritDoc}
     */
    public String getCommand() {
        return command;
    }

    /**
     * {@inheritDoc}
     */
    public String[] getAliases() {
        return aliases.clone();
    }

    /**
     * {@inheritDoc}
     */
    public String usage() {
        return usage;
    }

    /**
     * {@inheritDoc}
     */
    public String help() {
        return help;
    }

    /**
     * {@inheritDoc}
     */
    public ArgumentType[] getArgumentTypes() {
        return arguments.clone();
    }

    /**
     * Checks the arguments and calls executeCommand.
     */
    public final boolean execute(final String[] args) throws RepositoryException, IOException {
        if (!hasValidArgs(args)) {
            showUsage();
            return false;
        }
        if (needsLiveSession()) {
            JcrWrapper.connect();
        }
        return executeCommand(args);
    }

    /**
     * Execute the {@link Command} after the arguments have been checked.
     * @param args
     * @return true when the command succeeds
     */
    protected abstract boolean executeCommand(final String[] args) throws RepositoryException, IOException;

    /**
      * Hook for checking arguments before execution of the command.
     */
    protected abstract boolean hasValidArgs(final String[] args);

    /**
     * Override this method if the command doesn't need a live session.
     * @return true if the command needs a live session.
     */
    protected boolean needsLiveSession() {
        return true;
    }

    // --------------------------- helper methods ----------------------- //

    /**
     * Print help and usage to the org.onehippo.forge.jcrshell.console.
     */
    protected void showUsage() {
        JcrShellPrinter.println("Usage: " + usage());
        JcrShellPrinter.println(help());
    }
}
