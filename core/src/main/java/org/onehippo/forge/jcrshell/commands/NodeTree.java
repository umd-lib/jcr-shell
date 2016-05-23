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

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;

import org.onehippo.forge.jcrshell.JcrShellPrinter;
import org.onehippo.forge.jcrshell.JcrWrapper;
import org.onehippo.forge.jcrshell.util.HippoJcrUtils;

/**
 * Print a node tree.
 */
public class NodeTree extends AbstractCommand {

    private static final ArgumentType[] ARGUMENTS = new ArgumentType[] { ArgumentType.INTEGER };

    private StringBuffer prefix = new StringBuffer();

    public NodeTree() {
        super("nodetree", new String[] { "tree" }, "nodetree [<levels>]",
                "print a nodetree number of levels deep, default is 3", ARGUMENTS);
    }

    /**
     * {@inheritDoc}
     * @throws RepositoryException 
     */
    @Override
    protected final boolean executeCommand(final String[] args) throws RepositoryException {
        int maxLevel = 3;
        if (args.length == 2) {
            maxLevel = Integer.valueOf(args[1]);
        }
        Node node = JcrWrapper.getCurrentNode();
        if (node == null) {
            return false;
        }

        printTree(node, 0, maxLevel, 0, 0);

        return true;
    }

    /**
     * Recursive print tree maxLevels deep
     * @param node start node
     * @param level current level
     * @param maxLevel max depth
     * @param childCount total number of child nodes of parent node
     * @param pos position of current child node of parent node
     * @throws RepositoryException
     */
    private void printTree(final Node node, final int level, final int maxLevel, final long childCount, final long pos)
            throws RepositoryException {
        if (level == (maxLevel + 1)) {
            // done..
            return;
        }
        StringBuffer buf = new StringBuffer();
        if (level > 0) {
            buf.append(prefix);
            if (pos == childCount) {
                buf.append("`--");
            } else {
                buf.append("|--");
            }
            buf.append(JcrWrapper.fullName(node));
            if (HippoJcrUtils.isVirtual(node)) {
                buf.append("*");
            }
            buf.append(" {").append(node.getPrimaryNodeType().getName()).append("}");
            JcrShellPrinter.println(buf.toString());

            if (pos == childCount) {
                prefix.append("   ");
            } else {
                prefix.append("|  ");
            }
        } else {
            // treat first node specifically
            buf.append(JcrWrapper.fullName(node));
            buf.append(" {").append(node.getPrimaryNodeType().getName()).append("}");
            JcrShellPrinter.println(buf.toString());
        }
        NodeIterator iter = node.getNodes();
        long size = iter.getSize();
        while (iter.hasNext()) {
            Node n = iter.nextNode();
            printTree(n, (level + 1), maxLevel, size, iter.getPosition());

        }
        if (level > 0) {
            prefix.delete(prefix.length() - 3, prefix.length());
        }
    }

    @Override
    protected boolean hasValidArgs(String[] args) {
        return args.length == 1 || args.length == 2;
    }
}
