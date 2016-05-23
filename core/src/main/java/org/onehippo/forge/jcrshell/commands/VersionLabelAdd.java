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

import java.util.EnumSet;

import javax.jcr.RepositoryException;
import javax.jcr.version.VersionException;
import javax.jcr.version.VersionHistory;

import org.onehippo.forge.jcrshell.JcrWrapper;
import org.onehippo.forge.jcrshell.JcrShellPrinter;

/**
 * Add a label to a version.
 */
public class VersionLabelAdd extends AbstractCommand {

    private static final ArgumentType[] ARGUMENTS = new ArgumentType[] { new ArgumentType(EnumSet.of(ArgumentType.Flags.VERSION, ArgumentType.Flags.NO_LABELS)) };

    public VersionLabelAdd() {
        super("versionlabeladd", new String[] { "addlabel" }, "versionslabeladd <version> <label> [<true|false>]",
                "add a label to a version", ARGUMENTS);
    }

    /**
     * {@inheritDoc}
     * @throws RepositoryException 
     */
    @Override
    protected final boolean executeCommand(final String[] args) throws RepositoryException {
        VersionHistory vh = JcrWrapper.getVersionHistory();
        try {
            vh.addVersionLabel(args[1], args[2], getMoveLabel(args));
            JcrShellPrinter.printOkln("Label " + args[2] + " added to versoin " + args[1]);
        } catch (VersionException e) {
            JcrShellPrinter.printWarnln("Unable to add label: " + e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    protected boolean hasValidArgs(String[] args) {
        return args.length == 3 || args.length == 4;
    }

    private boolean getMoveLabel(final String[] args) {
        if (args.length == 4) {
            return "true".equalsIgnoreCase(args[3]);
        } else {
            return false;
        }
    }
}
