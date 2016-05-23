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

import java.util.EnumSet;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.onehippo.forge.jcrshell.JcrShellPrinter;
import org.onehippo.forge.jcrshell.JcrWrapper;
import org.onehippo.forge.jcrshell.Command.ArgumentType.Flags;

/**
 * Delete property from current node.
 */
public class PropDelete extends AbstractCommand {

    private static final ArgumentType[] ARGUMENTS = new ArgumentType[] { new ArgumentType(EnumSet.of(Flags.PROPERTY,
            Flags.REMOVE)) };

    public PropDelete() {
        super("propdelete", new String[] { "deleteprop" }, "propdelete <property> [<property> [..]]",
                "delete properties from the current node", ARGUMENTS);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final boolean executeCommand(final String[] args) {
        Node node = JcrWrapper.getCurrentNode();

        for (int i = 1; i < args.length; i++) {
            String propName = null;
            try {
                propName = args[i];
                if (!node.hasProperty(propName)) {
                    JcrShellPrinter.printWarnln("Node doesn't have a property with name: " + propName);
                    continue;
                }
                node.getProperty(propName).remove();
                JcrShellPrinter.println("Property '" + propName + "' removed.");
                JcrWrapper.removeFromCache(node.getPath());
            } catch (RepositoryException e) {
                JcrShellPrinter.printErrorln("Error removing property " + propName + " " + e.getMessage());
            }
        }
        return true;

    }

    @Override
    protected boolean hasValidArgs(String[] args) {
        return args.length >= 2;
    }
}
