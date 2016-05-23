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

import javax.jcr.RepositoryException;

import org.onehippo.forge.jcrshell.JcrShellPrinter;
import org.onehippo.forge.jcrshell.JcrWrapper;

/**
 * List child nodes of current node.
 */
public class NamespaceRemove extends AbstractCommand {

    private static final ArgumentType[] ARGUMENTS = new ArgumentType[] { ArgumentType.STRING };

    public NamespaceRemove() {
        super("namespaceremove", new String[] { "removenamespace" }, "namespaceremove <prefix>",
                "Unregister namespace", ARGUMENTS);
    }

    /**
     * {@inheritDoc}
     * @throws RepositoryException 
     */
    @Override
    protected final boolean executeCommand(final String[] args) throws RepositoryException {
        if (JcrWrapper.removeNamespace(args[1])) {
            JcrShellPrinter.printOkln("Namespace " + args[1] + " unregistered.");
            return true;
        } else {
            JcrShellPrinter.printWarnln("Failed to unregister namespace.");
            return false;
        }
    }

    @Override
    protected boolean hasValidArgs(String[] args) {
        return args.length == 2;
    }
}
