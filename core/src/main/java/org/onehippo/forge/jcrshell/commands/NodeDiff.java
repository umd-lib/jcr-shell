/*
 *  Copyright 2010 Hippo.
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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Stack;

import javax.jcr.ItemExistsException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

import org.onehippo.forge.jcrshell.JcrShellPrinter;
import org.onehippo.forge.jcrshell.JcrWrapper;
import org.onehippo.forge.jcrshell.diff.Change;
import org.onehippo.forge.jcrshell.diff.JcrDiff;
import org.onehippo.forge.jcrshell.diff.PropertyChange;
import org.onehippo.forge.jcrshell.diff.PropertyChanged;

/**
 * Diff child node.
 */
public class NodeDiff extends AbstractCommand {

    private static final ArgumentType[] ARGUMENTS = new ArgumentType[] { ArgumentType.NODE, ArgumentType.NODE };

    public NodeDiff() {
        super("nodediff", new String[] { "diff" }, "nodediff <nodeame> [<path>/]<base>",
                "nodediff node target: compare node to target", ARGUMENTS);
    }

    /**
     * {@inheritDoc}
     * @throws RepositoryException 
     */
    @Override
    protected final boolean executeCommand(final String[] args) throws RepositoryException {

        Node node = JcrWrapper.getCurrentNode();

        String src = args[1];
        String dest = args[2];

        // tab completion slash
        if (src.endsWith("/")) {
            src = src.substring(0, src.length() - 1);
        }

        Node srcNode = null;
        if (src.startsWith("/")) {
            if (!node.getSession().getRootNode().hasNode(src.substring(1))) {
                JcrShellPrinter.println("Src node not found: " + src);
                return false;
            }
            srcNode = node.getSession().getRootNode().getNode(src.substring(1));
        } else {
            if (!node.hasNode(src)) {
                JcrShellPrinter.println("Src node not found: " + src);
                return false;
            }
            srcNode = node.getNode(src);
        }
        if (!dest.startsWith("/")) {
            if (node.getPath().equals("/")) {
                dest = "/" + dest;
            } else {
                dest = node.getPath() + "/" + dest;
            }
        }
        try {
            Node destNode = (Node) node.getSession().getItem(dest);
            Iterator<Change> diff = JcrDiff.compare(srcNode, destNode);

            Stack<String> stack = new Stack<String>();
            stack.add("");

            List<String[]> rows = new ArrayList<String[]>();
            rows.add(new String[] { "", "Name", "Type", "Value" });

            while (diff.hasNext()) {
                Change change = diff.next();
                String path = change.getPath();

                String rootPath;
                if (change.isAddition()) {
                    rootPath = destNode.getPath();
                } else {
                    rootPath = srcNode.getPath();
                }
                if ("/".equals(rootPath)) {
                    rootPath = "";
                }

                path = path.substring(rootPath.length());
                path = path.substring(0, path.lastIndexOf('/'));

                LinkedList<String> ancestors = new LinkedList<String>();
                while (!stack.contains(path)) {
                    ancestors.add(path);
                    path = path.substring(0, path.lastIndexOf('/'));
                }

                String last = stack.peek();
                while (!path.equals(last)) {
                    stack.pop();
                    last = stack.peek();
                }

                int depth = stack.size();
                for (ListIterator<String> iter = ancestors.listIterator(ancestors.size()); iter.hasPrevious();) {
                    String ancestor = iter.previous();
                    stack.push(ancestor);

                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < depth; i++) {
                        sb.append("  ");
                    }
                    sb.append(ancestor.substring(ancestor.lastIndexOf('/') + 1));
                    rows.add(new String[] { "", sb.toString(), "", "" });
                    depth++;
                }
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < depth; i++) {
                    sb.append("  ");
                }
                String prefix = sb.toString();

                if (!change.isPropertyChange()) {
                    rows.add(new String[] { change.isRemoval() ? "-" : "+", prefix + change.getName(),
                            change.getType(), "" });
                } else {
                    if (change.isAddition() || change.isRemoval()) {
                        PropertyChange pc = (PropertyChange) change;
                        rows.add(new String[] { change.isRemoval() ? "-" : "+", prefix + change.getName(),
                                pc.getType(), pc.getValue() });
                    } else {
                        PropertyChanged pcd = (PropertyChanged) change;
                        PropertyChange removal = pcd.getRemoval();
                        rows.add(new String[] { "-", prefix + pcd.getName(), removal.getType(), removal.getValue() });

                        PropertyChange addition = pcd.getAddition();
                        rows.add(new String[] { "+", prefix + pcd.getName(), addition.getType(), addition.getValue() });
                    }
                }
            }
            JcrShellPrinter.printTableWithHeader(rows);
        } catch (ItemExistsException e) {
            JcrShellPrinter.printWarnln("Target already exists: " + dest);
        } catch (PathNotFoundException e) {
            JcrShellPrinter.printWarnln("Target not found: " + dest);
        }

        return true;

    }

    @Override
    protected boolean hasValidArgs(String[] args) {
        return args.length == 3;
    }
}
