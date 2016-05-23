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
import java.util.List;

import javax.jcr.RepositoryException;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionIterator;

import org.apache.jackrabbit.util.ISO8601;
import org.onehippo.forge.jcrshell.JcrShellPrinter;
import org.onehippo.forge.jcrshell.JcrWrapper;
import org.onehippo.forge.jcrshell.util.StringUtils;

/**
 * Save the current node.
 */
public class VersionList extends AbstractCommand {

    public VersionList() {
        super("versionlist", new String[] { "listversions" }, "versionlist",
                "show a list of versions of the current node");
    }

    /**
     * {@inheritDoc}
     * @throws RepositoryException 
     */
    @Override
    protected final boolean executeCommand(final String[] args) throws RepositoryException {
        VersionHistory vh = JcrWrapper.getVersionHistory();
        VersionIterator vi = vh.getAllVersions();

        List<String[]> rows = new ArrayList<String[]>();
        rows.add(new String[] { "Name", "Date", "Labels", "uuid" });
        while (vi.hasNext()) {
            Version v = vi.nextVersion();
            rows.add(new String[] { v.getName(), ISO8601.format(v.getCreated()),
                    StringUtils.join(vh.getVersionLabels(v), ", "), v.getUUID() });
        }
        JcrShellPrinter.printTableWithHeader(rows);
        return true;
    }

    @Override
    protected boolean hasValidArgs(String[] args) {
        return args.length == 1;
    }
}
