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
package org.onehippo.forge.jcrshell.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.onehippo.forge.jcrshell.Command;
import org.onehippo.forge.jcrshell.CommandHelper;
import org.onehippo.forge.jcrshell.JcrShellPrinter;

import static org.onehippo.forge.jcrshell.output.Output.out;

/**
 * Help command.
 */
public class Help extends AbstractCommand {

    private static final ArgumentType[] ARGUMENTS = new ArgumentType[] { ArgumentType.COMMAND };

    public Help() {
        super("help", new String[] { "?", "commands" }, "help [<command>]",
                "print general help message or the help of the command", ARGUMENTS);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final boolean executeCommand(final String[] args) {
        if (args.length == 1) {
            String[] commands = CommandHelper.getCommandsAsArray();
            Arrays.sort(commands);

            List<String[]> rows = new ArrayList<String[]>();
            rows.add(new String[] { "Command", "Usage" });
            for (String command : commands) {
                Command cmd = CommandHelper.getCommandInstance(command);
                if (cmd != null) {
                    rows.add(new String[] { command, cmd.usage() });
                }
            }
            JcrShellPrinter.printTableWithHeader(rows);
        } else if (args.length == 2) {
            Command cmd = CommandHelper.getCommandInstance(args[1]);
            if (cmd == null) {
                JcrShellPrinter.println("Command or alias not found: " + args[1]);
                JcrShellPrinter.println("Usage: " + usage());
                JcrShellPrinter.println("   " + help());
            } else {
                JcrShellPrinter.println("Usage: " + cmd.usage());
                JcrShellPrinter.println("   " + cmd.help());
            }
        } else {
            JcrShellPrinter.println("Usage: " + usage());
            JcrShellPrinter.println("   " + help());
        }
        return true;
    }

    @Override
    protected boolean needsLiveSession() {
        return false;
    }

    @Override
    protected boolean hasValidArgs(String[] args) {
        return args.length == 1 || args.length == 2;
    }
}
