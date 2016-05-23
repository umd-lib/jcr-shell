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

import org.onehippo.forge.jcrshell.JcrShellPrinter;
import org.onehippo.forge.jcrshell.JcrWrapper;
import org.onehippo.forge.jcrshell.output.Output;
import org.onehippo.forge.jcrshell.output.TextOutput;

import javax.jcr.RepositoryException;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionIterator;

/**
 * Remove all versions from the version history.
 */
public class VersionRemoveAll extends AbstractCommand {

    public VersionRemoveAll() {
        super("versionremoveall", new String[] { "removeallversions" }, "versionremoveall",
                "remove all versions of the current node");
    }

    /**
     * {@inheritDoc}
     * @throws RepositoryException 
     */
    @Override
    protected final boolean executeCommand(final String[] args) throws RepositoryException {
        VersionHistory vh = JcrWrapper.getVersionHistory();
        Version base = JcrWrapper.getCurrentNode().getBaseVersion();
        JcrShellPrinter.println("Current base: " + base.getName());
        TextOutput text = Output.out().a("Removing version:");
        VersionIterator vi = vh.getAllVersions();
        while (vi.hasNext()) {
            Version v  = vi.nextVersion();
            // current and root versions cannot be removed
            if (base.isSame(v) || vh.getRootVersion().isSame(v)) {
                continue;
            }
            text = text.a(" " + v.getName());
            vh.removeVersion(v.getName());
        }
        JcrShellPrinter.print(text.a("."));
        JcrShellPrinter.printOkln("Versions removed.");
        return true;
    }

    @Override
    protected boolean hasValidArgs(String[] args) {
        return args.length == 1;
    }
}
