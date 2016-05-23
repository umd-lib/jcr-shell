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

import javax.jcr.RepositoryException;
import javax.jcr.version.VersionException;
import javax.jcr.version.VersionHistory;

import org.onehippo.forge.jcrshell.JcrWrapper;
import org.onehippo.forge.jcrshell.JcrShellPrinter;

/**
 * Remove a version from the version history.
 */
public class VersionRemove extends AbstractCommand {

    private static final ArgumentType[] ARGUMENTS = new ArgumentType[] { ArgumentType.VERSION };
    
    public VersionRemove() {
        super("versionremove", new String[] { "removeversion" }, "versionremove <version>|<label>",
                "remove a versions of the current node", ARGUMENTS);
    }

    /**
     * {@inheritDoc}
     * @throws RepositoryException 
     */
    @Override
    protected final boolean executeCommand(final String[] args) throws RepositoryException {
        VersionHistory vh = JcrWrapper.getVersionHistory();

        String name = args[1];
        if (vh.hasVersionLabel(args[1])) {
            name = vh.getVersionByLabel(args[1]).getName();
        }
        try {
            vh.removeVersion(name);
            JcrShellPrinter.printOkln("Version " + name + " removed.");
        } catch (VersionException e) {
            JcrShellPrinter.printWarnln("Unable to remove version: " + e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    protected boolean hasValidArgs(String[] args) {
        return args.length == 2;
    }
}
