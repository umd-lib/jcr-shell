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

import org.apache.jackrabbit.util.ISO8601;
import org.onehippo.forge.jcrshell.JcrShellPrinter;
import org.onehippo.forge.jcrshell.JcrWrapper;
import org.onehippo.forge.jcrshell.output.Output;
import org.onehippo.forge.jcrshell.output.TextOutput;

import javax.jcr.RepositoryException;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;

/**
 * Save the current node.
 */
public class VersionInfo extends AbstractCommand {

    public VersionInfo() {
        super("versioninfo", new String[0], "versioninfo [<version>]",
                "show version info");
    }

    /**
     * {@inheritDoc}
     * @throws RepositoryException 
     */
    @Override
    protected final boolean executeCommand(final String[] args) throws RepositoryException {
        VersionHistory vh = JcrWrapper.getVersionHistory();

        Version v = JcrWrapper.getCurrentNode().getBaseVersion();
        if (args.length == 2) {
            v = vh.getVersion(args[1]);
        }
        JcrShellPrinter.println("Base version : " + JcrWrapper.getCurrentNode().getBaseVersion().getName());
        JcrShellPrinter.println("Version      : " + v.getName());
        JcrShellPrinter.println("Created      : " + ISO8601.format(v.getCreated()));
        JcrShellPrinter.println("Version      : " + v.getName());

        TextOutput text = Output.out().a("Predecessors :");
        for (Version p : v.getPredecessors()) {
            text = text.a(" " + p.getName());
        }
        JcrShellPrinter.print(text);

        text = Output.out().a("Successors   :");
        for (Version s : v.getSuccessors()) {
            text = text.a(" " + s.getName());
        }
        JcrShellPrinter.print(text);
        
        return true;
    }

    @Override
    protected boolean hasValidArgs(String[] args) {
        return args.length == 1 || args.length == 2;
    }
}
