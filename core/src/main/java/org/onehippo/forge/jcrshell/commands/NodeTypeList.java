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

import java.util.Set;

import javax.jcr.RepositoryException;

import org.onehippo.forge.jcrshell.JcrWrapper;
import org.onehippo.forge.jcrshell.JcrShellPrinter;

/**
 * List child nodes of current node.
 */
public class NodeTypeList extends AbstractCommand {

    private static final ArgumentType[] ARGUMENTS = new ArgumentType[] { ArgumentType.STRING };

    public NodeTypeList() {
        super("nodetypelist", new String[] { "listnodetypes", "ntlist" }, "nodetypelist [<primary|mixin>]",
                "list registered nodetypes", ARGUMENTS);
    }

    /**
     * {@inheritDoc}
     * @throws RepositoryException 
     */
    @Override
    protected final boolean executeCommand(final String[] args) throws RepositoryException {
        Set<String> types = null;
        if (args.length == 2) {
            types = JcrWrapper.getNodeTypes(args[1]);
        } else {
            types = JcrWrapper.getNodeTypes("all");
        }
        for (String type : types) {
            JcrShellPrinter.println(type);
        }
        return true;
    }

    @Override
    protected boolean hasValidArgs(String[] args) {
        if (args.length == 1) {
            return true;
        } else if (args.length == 2) {
            return "mixin".equals(args[1]) || "primary".equals(args[1]);
        } else {
            return false;
        }
    }
}
