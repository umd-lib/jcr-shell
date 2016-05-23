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

import org.onehippo.forge.jcrshell.JcrWrapper;
import org.onehippo.forge.jcrshell.console.Terminal;

/**
 * Set credentials for login.
 * TODO: don't echo password to screen
 */
public class Credentials extends AbstractCommand {

    private static final ArgumentType[] ARGUMENTS = new ArgumentType[] { ArgumentType.USER, ArgumentType.STRING };

    public Credentials() {
        super("credentials", new String[] { "username" }, "credentials <username>",
                "credentials <user> [<password>]: set the credentials for the server", ARGUMENTS);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final boolean executeCommand(final String[] args) {
        JcrWrapper.setUsername(args[1]);
        if (args.length == 3) {
            JcrWrapper.setPassword(args[2]);
        } else {
            JcrWrapper.setPassword(Terminal.getPassword());
        }
        return true;
    }

    @Override
    protected boolean needsLiveSession() {
        return false;
    }

    @Override
    protected boolean hasValidArgs(String[] args) {
        return args.length == 2 || args.length == 3;
    }
}
