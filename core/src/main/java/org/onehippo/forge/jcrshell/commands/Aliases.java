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

import org.onehippo.forge.jcrshell.CommandHelper;
import org.onehippo.forge.jcrshell.JcrShellPrinter;

/**
 * Show aliases.
 */
public class Aliases extends AbstractCommand {

    private static final String COMMAND = "aliases";
    private static final String[] ALIASES = new String[] {};
    private static final String USAGE = "aliases";
    private static final String HELP = "Show available aliases";
    private static final ArgumentType[] ARGUMENTS = new ArgumentType[] {};

    public Aliases() {
        super(COMMAND, ALIASES, USAGE, HELP, ARGUMENTS);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean executeCommand(final String[] args) {
        List<String[]> rows = new ArrayList<String[]>();
        rows.add(new String[] { "Command", "Alias" });
        String[] commands = CommandHelper.getCommandsAsArray();
        for (String command : commands) {
            String[] commandAliases = CommandHelper.getAliasesForCommand(command);
            if (commandAliases.length > 0) {
                rows.add(new String[] { command, Arrays.toString(commandAliases) });
            }
        }
        JcrShellPrinter.printTableWithHeader(rows);
        return true;
    }

    @Override
    protected boolean needsLiveSession() {
        return false;
    }

    @Override
    protected boolean hasValidArgs(String[] args) {
        return args.length == 1;
    }
}
