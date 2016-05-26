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
import org.onehippo.forge.jcrshell.JcrShellPrinter;

/**
 * Set the server rmi url.
 */
public class Server extends AbstractCommand {

    public Server() {
        super("server", new String[] {}, "server [<rmi_url>]",
                "show current server or set server rmi url to connect to");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final boolean executeCommand(final String[] args) {
        if (args.length == 1) {
            JcrShellPrinter.println("Current server: " + JcrWrapper.getServer());
        } else if (args.length == 2) {
            JcrWrapper.setServer(args[1]);
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
