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

/**
 * Remove child node.
 */
public class NodeRemove extends AbstractCommand {

    private static final ArgumentType[] ARGUMENTS = new ArgumentType[] { new ArgumentType(EnumSet.of(
            ArgumentType.Flags.NODE, ArgumentType.Flags.REMOVE)) };

    public NodeRemove() {
        super("noderemove", new String[] { "removenode", "noderm", "rmnode", "delete", "nodedel", "rm" },
                "noderemove <nodename>", "noderm node [,node,[..]]: delete child nodes from the current node",
                ARGUMENTS);
    }

    /**
     * {@inheritDoc}
     * @throws RepositoryException 
     */
    @Override
    protected final boolean executeCommand(final String[] args) throws RepositoryException {
        boolean success = true;

        for (int i = 1; i < args.length; i++) {
            final Node node = JcrWrapper.resolvePath(args[i]);
            if (node == null) {
                success = false;
                JcrShellPrinter.printWarnln("Node does not exist: " + args[i]);
            } else if (node.isSame(node.getSession().getRootNode())) {
                success = false;
                JcrShellPrinter.printWarnln("Cannot remove root node.");
            } else {
                if (JcrWrapper.removeNode(node)) {
                    JcrShellPrinter.println("Node '" + args[i] + "' removed.");
                } else {
                    success = false;
                    JcrShellPrinter.printWarnln("Failed to remove node: " + args[i]);
                }
            }
        }
        return success;
    }

    @Override
    protected boolean hasValidArgs(String[] args) {
        return args.length > 1;
    }
}
