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
 * Go to previous location.
 */
public class CdPrevious extends AbstractCommand {

    public CdPrevious() {
        super("cdprev", new String[] { "p", "back" }, "cdprev", "change to previous directory");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final boolean executeCommand(final String[] args) {
        if (!JcrWrapper.cdPrevious()) {
            JcrShellPrinter.printWarnln("Previous directory not (yet) set.");
            return false;
        }
        return true;
    }

    @Override
    protected boolean hasValidArgs(String[] args) {
        return args.length == 1;
    }
}
