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

import org.onehippo.forge.jcrshell.JcrShellPrinter;
import org.onehippo.forge.jcrshell.JcrWrapper;
import org.onehippo.forge.jcrshell.output.Output;
import org.onehippo.forge.jcrshell.output.TextOutput;
import org.onehippo.forge.jcrshell.util.StringUtils;

import javax.jcr.RepositoryException;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;

/**
 * Print a node tree.
 */
public class VersionTree extends AbstractCommand {

    private static final int DEFAULT_MAX_LEVELS = 5;

    private static final ArgumentType[] ARGUMENTS = new ArgumentType[] { ArgumentType.INTEGER };

    private StringBuffer prefix = new StringBuffer();

    public VersionTree() {
        super("versiontree", new String[0], "versiontree [<levels>]",
                "print a version tree number of levels deep, default is 5", ARGUMENTS);
    }

    /**
     * {@inheritDoc}
     * @throws RepositoryException 
     */
    @Override
    protected final boolean executeCommand(final String[] args) throws RepositoryException {
        int maxLevel = DEFAULT_MAX_LEVELS;
        if (args.length == 2) {
            maxLevel = Integer.valueOf(args[1]);
        }
        VersionHistory vh = JcrWrapper.getVersionHistory();
        Version root = vh.getRootVersion();

        printTree(root, 0, maxLevel, 0, 0);

        return true;
    }

    /**
     * Recursive print tree maxLevels deep
     * @param version start node
     * @param level current level
     * @param maxLevel max depth
     * @param childCount total number of child nodes of parent node
     * @param pos position of current child node of parent node
     * @throws RepositoryException
     */
    private void printTree(final Version version, final int level, final int maxLevel, final long childCount, final long pos)
            throws RepositoryException {
        if (level == (maxLevel + 1)) {
            // done..
            return;
        }
        if (level > 0) {
            TextOutput text = Output.out().a(prefix.toString());
            if (pos == childCount) {
                text = text.a("`--");
            } else {
                text = text.a("|--");
            }
            text = text.ok(version.getName() + " ")
                    .a(StringUtils.join(version.getContainingHistory().getVersionLabels(version), ", "));
            JcrShellPrinter.print(text);
            
            if (pos == childCount) {
                prefix.append("   ");
            } else {
                prefix.append("|  ");
            }
        } else {
            // treat first node specifically
            JcrShellPrinter.printWarnln(version.getName());
        }
        Version[] successors = version.getSuccessors();
        long size = successors.length;
        int sucPos = 1;
        for (Version s : successors) {
            printTree(s, (level + 1), maxLevel, size, sucPos);
            sucPos++;
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
