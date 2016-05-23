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

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.version.VersionHistory;

import org.onehippo.forge.jcrshell.JcrWrapper;
import org.onehippo.forge.jcrshell.JcrShellPrinter;

/**
 * Create a new version in the version history.
 */
public class VersionCreate extends AbstractCommand {

    public VersionCreate() {
        super("versioncreate", new String[] { "createversion" }, "versioncreate [<label>]",
                "create a new versions of the current node");
    }

    /**
     * {@inheritDoc}
     * @throws RepositoryException 
     */
    @Override
    protected final boolean executeCommand(final String[] args) throws RepositoryException {
        VersionHistory vh = JcrWrapper.getVersionHistory();
        createVersion();
        String base = JcrWrapper.getCurrentNode().getBaseVersion().getName();
        if (args.length == 2) {
            vh.addVersionLabel(base, args[1], true);
            JcrShellPrinter.printOkln("Version " + base + " created with label '" + args[1] + "'.");
        } else {
            JcrShellPrinter.printOkln("Version " + base + " created.");
        }
        return true;
    }

    private void createVersion() throws RepositoryException {
        Node n = JcrWrapper.getCurrentNode();
        if (!n.isCheckedOut()) {
            n.checkout();
        }
        n.checkin();
        n.checkout();
    }

    @Override
    protected boolean hasValidArgs(String[] args) {
        return args.length == 1 || args.length == 2;
    }
}
