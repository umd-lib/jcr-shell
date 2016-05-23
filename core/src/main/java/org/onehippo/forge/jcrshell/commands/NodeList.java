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
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;

import org.onehippo.forge.jcrshell.JcrShellPrinter;
import org.onehippo.forge.jcrshell.JcrWrapper;
import org.onehippo.forge.jcrshell.util.HippoJcrUtils;

/**
 * List child nodes of current node.
 */
public class NodeList extends AbstractCommand {

    private static final ArgumentType[] ARGUMENTS = new ArgumentType[] { ArgumentType.NODE };

    public NodeList() {
        super("ls", new String[] { "dir", "nodelist" }, "ls [<path>]", "list child nodes of the current node",
                ARGUMENTS);
    }

    /**
     * {@inheritDoc}
     * @throws RepositoryException 
     */
    @Override
    protected final boolean executeCommand(final String[] args) throws RepositoryException {

        StringBuilder path = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            if (i > 1) {
                path.append(" ");
            }
            path.append(args[i]);
        }

        NodeIterator iter = JcrWrapper.getNodes(path.toString());
        if (iter == null) {
            JcrShellPrinter.printWarnln("Path not found: " + path);
            return false;
        }

        List<String[]> rows = new ArrayList<String[]>();
        rows.add(new String[] { "Name", "Type" });
        while (iter.hasNext()) {
            Node n = iter.nextNode();
            StringBuilder name = new StringBuilder(JcrWrapper.fullName(n));
            if (HippoJcrUtils.isVirtual(n)) {
                name.append('*');
            }
            rows.add(new String[] { name.toString(), n.getPrimaryNodeType().getName() });
        }
        JcrShellPrinter.printTableWithHeader(rows);
        return true;

    }

    @Override
    protected boolean hasValidArgs(String[] args) {
        return args.length >= 1;
    }
}
