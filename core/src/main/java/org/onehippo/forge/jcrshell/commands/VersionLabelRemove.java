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

import org.onehippo.forge.jcrshell.JcrShellPrinter;
import org.onehippo.forge.jcrshell.JcrWrapper;

/**
 * Remove a label from a version.
 */
public class VersionLabelRemove extends AbstractCommand {

    private static final ArgumentType[] ARGUMENTS = new ArgumentType[] { new ArgumentType(EnumSet.of(ArgumentType.Flags.VERSION, ArgumentType.Flags.ONLY_LABELS)) };

    public VersionLabelRemove() {
        super("versionlabelremove", new String[] { "removelabel" }, "versionslabelremove <label>",
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
            vh.removeVersionLabel(args[1]);
            JcrShellPrinter.printOkln("Label " + args[1] + " removed. ");
        } catch (VersionException e) {
            JcrShellPrinter.printWarnln("Unable to remove label: " + e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    protected boolean hasValidArgs(String[] args) {
        return args.length == 2;
    }

}
