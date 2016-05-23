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
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Stack;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.version.VersionHistory;

import org.onehippo.forge.jcrshell.JcrShellPrinter;
import org.onehippo.forge.jcrshell.JcrWrapper;
import org.onehippo.forge.jcrshell.diff.Change;
import org.onehippo.forge.jcrshell.diff.JcrDiff;
import org.onehippo.forge.jcrshell.diff.PropertyChange;
import org.onehippo.forge.jcrshell.diff.PropertyChanged;

/**
 * Diff the current node with one in the version history.
 */
public class VersionDiff extends AbstractCommand {

    private static final ArgumentType[] ARGUMENTS = new ArgumentType[] {
            new ArgumentType(EnumSet.of(ArgumentType.Flags.VERSION, ArgumentType.Flags.NO_LABELS)),
            new ArgumentType(EnumSet.of(ArgumentType.Flags.VERSION, ArgumentType.Flags.NO_LABELS)) };

    public VersionDiff() {
        super("versiondiff", new String[0], "versiondiff [version [<otherversion>]]",
                "versiondiff node target: compare node to target", ARGUMENTS);
    }

    /**
     * {@inheritDoc}
     * @throws RepositoryException 
     */
    @Override
    protected final boolean executeCommand(final String[] args) throws RepositoryException {
        VersionHistory vh = JcrWrapper.getVersionHistory();

        Node base = null;
        Node current = null;

        if (args.length == 2) {
            base = vh.getVersion(args[1]);
            current = JcrWrapper.getCurrentNode();
        } else {
            base = vh.getVersion(args[1]);
            current = vh.getVersion(args[2]);
        }

        Iterator<Change> diff = JcrDiff.compare(base, current);

        Stack<String> stack = new Stack<String>();
        stack.add("");

        List<String[]> rows = new ArrayList<String[]>();
        rows.add(new String[] { "", "Name", "Type", "Value" });

        while (diff.hasNext()) {
            Change change = diff.next();
            String path = change.getPath();

            String rootPath;
            if (change.isAddition()) {
                rootPath = current.getPath();
            } else {
                rootPath = base.getPath();
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
                rows
                        .add(new String[] { change.isRemoval() ? "-" : "+", prefix + change.getName(),
                                change.getType(), "" });
            } else {
                if (change.isAddition() || change.isRemoval()) {
                    PropertyChange pc = (PropertyChange) change;
                    rows.add(new String[] { change.isRemoval() ? "-" : "+", prefix + change.getName(), pc.getType(),
                            pc.getValue() });
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
        return true;

    }

    @Override
    protected boolean hasValidArgs(String[] args) {
        return args.length == 2 || args.length == 3;
    }
}
